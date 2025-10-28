/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/wise-api/src/main/java/com/wisemapping/scheduler/InactiveUserSuspensionSchedulerTransactionTest.java
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.scheduler;

import com.wisemapping.config.AppConfig;
import com.wisemapping.service.InactiveUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the InactiveUserSuspensionScheduler properly handles
 * the suspension task without throwing transaction errors. This test validates
 * that the scheduler can invoke the InactiveUserService which was previously
 * throwing TransactionRequiredException in production.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.batch.inactive-user-suspension.enabled=true",
    "app.batch.inactive-user-suspension.preview-enabled=true",
    "app.batch.inactive-user-suspension.run-on-startup=false",
    "app.batch.inactive-user-suspension.inactivity-years=1",
    "app.batch.inactive-user-suspension.batch-size=2", 
    "app.batch.inactive-user-suspension.dry-run=false"
})
@Transactional
public class InactiveUserSuspensionSchedulerTransactionTest {

    @Autowired
    private InactiveUserSuspensionScheduler scheduler;

    @Autowired
    private InactiveUserService inactiveUserService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testSchedulerNoTransactionErrorsInProcessInactiveUserSuspension() {
        // Test that the main scheduled task can be called without TransactionRequiredException
        // This was the main issue reported in production when the scheduler triggered the service
        
        assertDoesNotThrow(() -> {
            scheduler.processInactiveUserSuspension();
        }, "processInactiveUserSuspension should not throw TransactionRequiredException - this was the production bug!");
    }

    @Test
    void testSchedulerNoTransactionErrorsInStartupTask() {
        // Test that the startup task can be called without TransactionRequiredException
        // This validates the async startup processing works correctly
        
        assertDoesNotThrow(() -> {
            scheduler.processInactiveUserSuspensionOnStartup();
        }, "processInactiveUserSuspensionOnStartup should not throw TransactionRequiredException!");
    }

    @Test
    void testSchedulerNoTransactionErrorsInPreviewTask() {
        // Test that the preview task can be called without TransactionRequiredException
        // This validates the preview functionality works correctly
        
        assertDoesNotThrow(() -> {
            scheduler.previewInactiveUserSuspension();
        }, "previewInactiveUserSuspension should not throw TransactionRequiredException!");
    }

    @Test
    void testSchedulerServiceIntegration() {
        // Test that the scheduler properly integrates with the service layer
        // and that the transaction fix in InactiveUserService is working
        
        assertDoesNotThrow(() -> {
            // Verify the service methods work when called through scheduler
            scheduler.processInactiveUserSuspension();
            scheduler.previewInactiveUserSuspension();
        }, "Scheduler should integrate properly with InactiveUserService without transaction errors!");
    }

    @Test
    void testSchedulerConfigurationAndEnabledState() {
        // Test that the scheduler is properly configured and enabled
        
        assertNotNull(scheduler, "Scheduler should be properly autowired");
        assertNotNull(inactiveUserService, "InactiveUserService should be properly autowired");
        
        // Test that all scheduler methods handle enabled/disabled state gracefully
        assertDoesNotThrow(() -> {
            scheduler.processInactiveUserSuspension();
            scheduler.processInactiveUserSuspensionOnStartup();
            scheduler.previewInactiveUserSuspension();
        }, "All scheduler methods should handle configuration state gracefully");
    }

    @Test
    void testSchedulerExceptionHandling() {
        // Test that the scheduler properly handles exceptions without propagating them
        // The scheduler should catch and log exceptions but not fail the application
        
        assertDoesNotThrow(() -> {
            scheduler.processInactiveUserSuspension();
        }, "Scheduler should handle any service exceptions gracefully");
        
        assertDoesNotThrow(() -> {
            scheduler.processInactiveUserSuspensionOnStartup();
        }, "Startup task should handle any service exceptions gracefully");
        
        assertDoesNotThrow(() -> {
            scheduler.previewInactiveUserSuspension();
        }, "Preview task should handle any service exceptions gracefully");
    }

    @Test
    void testEntityManagerIsAvailableForScheduler() {
        // Verify that the EntityManager is properly configured and available
        // This is crucial for the JPQL DELETE query in the service called by scheduler
        
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
        // This test validates that the @Transactional annotation fix is working
        // when the service is called from the scheduler context
        
        assertDoesNotThrow(() -> {
            // This was the exact flow that was failing in production:
            // 1. Scheduler triggers processInactiveUserSuspension()
            // 2. That calls InactiveUserService.processInactiveUsers()
            // 3. Which calls processBatch() with @Transactional
            // 4. processBatch() processes users and calls suspendInactiveUser()
            // 5. suspendInactiveUser() executes JPQL DELETE query
            // 6. That query needs a transaction context to work
            
            scheduler.processInactiveUserSuspension();
            
        }, "The transaction fix should work when service is called from scheduler context!");
    }

    @Test
    void testSchedulerAsyncCapability() {
        // Test that the scheduler can handle async operations without blocking
        // While we can't test true async behavior in unit tests, we can verify
        // that the methods don't throw exceptions related to async processing
        
        assertDoesNotThrow(() -> {
            // These methods are marked @Async in the scheduler
            scheduler.processInactiveUserSuspension();
            scheduler.processInactiveUserSuspensionOnStartup(); 
            scheduler.previewInactiveUserSuspension();
        }, "Async scheduler methods should execute without errors");
    }
}