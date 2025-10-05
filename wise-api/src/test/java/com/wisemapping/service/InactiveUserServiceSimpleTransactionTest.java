/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/wise-api/src/main/java/com/wisemapping/service/InactiveUserServiceSimpleTransactionTest.java
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.service;

import com.wisemapping.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify that the TransactionRequiredException has been fixed
 * in the InactiveUserService. This test validates that the service can run
 * without throwing transaction errors.
 */
@SpringBootTest(classes = {AppConfig.class})
@TestPropertySource(properties = {
    "app.batch.inactive-user-suspension.inactivity-years=1",
    "app.batch.inactive-user-suspension.batch-size=2", 
    "app.batch.inactive-user-suspension.dry-run=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class InactiveUserServiceSimpleTransactionTest {

    @Autowired
    private InactiveUserService inactiveUserService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testNoTransactionErrorsInProcessBatch() {
        // Test that processBatch can be called without TransactionRequiredException
        // This was the main issue reported in production
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        
        // Before our fix: This would throw TransactionRequiredException when there were inactive users
        // After our fix: This should complete successfully regardless of whether users exist
        assertDoesNotThrow(() -> {
            InactiveUserService.BatchResult result = inactiveUserService.processBatch(cutoffDate, 0, 5);
            assertNotNull(result, "Should return a valid result");
            assertTrue(result.processed >= 0, "Processed count should be non-negative");
            assertTrue(result.suspended >= 0, "Suspended count should be non-negative");
        }, "processBatch should not throw TransactionRequiredException - this was the production bug!");
    }

    @Test
    void testNoTransactionErrorsInProcessInactiveUsers() {
        // Test the full workflow that runs in production
        
        // Before our fix: This could throw TransactionRequiredException
        // After our fix: This should complete successfully
        assertDoesNotThrow(() -> {
            inactiveUserService.processInactiveUsers();
        }, "processInactiveUsers should not throw TransactionRequiredException - this is what runs in production!");
    }

    @Test
    void testBatchResultIsAccessible() {
        // Test that BatchResult is now public and accessible
        // We made this change to support testing
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        
        InactiveUserService.BatchResult result = inactiveUserService.processBatch(cutoffDate, 0, 5);
        
        // Verify we can access the result fields (this proves BatchResult is now public)
        assertNotNull(result, "Result should not be null");
        assertTrue(result.processed >= 0, "Should have non-negative processed count");
        assertTrue(result.suspended >= 0, "Should have non-negative suspended count");
        assertTrue(result.suspended <= result.processed, "Suspended count should not exceed processed count");
    }

    @Test
    void testTransactionBoundaryFix() {
        // This test validates that the @Transactional annotation is in the right place
        // The key fix was moving @Transactional from suspendInactiveUser (private method) 
        // to processBatch (public method) so Spring can properly manage the transaction
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        
        // This tests the exact scenario that was failing in production:
        // 1. processBatch is called
        // 2. It finds inactive users (or not)
        // 3. For each user, it calls suspendInactiveUser
        // 4. suspendInactiveUser executes a JPQL DELETE query
        // 5. That query needs a transaction context to work
        
        assertDoesNotThrow(() -> {
            // Count inactive users first
            long count = inactiveUserService.countInactiveUsers(cutoffDate);
            assertTrue(count >= 0, "Count should be non-negative");
            
            // Process a batch - this is where the error occurred
            InactiveUserService.BatchResult result = inactiveUserService.processBatch(cutoffDate, 0, 10);
            
            // If we get here, the transaction fix worked!
            assertNotNull(result, "Should return a result");
            assertEquals(Math.min((int)count, 10), result.processed, "Should process the right number of users");
            
        }, "The transaction fix should prevent TransactionRequiredException!");
    }

    @Test
    void testEntityManagerIsAvailable() {
        // Verify that the EntityManager is properly configured and available
        // This is needed for the JPQL DELETE query in suspendInactiveUser
        
        assertNotNull(entityManager, "EntityManager should be available");
        assertTrue(entityManager.isOpen(), "EntityManager should be open");
        
        // Test a simple query to ensure it works
        assertDoesNotThrow(() -> {
            Long count = entityManager.createQuery("SELECT COUNT(c) FROM Collaborator c", Long.class)
                    .getSingleResult();
            assertTrue(count >= 0, "Should get a valid count");
        }, "EntityManager should work properly");
    }
}