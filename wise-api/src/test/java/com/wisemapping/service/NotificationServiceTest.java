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
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationService notificationService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MailerService mailerService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        ReflectionTestUtils.setField(notificationService, "messageSource", messageSource);
        ReflectionTestUtils.setField(notificationService, "mailerService", mailerService);
        ReflectionTestUtils.setField(notificationService, "baseUrl", "https://app.wisemapping.com");

        // Set up locale
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        // Mock mailer service
        when(mailerService.getServerSenderEmail()).thenReturn("noreply@wisemapping.com");
        when(mailerService.getSupportEmail()).thenReturn("support@wisemapping.com");
    }

    @Test
    void testNewCollaboration_EnglishTranslations() {
        // Arrange
        Mindmap mindmap = createMindmap(123, "Test Mindmap");
        Account user = createAccount("John Doe", "john@example.com");
        Collaboration collaboration = createCollaboration("collaborator@example.com");

        String mapEditUrl = "https://app.wisemapping.com/c/maps/123/edit?shared=true";

        // Mock message source responses (English)
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_SUBJECT"), any(), eq(Locale.ENGLISH)))
                .thenReturn("John Doe has shared a mind map with you");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_TITLE"), any(), eq(Locale.ENGLISH)))
                .thenReturn("I've shared <a href='" + mapEditUrl + "'>Test Mindmap</a> mindmap with you.");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_CLICK_TO_OPEN"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Click to open: <a href='" + mapEditUrl + "'>Test Mindmap</a>");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_MESSAGE_FROM"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Message from john@example.com:");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_ACCOUNT_INFO"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("Do you have a WiseMapping account? Don't worry, you can create an account for free.");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_TEAM"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("The WiseMapping Team");
        when(messageSource.getMessage(eq("EMAIL.DO_NOT_REPLAY"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Important: Do not reply this email.");

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, "Please review this map");

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = (ArgumentCaptor<Map<String, Object>>) (Object) ArgumentCaptor.forClass(Map.class);
        verify(mailerService).sendEmail(
                eq("noreply@wisemapping.com"),
                eq("collaborator@example.com"),
                eq("John Doe has shared a mind map with you"),
                modelCaptor.capture(),
                eq("newCollaboration.vm")
        );

        Map<String, Object> model = modelCaptor.getValue();

        // Verify all pre-rendered messages are in the model
        assertEquals("I've shared <a href='" + mapEditUrl + "'>Test Mindmap</a> mindmap with you.", 
                model.get("emailTitle"), "emailTitle should be pre-rendered with URL and title");
        assertEquals("Click to open: <a href='" + mapEditUrl + "'>Test Mindmap</a>", 
                model.get("emailClickToOpen"), "emailClickToOpen should be pre-rendered with URL and title");
        assertEquals("Message from john@example.com:", 
                model.get("emailMessageFrom"), "emailMessageFrom should be pre-rendered with sender email");
        assertEquals("Do you have a WiseMapping account? Don't worry, you can create an account for free.", 
                model.get("emailAccountInfo"), "emailAccountInfo should be present");
        assertEquals("The WiseMapping Team", 
                model.get("emailTeam"), "emailTeam should be present");

        // Verify other model properties
        assertEquals(mindmap, model.get("mindmap"));
        assertEquals(mapEditUrl, model.get("mapEditUrl"));
        assertEquals("https://app.wisemapping.com", model.get("baseUrl"));
        assertEquals("john@example.com", model.get("senderMail"));
        assertEquals("Please review this map", model.get("message"));
    }

    @Test
    void testNewCollaboration_SpanishTranslations() {
        // Arrange
        Locale spanishLocale = Locale.forLanguageTag("es");
        LocaleContextHolder.setLocale(spanishLocale);
        
        Mindmap mindmap = createMindmap(456, "Mapa de Prueba");
        Account user = createAccount("María García", "maria@example.com");
        Collaboration collaboration = createCollaboration("colaborador@example.com");

        String mapEditUrl = "https://app.wisemapping.com/c/maps/456/edit?shared=true";

        // Mock message source responses (Spanish)
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_SUBJECT"), any(), eq(spanishLocale)))
                .thenReturn("María García te ha compartido un mapa mental");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_TITLE"), any(), eq(spanishLocale)))
                .thenReturn("He compartido el mapa mental <a href='" + mapEditUrl + "'>Mapa de Prueba</a> contigo.");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_CLICK_TO_OPEN"), any(), eq(spanishLocale)))
                .thenReturn("Haz clic para abrir: <a href='" + mapEditUrl + "'>Mapa de Prueba</a>");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_MESSAGE_FROM"), any(), eq(spanishLocale)))
                .thenReturn("Mensaje de maria@example.com:");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_ACCOUNT_INFO"), isNull(), eq(spanishLocale)))
                .thenReturn("¿Tienes una cuenta de WiseMapping? No te preocupes, puedes crear una cuenta gratis.");
        when(messageSource.getMessage(eq("SHARE_MAP.EMAIL_TEAM"), isNull(), eq(spanishLocale)))
                .thenReturn("El equipo de WiseMapping");
        when(messageSource.getMessage(eq("EMAIL.DO_NOT_REPLAY"), any(), eq(spanishLocale)))
                .thenReturn("Importante: No respondas a este correo.");

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, null);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> modelCaptor = (ArgumentCaptor<Map<String, Object>>) (Object) ArgumentCaptor.forClass(Map.class);
        verify(mailerService).sendEmail(
                anyString(),
                anyString(),
                eq("María García te ha compartido un mapa mental"),
                modelCaptor.capture(),
                eq("newCollaboration.vm")
        );

        Map<String, Object> model = modelCaptor.getValue();

        // Verify Spanish translations are properly rendered
        assertEquals("He compartido el mapa mental <a href='" + mapEditUrl + "'>Mapa de Prueba</a> contigo.", 
                model.get("emailTitle"));
        assertEquals("Haz clic para abrir: <a href='" + mapEditUrl + "'>Mapa de Prueba</a>", 
                model.get("emailClickToOpen"));
        assertEquals("Mensaje de maria@example.com:", 
                model.get("emailMessageFrom"));
        assertEquals("¿Tienes una cuenta de WiseMapping? No te preocupes, puedes crear una cuenta gratis.", 
                model.get("emailAccountInfo"));
        assertEquals("El equipo de WiseMapping", 
                model.get("emailTeam"));
    }

    @Test
    void testNewCollaboration_WithoutMessage() {
        // Arrange
        Mindmap mindmap = createMindmap(789, "Another Map");
        Account user = createAccount("Test User", "test@example.com");
        Collaboration collaboration = createCollaboration("recipient@example.com");

        // Mock message source
        when(messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Mocked translation");

        // Act - no message parameter
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
        assertNull(model.get("message"), "Message should be null when not provided");
    }

    @Test
    void testNewCollaboration_VerifyMessageSourceCalls() {
        // Arrange
        Mindmap mindmap = createMindmap(999, "Test");
        Account user = createAccount("John Doe", "user@example.com");
        Collaboration collaboration = createCollaboration("collab@example.com");

        when(messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Translation");

        // Act
        notificationService.newCollaboration(collaboration, mindmap, user, "Test message");

        // Assert - Verify all translation keys are called with correct parameters
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_SUBJECT"), 
                argThat(args -> args != null && args.length == 1 && "John Doe".equals(args[0])), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_TITLE"), 
                argThat(args -> args != null && args.length == 2), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_CLICK_TO_OPEN"), 
                argThat(args -> args != null && args.length == 2), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_MESSAGE_FROM"), 
                argThat(args -> args != null && args.length == 1 && "user@example.com".equals(args[0])), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_ACCOUNT_INFO"), 
                isNull(), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("SHARE_MAP.EMAIL_TEAM"), 
                isNull(), 
                any(Locale.class));
        
        verify(messageSource).getMessage(eq("EMAIL.DO_NOT_REPLAY"), 
                any(), 
                any(Locale.class));
    }

    // Helper methods
    private Mindmap createMindmap(int id, String title) {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(id);
        mindmap.setTitle(title);
        return mindmap;
    }

    private Account createAccount(String fullName, String email) {
        Account account = new Account();
        account.setFirstname(fullName.split(" ")[0]);
        account.setLastname(fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ") + 1) : "");
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
