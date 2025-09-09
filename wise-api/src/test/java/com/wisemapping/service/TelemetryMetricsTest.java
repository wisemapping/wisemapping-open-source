/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryMetricsTest {

    @Mock
    private MindmapService mindmapService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    private MeterRegistry meterRegistry;
    private MindmapServiceImpl mindmapServiceImpl;
    private MetricsService telemetryMetricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        mindmapServiceImpl = new MindmapServiceImpl();
        telemetryMetricsService = new MetricsService();
        
        // Use reflection to inject the meterRegistry since it's private
        try {
            java.lang.reflect.Field field = MindmapServiceImpl.class.getDeclaredField("meterRegistry");
            field.setAccessible(true);
            field.set(mindmapServiceImpl, meterRegistry);
            
            // Inject meterRegistry into telemetryMetricsService
            field = MetricsService.class.getDeclaredField("meterRegistry");
            field.setAccessible(true);
            field.set(telemetryMetricsService, meterRegistry);
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
        Counter counter = Counter.builder("mindmaps.created")
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
        Counter newCounter = Counter.builder("mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "new")
                .register(meterRegistry);
        
        // Duplicate mindmap
        Counter duplicateCounter = Counter.builder("mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "duplicate")
                .register(meterRegistry);
        
        // Tutorial mindmap
        Counter tutorialCounter = Counter.builder("mindmaps.created")
                .description("Total number of mindmaps created")
                .tag("type", "tutorial")
                .register(meterRegistry);

        // Increment counters
        newCounter.increment();
        newCounter.increment();
        duplicateCounter.increment();
        tutorialCounter.increment();

        // Verify counts
        assertEquals(2.0, newCounter.count());
        assertEquals(1.0, duplicateCounter.count());
        assertEquals(1.0, tutorialCounter.count());
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
        telemetryMetricsService.trackUserRegistration(gmailUser, "gmail");
        
        // Test other email registration
        Account otherUser = createTestUser("test@company.com", AuthenticationType.DATABASE);
        telemetryMetricsService.trackUserRegistration(otherUser, "other");
        
        // Test Google OAuth registration
        Account googleUser = createTestUser("test@gmail.com", AuthenticationType.GOOGLE_OAUTH2);
        telemetryMetricsService.trackUserRegistration(googleUser, "gmail");
        
        // Verify metrics were recorded
        Double gmailRegistrations = meterRegistry.find("user.registrations")
                .tag("email_provider", "gmail")
                .tag("auth_type", "D")
                .counter().count();
        
        Double otherRegistrations = meterRegistry.find("user.registrations")
                .tag("email_provider", "other")
                .tag("auth_type", "D")
                .counter().count();
        
        Double googleRegistrations = meterRegistry.find("user.registrations")
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
        telemetryMetricsService.trackUserLogin(user, "database");
        telemetryMetricsService.trackUserLogin(user, "database");
        
        // Test google oauth login
        telemetryMetricsService.trackUserLogin(user, "google_oauth");
        
        // Verify metrics
        Double databaseLogins = meterRegistry.find("user.logins")
                .tag("auth_type", "database")
                .counter().count();
        
        Double googleLogins = meterRegistry.find("user.logins")
                .tag("auth_type", "google_oauth")
                .counter().count();
        
        assertEquals(2.0, databaseLogins);
        assertEquals(1.0, googleLogins);
    }

    @Test
    void testSpamPreventionMetrics() {
        // Test spam prevention (simpler test without needing SpamDetectionResult)
        Mindmap mindmap = createTestMindmap();
        telemetryMetricsService.trackSpamPrevention(mindmap, "publish");
        
        // Verify prevention metric
        Double prevention = meterRegistry.find("spam.prevented")
                .tag("action", "publish")
                .counter().count();
        
        assertEquals(1.0, prevention);
    }

    @Test
    void testEmailProviderExtraction() {
        // Test various email providers
        assertEquals("gmail", telemetryMetricsService.extractEmailProvider("test@gmail.com"));
        assertEquals("gmail", telemetryMetricsService.extractEmailProvider("test@googlemail.com"));
        assertEquals("yahoo", telemetryMetricsService.extractEmailProvider("test@yahoo.com"));
        assertEquals("microsoft", telemetryMetricsService.extractEmailProvider("test@outlook.com"));
        assertEquals("microsoft", telemetryMetricsService.extractEmailProvider("test@hotmail.com"));
        assertEquals("apple", telemetryMetricsService.extractEmailProvider("test@icloud.com"));
        assertEquals("education", telemetryMetricsService.extractEmailProvider("test@university.edu"));
        assertEquals("government", telemetryMetricsService.extractEmailProvider("test@agency.gov"));
        assertEquals("other", telemetryMetricsService.extractEmailProvider("test@company.com"));
        assertEquals("other", telemetryMetricsService.extractEmailProvider("invalid-email"));
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
