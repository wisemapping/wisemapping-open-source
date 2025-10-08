/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.service;

import com.wisemapping.model.Account;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for NotificationService that uses real ResourceBundleMessageSource
 * to verify that parameter substitution works correctly with actual message properties.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceIntegrationTest {

    private NotificationService notificationService;
    private ResourceBundleMessageSource messageSource;

    @Mock
    private MailerService mailerService;

    @BeforeEach
    void setUp() {
        // Create a real ResourceBundleMessageSource (not mocked)
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        notificationService = new NotificationService();
        ReflectionTestUtils.setField(notificationService, "messageSource", messageSource);
        ReflectionTestUtils.setField(notificationService, "mailerService", mailerService);
        ReflectionTestUtils.setField(notificationService, "baseUrl", "https://app.wisemapping.com");

        // Mock mailer service
        when(mailerService.getServerSenderEmail()).thenReturn("noreply@wisemapping.com");
        when(mailerService.getSupportEmail()).thenReturn("support@wisemapping.com");
    }

    @Test
    void testNewCollaboration_EnglishParameterSubstitution() {
        // Set English locale
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // Arrange
        Mindmap mindmap = createMindmap(123, "My Test Map");
        Account user = createAccount("John", "Doe", "john@example.com");
        Collaboration collaboration = createCollaboration("collaborator@example.com");

        String expectedMapEditUrl = "https://app.wisemapping.com/c/maps/123/edit";

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, "Please review this map");

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = (ArgumentCaptor<Map<String, Object>>) (Object) ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(mailerService).sendEmail(
                eq("noreply@wisemapping.com"),
                eq("collaborator@example.com"),
                subjectCaptor.capture(),
                modelCaptor.capture(),
                eq("newCollaboration.vm")
        );

        String subject = subjectCaptor.getValue();
        Map<String, Object> model = modelCaptor.getValue();

        // Print actual values for debugging FIRST
        System.out.println("=== English Translation Test Results ===");
        System.out.println("Subject: " + subject);
        System.out.println("emailTitle: " + model.get("emailTitle"));
        System.out.println("emailClickToOpen: " + model.get("emailClickToOpen"));
        System.out.println("emailMessageFrom: " + model.get("emailMessageFrom"));
        System.out.println("emailAccountInfo: " + model.get("emailAccountInfo"));
        System.out.println("emailTeam: " + model.get("emailTeam"));
        System.out.println("doNotReplay: " + model.get("doNotReplay"));

        // Verify subject has user name substituted
        assertEquals("John Doe has shared a mind map with you", subject,
                "Subject should have {0} parameter replaced with user's full name");

        // Verify emailTitle has BOTH parameters substituted (URL and mindmap title)
        String emailTitle = (String) model.get("emailTitle");
        assertNotNull(emailTitle, "emailTitle should not be null");
        assertFalse(emailTitle.contains("{0}"), "emailTitle should not contain {0} parameter");
        assertFalse(emailTitle.contains("{1}"), "emailTitle should not contain {1} parameter");
        assertTrue(emailTitle.contains(expectedMapEditUrl), 
                "emailTitle should contain the actual map URL, not {0}");
        assertTrue(emailTitle.contains("My Test Map"), 
                "emailTitle should contain the actual mindmap title, not {1}");
        assertTrue(emailTitle.contains("I've shared") || emailTitle.contains("I've shared"), 
                "emailTitle should contain proper apostrophe in I've");

        // Verify emailClickToOpen has parameters substituted
        String emailClickToOpen = (String) model.get("emailClickToOpen");
        assertNotNull(emailClickToOpen, "emailClickToOpen should not be null");
        assertFalse(emailClickToOpen.contains("{0}"), "emailClickToOpen should not contain {0} parameter");
        assertFalse(emailClickToOpen.contains("{1}"), "emailClickToOpen should not contain {1} parameter");
        assertTrue(emailClickToOpen.contains(expectedMapEditUrl), 
                "emailClickToOpen should contain the actual URL");
        assertTrue(emailClickToOpen.contains("My Test Map"), 
                "emailClickToOpen should contain the actual mindmap title");

        // Verify emailMessageFrom has parameter substituted
        String emailMessageFrom = (String) model.get("emailMessageFrom");
        assertNotNull(emailMessageFrom, "emailMessageFrom should not be null");
        assertFalse(emailMessageFrom.contains("{0}"), "emailMessageFrom should not contain {0} parameter");
        assertTrue(emailMessageFrom.contains("john@example.com"), 
                "emailMessageFrom should contain the actual email address");

        // Verify other properties don't have parameters (they shouldn't)
        String emailAccountInfo = (String) model.get("emailAccountInfo");
        assertNotNull(emailAccountInfo, "emailAccountInfo should not be null");
        assertFalse(emailAccountInfo.contains("{"), 
                "emailAccountInfo should not contain any unreplaced parameters");

        String emailTeam = (String) model.get("emailTeam");
        assertNotNull(emailTeam, "emailTeam should not be null");
        assertFalse(emailTeam.contains("{"), 
                "emailTeam should not contain any unreplaced parameters");

        String doNotReplay = (String) model.get("doNotReplay");
        assertNotNull(doNotReplay, "doNotReplay should not be null");
        assertFalse(doNotReplay.contains("{0}"), "doNotReplay should not contain {0} parameter");
        assertTrue(doNotReplay.contains("support@wisemapping.com"), 
                "doNotReplay should contain the actual support email");
    }

    @Test
    void testNewCollaboration_SpanishParameterSubstitution() {
        // Set Spanish locale
        Locale spanishLocale = Locale.forLanguageTag("es");
        LocaleContextHolder.setLocale(spanishLocale);

        // Arrange
        Mindmap mindmap = createMindmap(456, "Mapa de Prueba");
        Account user = createAccount("María", "García", "maria@example.com");
        Collaboration collaboration = createCollaboration("colaborador@example.com");

        String expectedMapEditUrl = "https://app.wisemapping.com/c/maps/456/edit";

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, null);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = (ArgumentCaptor<Map<String, Object>>) (Object) ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(mailerService).sendEmail(
                anyString(),
                anyString(),
                subjectCaptor.capture(),
                modelCaptor.capture(),
                eq("newCollaboration.vm")
        );

        String subject = subjectCaptor.getValue();
        Map<String, Object> model = modelCaptor.getValue();

        // Verify Spanish translations have parameters substituted
        assertTrue(subject.contains("María García"), 
                "Spanish subject should contain user's full name");

        String emailTitle = (String) model.get("emailTitle");
        assertNotNull(emailTitle, "emailTitle should not be null");
        assertFalse(emailTitle.contains("{0}"), "emailTitle should not contain {0} parameter");
        assertFalse(emailTitle.contains("{1}"), "emailTitle should not contain {1} parameter");
        assertTrue(emailTitle.contains(expectedMapEditUrl), 
                "Spanish emailTitle should contain the actual map URL");
        assertTrue(emailTitle.contains("Mapa de Prueba"), 
                "Spanish emailTitle should contain the actual mindmap title");

        // Print actual values for debugging
        System.out.println("=== Spanish Translation Test Results ===");
        System.out.println("Subject: " + subject);
        System.out.println("emailTitle: " + emailTitle);
        System.out.println("emailClickToOpen: " + model.get("emailClickToOpen"));
        System.out.println("emailMessageFrom: " + model.get("emailMessageFrom"));
    }

    @Test
    void testNewCollaboration_ChineseParameterSubstitution() {
        // Set Chinese locale
        Locale chineseLocale = Locale.SIMPLIFIED_CHINESE;
        LocaleContextHolder.setLocale(chineseLocale);

        // Arrange
        Mindmap mindmap = createMindmap(789, "测试地图");
        Account user = createAccount("李", "明", "li@example.com");
        Collaboration collaboration = createCollaboration("collaborator@example.com");

        String expectedMapEditUrl = "https://app.wisemapping.com/c/maps/789/edit";

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, null);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = (ArgumentCaptor<Map<String, Object>>) (Object) ArgumentCaptor.forClass(Map.class);
        
        verify(mailerService).sendEmail(
                anyString(),
                anyString(),
                anyString(),
                modelCaptor.capture(),
                eq("newCollaboration.vm")
        );

        Map<String, Object> model = modelCaptor.getValue();

        // Verify Chinese translations have parameters substituted
        String emailTitle = (String) model.get("emailTitle");
        assertNotNull(emailTitle, "emailTitle should not be null");
        assertFalse(emailTitle.contains("{0}"), "emailTitle should not contain {0} parameter");
        assertFalse(emailTitle.contains("{1}"), "emailTitle should not contain {1} parameter");
        assertTrue(emailTitle.contains(expectedMapEditUrl), 
                "Chinese emailTitle should contain the actual map URL");
        assertTrue(emailTitle.contains("测试地图"), 
                "Chinese emailTitle should contain the actual mindmap title");

        // Print actual values for debugging
        System.out.println("=== Chinese Translation Test Results ===");
        System.out.println("emailTitle: " + emailTitle);
        System.out.println("emailClickToOpen: " + model.get("emailClickToOpen"));
    }

    // Helper methods
    private Mindmap createMindmap(int id, String title) {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(id);
        mindmap.setTitle(title);
        return mindmap;
    }

    private Account createAccount(String firstName, String lastName, String email) {
        Account account = new Account();
        account.setFirstname(firstName);
        account.setLastname(lastName);
        account.setEmail(email);
        return account;
    }

    private Collaboration createCollaboration(String email) {
        Collaborator collaborator = new Collaborator();
        collaborator.setEmail(email);
        
        Collaboration collaboration = new Collaboration();
        collaboration.setCollaborator(collaborator);
        return collaboration;
    }
}

