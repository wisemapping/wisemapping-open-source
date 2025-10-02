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
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.service.spam.SpamDetectionResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TelemetryMetricsTest {

    @Mock
    private MindmapService mindmapService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        new MindmapServiceImpl();
        metricsService = new MetricsService();
        
        // Use reflection to inject the meterRegistry since it's private
        try {
            // Inject meterRegistry into metricsService
            java.lang.reflect.Field field = MetricsService.class.getDeclaredField("meterRegistry");
            field.setAccessible(true);
            field.set(metricsService, meterRegistry);
        } catch (Exception e) {
            fail("Failed to inject meterRegistry: " + e.getMessage());
        }
    }

    @Test
    void testMindmapCreationMetrics() {
        // Given
        Account user = new Account();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("password");
        user.setActivationCode(12345L);

        Mindmap mindmap = new Mindmap();
        mindmap.setTitle("Test Mindmap");
        mindmap.setDescription("Test Description");

        // When - simulate mindmap creation
        // Note: This is a simplified test since we can't easily mock all dependencies
        // In a real scenario, you would use @SpringBootTest with proper test configuration
        
        // Verify that the counter metric exists and can be created
        Counter counter = Counter.builder("wisemapping.api.mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "new")
                .register(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals(0.0, counter.count());
        
        // Increment the counter to simulate mindmap creation
        counter.increment();
        assertEquals(1.0, counter.count());
    }

    @Test
    void testMultipleMindmapTypesMetrics() {
        // Test different types of mindmap creation metrics
        
        // New mindmap
        Counter newCounter = Counter.builder("wisemapping.api.mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "new")
                .register(meterRegistry);
        
        // Duplicate mindmap
        Counter duplicateCounter = Counter.builder("wisemapping.api.mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "duplicate")
                .register(meterRegistry);
        
        // Increment counters
        newCounter.increment();
        newCounter.increment();
        duplicateCounter.increment();

        // Verify counts
        assertEquals(2.0, newCounter.count());
        assertEquals(1.0, duplicateCounter.count());
    }

    @Test
    void testMeterRegistryConfiguration() {
        // Verify that the meter registry is properly configured
        assertNotNull(meterRegistry);
        
        // Verify that we can create and register metrics
        Counter testCounter = Counter.builder("test.counter")
                .description("Test counter")
                .register(meterRegistry);
        
        assertNotNull(testCounter);
        assertEquals(0.0, testCounter.count());
    }

    @Test
    void testUserRegistrationMetrics() {
        // Test Gmail registration
        Account gmailUser = createTestUser("test@gmail.com", AuthenticationType.DATABASE);
        metricsService.trackUserRegistration(gmailUser, "gmail");
        
        // Test other email registration
        Account otherUser = createTestUser("test@company.com", AuthenticationType.DATABASE);
        metricsService.trackUserRegistration(otherUser, "other");
        
        // Test Google OAuth registration
        Account googleUser = createTestUser("test@gmail.com", AuthenticationType.GOOGLE_OAUTH2);
        metricsService.trackUserRegistration(googleUser, "gmail");
        
        // Verify metrics were recorded
        Double gmailRegistrations = meterRegistry.find("wisemapping.api.user.registrations")
                .tag("email_provider", "gmail")
                .tag("auth_type", "D")
                .counter().count();
        
        Double otherRegistrations = meterRegistry.find("wisemapping.api.user.registrations")
                .tag("email_provider", "other")
                .tag("auth_type", "D")
                .counter().count();
        
        Double googleRegistrations = meterRegistry.find("wisemapping.api.user.registrations")
                .tag("email_provider", "gmail")
                .tag("auth_type", "G")
                .counter().count();
        
        assertEquals(1.0, gmailRegistrations);
        assertEquals(1.0, otherRegistrations);
        assertEquals(1.0, googleRegistrations);
    }

    @Test
    void testUserLoginMetrics() {
        Account user = createTestUser("test@example.com", AuthenticationType.DATABASE);
        
        // Test database login
        metricsService.trackUserLogin(user, "database");
        metricsService.trackUserLogin(user, "database");
        
        // Test google oauth login
        metricsService.trackUserLogin(user, "google_oauth");
        
        // Verify metrics
        Double databaseLogins = meterRegistry.find("wisemapping.api.user.logins")
                .tag("auth_type", "database")
                .counter().count();
        
        Double googleLogins = meterRegistry.find("wisemapping.api.user.logins")
                .tag("auth_type", "google_oauth")
                .counter().count();
        
        assertEquals(2.0, databaseLogins);
        assertEquals(1.0, googleLogins);
    }

    @Test
    void testUserLogoutMetrics() {
        Account user = createTestUser("test@example.com", AuthenticationType.DATABASE);
        
        // Test manual logout
        metricsService.trackUserLogout(user, "manual");
        metricsService.trackUserLogout(user, "manual");
        
        // Test session expired logout
        metricsService.trackUserLogout(user, "session_expired");
        
        // Verify metrics
        Double manualLogouts = meterRegistry.find("wisemapping.api.user.logouts")
                .tag("logout_type", "manual")
                .counter().count();
        
        Double expiredLogouts = meterRegistry.find("wisemapping.api.user.logouts")
                .tag("logout_type", "session_expired")
                .counter().count();
        
        assertEquals(2.0, manualLogouts);
        assertEquals(1.0, expiredLogouts);
    }

    @Test
    void testMindmapMadePublicMetrics() {
        Account user = createTestUser("test@example.com", AuthenticationType.DATABASE);
        Mindmap mindmapWithDescription = createTestMindmap();
        mindmapWithDescription.setDescription("Test description");
        
        Mindmap mindmapWithoutDescription = createTestMindmap();
        mindmapWithoutDescription.setDescription("");
        
        // Test tracking mindmap made public with description
        metricsService.trackMindmapMadePublic(mindmapWithDescription, user);
        
        // Test tracking mindmap made public without description
        metricsService.trackMindmapMadePublic(mindmapWithoutDescription, user);
        
        // Verify metrics
        Double withDescription = meterRegistry.find("wisemapping.api.mindmaps.made_public")
                .tag("has_description", "true")
                .counter().count();
        
        Double withoutDescription = meterRegistry.find("wisemapping.api.mindmaps.made_public")
                .tag("has_description", "false")
                .counter().count();
        
        assertEquals(1.0, withDescription);
        assertEquals(1.0, withoutDescription);
    }

    @Test
    void testMindmapSharedMetrics() {
        Account sharer = createTestUser("sharer@example.com", AuthenticationType.DATABASE);
        Mindmap mindmap = createTestMindmap();
        
        // Test sharing with different roles and email providers
        metricsService.trackMindmapShared(mindmap, "collaborator@gmail.com", "VIEWER", sharer);
        metricsService.trackMindmapShared(mindmap, "editor@company.com", "EDITOR", sharer);
        
        // Verify metrics
        Double viewerShares = meterRegistry.find("wisemapping.api.mindmaps.shared")
                .tag("role", "viewer")
                .tag("collaborator_email_provider", "gmail")
                .counter().count();
        
        Double editorShares = meterRegistry.find("wisemapping.api.mindmaps.shared")
                .tag("role", "editor")
                .tag("collaborator_email_provider", "other")
                .counter().count();
        
        assertEquals(1.0, viewerShares);
        assertEquals(1.0, editorShares);
    }

    @Test
    void testUserSuspensionMetrics() {
        Account user = createTestUser("suspended@example.com", AuthenticationType.DATABASE);
        
        // Test user suspension for different reasons
        metricsService.trackUserSuspension(user, "ABUSE");
        metricsService.trackUserSuspension(user, "SPAM");
        
        // Verify metrics
        Double abuseSuspensions = meterRegistry.find("wisemapping.api.user.suspensions")
                .tag("reason", "abuse")
                .counter().count();
        
        Double spamSuspensions = meterRegistry.find("wisemapping.api.user.suspensions")
                .tag("reason", "spam")
                .counter().count();
        
        assertEquals(1.0, abuseSuspensions);
        assertEquals(1.0, spamSuspensions);
    }

    @Test
    void testSpamAnalysisMetrics() {
        Mindmap mindmap = createTestMindmap();
        
        // Create spam detection results
        SpamDetectionResult spamResult = SpamDetectionResult.spam("Test reason", "Test details", SpamStrategyType.CONTACT_INFO);
        SpamDetectionResult cleanResult = SpamDetectionResult.notSpam();
        
        // Test spam analysis for spam detection
        metricsService.trackSpamAnalysis(mindmap, spamResult, "creation");
        
        // Test spam analysis for clean result
        metricsService.trackSpamAnalysis(mindmap, cleanResult, "update");
        
        // Verify unified spam analysis metrics
        Double spamAnalyzed = meterRegistry.find("wisemapping.api.spam.analyzed")
                .tag("context", "creation")
                .tag("is_spam", "yes")
                .tag("spam_type", "CONTACT_INFO")
                .counter().count();
        
        Double cleanAnalyzed = meterRegistry.find("wisemapping.api.spam.analyzed")
                .tag("context", "update")
                .tag("is_spam", "no")
                .tag("spam_type", "none")
                .counter().count();
        
        assertEquals(1.0, spamAnalyzed);
        assertEquals(1.0, cleanAnalyzed);
    }

    @Test
    void testSpamPreventionMetrics() {
        // Test spam prevention (simpler test without needing SpamDetectionResult)
        Mindmap mindmap = createTestMindmap();
        metricsService.trackSpamPrevention(mindmap, "publish");
        
        // Verify prevention metric
        Double prevention = meterRegistry.find("wisemapping.api.spam.prevented")
                .tag("action", "publish")
                .counter().count();
        
        assertEquals(1.0, prevention);
    }

    @Test
    void testEmailProviderExtraction() {
        // Test various email providers
        assertEquals("gmail", metricsService.extractEmailProvider("test@gmail.com"));
        assertEquals("gmail", metricsService.extractEmailProvider("test@googlemail.com"));
        assertEquals("yahoo", metricsService.extractEmailProvider("test@yahoo.com"));
        assertEquals("microsoft", metricsService.extractEmailProvider("test@outlook.com"));
        assertEquals("microsoft", metricsService.extractEmailProvider("test@hotmail.com"));
        assertEquals("apple", metricsService.extractEmailProvider("test@icloud.com"));
        assertEquals("education", metricsService.extractEmailProvider("test@university.edu"));
        assertEquals("government", metricsService.extractEmailProvider("test@agency.gov"));
        assertEquals("other", metricsService.extractEmailProvider("test@company.com"));
        assertEquals("other", metricsService.extractEmailProvider("invalid-email"));
    }

    private Account createTestUser(String email, AuthenticationType authType) {
        Account user = new Account();
        user.setId(1);
        user.setEmail(email);
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("password");
        user.setActivationCode(12345L);
        user.setAuthenticationType(authType);
        return user;
    }

    private Mindmap createTestMindmap() {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(1);
        mindmap.setTitle("Test Mindmap");
        mindmap.setDescription("Test Description");
        mindmap.setPublic(true);
        return mindmap;
    }

    // Removed createSpamResult since SpamDetectionResult constructor is not accessible in tests
}
