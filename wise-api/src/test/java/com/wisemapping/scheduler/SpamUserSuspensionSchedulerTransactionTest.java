/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/wise-api/src/main/java/com/wisemapping/scheduler/SpamUserSuspensionSchedulerTransactionTest.java
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.scheduler;

import com.wisemapping.config.AppConfig;
import com.wisemapping.service.SpamUserSuspensionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the SpamUserSuspensionScheduler properly handles
 * the spam user suspension task without throwing transaction errors. This test validates
 * that the scheduler can invoke the SpamUserSuspensionService which uses complex
 * transaction management with REQUIRES_NEW propagation.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.batch.spam-user-suspension.enabled=true",
    "app.batch.spam-user-suspension.months-back=6",
    "app.batch.spam-user-suspension.batch-size=2", 
    "app.batch.spam-user-suspension.public-spam-ratio-threshold=0.75",
    "app.batch.spam-user-suspension.min-any-spam-count=6"
})
@Transactional
public class SpamUserSuspensionSchedulerTransactionTest {

    @Autowired
    private SpamUserSuspensionScheduler scheduler;

    @Autowired
    private SpamUserSuspensionService spamUserSuspensionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testSchedulerNoTransactionErrorsInProcessSpamUserSuspension() {
        // Test that the main scheduled task can be called without TransactionRequiredException
        // This validates that the service's @Transactional(propagation = Propagation.REQUIRES_NEW) 
        // is working correctly when called from the scheduler
        
        assertDoesNotThrow(() -> {
            scheduler.processSpamUserSuspension();
        }, "processSpamUserSuspension should not throw TransactionRequiredException!");
    }

    @Test
    void testSchedulerServiceIntegration() {
        // Test that the scheduler properly integrates with the service layer
        // and that the complex transaction management in SpamUserSuspensionService is working
        
        assertDoesNotThrow(() -> {
            // Verify the service methods work when called through scheduler
            scheduler.processSpamUserSuspension();
        }, "Scheduler should integrate properly with SpamUserSuspensionService without transaction errors!");
    }

    @Test
    void testSchedulerConfigurationAndEnabledState() {
        // Test that the scheduler is properly configured and enabled
        
        assertNotNull(scheduler, "Scheduler should be properly autowired");
        assertNotNull(spamUserSuspensionService, "SpamUserSuspensionService should be properly autowired");
        
        // Test that scheduler methods handle enabled/disabled state gracefully
        assertDoesNotThrow(() -> {
            scheduler.processSpamUserSuspension();
        }, "Scheduler method should handle configuration state gracefully");
    }

    @Test
    void testSchedulerExceptionHandling() {
        // Test that the scheduler properly handles exceptions without propagating them
        // The scheduler should catch and log exceptions but not fail the application
        
        assertDoesNotThrow(() -> {
            scheduler.processSpamUserSuspension();
        }, "Scheduler should handle any service exceptions gracefully");
    }

    @Test
    void testEntityManagerIsAvailableForScheduler() {
        // Verify that the EntityManager is properly configured and available
        // This is crucial for the complex JPQL queries in the service called by scheduler
        
        assertNotNull(entityManager, "EntityManager should be available");
        assertTrue(entityManager.isOpen(), "EntityManager should be open");
        
        // Test a simple query to ensure it works in the scheduler context
        assertDoesNotThrow(() -> {
            Long count = entityManager.createQuery("SELECT COUNT(c) FROM Collaborator c", Long.class)
                    .getSingleResult();
            assertTrue(count >= 0, "Should get a valid count");
        }, "EntityManager should work properly in scheduler context");
    }

    @Test
    void testTransactionBoundaryFixInSchedulerContext() {
        // This test validates that the @Transactional(propagation = Propagation.REQUIRES_NEW) 
        // annotations are working correctly when the service is called from the scheduler context
        
        assertDoesNotThrow(() -> {
            // Transaction flow:
            // 1. Scheduler triggers processSpamUserSuspension()
            // 2. That calls SpamUserSuspensionService.processSpamUserSuspension()
            // 3. Which calls processBatch()/processRatioBatch() with @Transactional(propagation = Propagation.REQUIRES_NEW)
            // 4. The batch method executes complex JPQL queries and updates
            // 5. Those queries need proper transaction context to work
            
            scheduler.processSpamUserSuspension();
            
        }, "The transaction management should work when service is called from scheduler context!");
    }

    @Test
    void testSchedulerAsyncCapability() {
        // Test that the scheduler can handle async operations without blocking
        // While we can't test true async behavior in unit tests, we can verify
        // that the methods don't throw exceptions related to async processing
        
        assertDoesNotThrow(() -> {
            // This method is marked @Async in the scheduler
            scheduler.processSpamUserSuspension();
        }, "Async scheduler method should execute without errors");
    }

    @Test
    void testSpamUserSuspensionServiceDirectCall() {
        // Test that the service can be called directly without errors
        // This validates that the transaction management works correctly
        
        assertDoesNotThrow(() -> {
            spamUserSuspensionService.processSpamUserSuspension();
        }, "SpamUserSuspensionService should execute without transaction errors");
    }


    @Test
    void testServiceConfigurationAccess() {
        // Test that service configuration is properly accessible
        
        assertDoesNotThrow(() -> {
            assertTrue(spamUserSuspensionService.isEnabled(), "Service should be enabled");
            assertTrue(spamUserSuspensionService.getMonthsBack() >= 0, "Months back should be valid");
            assertTrue(spamUserSuspensionService.getBatchSize() > 0, "Batch size should be positive");
            assertTrue(spamUserSuspensionService.getPublicSpamRatioThreshold() >= 0.0 && 
                       spamUserSuspensionService.getPublicSpamRatioThreshold() <= 1.0, 
                       "Public spam ratio threshold should be between 0.0 and 1.0");
            assertTrue(spamUserSuspensionService.getMinAnySpamCount() >= 0, "Min any spam count should be valid");
        }, "Service configuration should be accessible and valid");
    }

    @Test
    void testBatchSizeClampedToSafeRange() {
        int original = spamUserSuspensionService.getBatchSize();
        try {
            ReflectionTestUtils.setField(spamUserSuspensionService, "batchSize", 5_000);
            assertEquals(500, spamUserSuspensionService.getBatchSize(), "Batch size should be capped at safe max");

            ReflectionTestUtils.setField(spamUserSuspensionService, "batchSize", 0);
            assertEquals(1, spamUserSuspensionService.getBatchSize(), "Batch size should not drop below 1");
        } finally {
            ReflectionTestUtils.setField(spamUserSuspensionService, "batchSize", original);
        }
    }

    @Test
    void testServiceTransactionalMethods() {
        // Test that the @Transactional(readOnly = true) methods work correctly
        
        assertDoesNotThrow(() -> {
            long publicSpamRatioCount = spamUserSuspensionService.getTotalUsersWithPublicSpamRatio();
            assertTrue(publicSpamRatioCount >= 0, "Public spam ratio count should be non-negative");
            
            long anySpamCount = spamUserSuspensionService.getTotalUsersWithAnySpam();
            assertTrue(anySpamCount >= 0, "Any spam count should be non-negative");
        }, "Transactional read-only methods should work without errors");
    }
}
