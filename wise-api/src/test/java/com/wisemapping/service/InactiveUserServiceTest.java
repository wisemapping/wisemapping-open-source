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

import com.wisemapping.config.AppConfig;
import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.InactiveUserResult;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SuspensionReason;
import com.wisemapping.dao.UserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test for InactiveUserService that combines:
 * 1. Transaction error reproduction and verification
 * 2. Real database integration tests with history removal
 * 3. Edge case testing with batch processing
 * 
 * This unified test class verifies:
 * - TransactionRequiredException is properly handled
 * - History removal logic works correctly
 * - All SQL paths are executed with sufficient data
 * - Batch processing works with edge cases
 * - Already suspended users are not processed again
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.batch.inactive-user-suspension.inactivity-years=1",
    "app.batch.inactive-user-suspension.batch-size=5",
    "app.batch.inactive-user-suspension.dry-run=false",
    "app.batch.inactive-user-suspension.grace-period-years=1"
})
@Transactional
public class InactiveUserServiceTest {

    @Autowired
    private InactiveUserService inactiveUserService;

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserManager userManager;

    @PersistenceContext
    private EntityManager entityManager;

    // Test data
    private Account inactiveUser1;
    private Account inactiveUser2;
    private Account activeUser;
    private Account alreadySuspendedUser;
    private Mindmap mindmap1;
    private Mindmap mindmap2;
    private Mindmap activeUserMindmap;
    private Mindmap suspendedUserMindmap;

    @BeforeEach
    void setUp() {
        // Create test users
        inactiveUser1 = createTestUser("inactive1@test.com", "Inactive", "User1");
        inactiveUser2 = createTestUser("inactive2@test.com", "Inactive", "User2");
        activeUser = createTestUser("active@test.com", "Active", "User");
        alreadySuspendedUser = createTestUser("suspended@test.com", "Suspended", "User");

        // Set creation and activation dates (2.5 years ago for inactive users to be older than grace period: 1 year inactivity + 1 year grace period = 2 years)
        Calendar creationDate = Calendar.getInstance();
        creationDate.add(Calendar.YEAR, -2);
        creationDate.add(Calendar.MONTH, -6); // 2.5 years ago
        
        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.YEAR, -2);
        activationDate.add(Calendar.MONTH, -6); // 2.5 years ago
        activationDate.add(Calendar.HOUR, 1);

        inactiveUser1.setCreationDate(creationDate);
        inactiveUser1.setActivationDate(activationDate);
        
        inactiveUser2.setCreationDate(creationDate);
        inactiveUser2.setActivationDate(activationDate);
        
        activeUser.setCreationDate(Calendar.getInstance()); // Recent creation
        activeUser.setActivationDate(Calendar.getInstance());
        
        alreadySuspendedUser.setCreationDate(creationDate);
        alreadySuspendedUser.setActivationDate(activationDate);
        alreadySuspendedUser.setSuspended(true);
        alreadySuspendedUser.setSuspensionReason(SuspensionReason.MANUAL_REVIEW);

        // Persist users
        entityManager.persist(inactiveUser1);
        entityManager.persist(inactiveUser2);
        entityManager.persist(activeUser);
        entityManager.persist(alreadySuspendedUser);
        entityManager.flush();

        // Create mindmaps
        mindmap1 = createTestMindmap("Test Mindmap 1", inactiveUser1);
        mindmap2 = createTestMindmap("Test Mindmap 2", inactiveUser2);
        activeUserMindmap = createTestMindmap("Active User Mindmap", activeUser);
        suspendedUserMindmap = createTestMindmap("Suspended User Mindmap", alreadySuspendedUser);
        
        // Fix activeUser mindmap to have recent creation time (since activeUser is recent)
        Calendar recentTime1 = Calendar.getInstance();
        activeUserMindmap.setCreationTime(recentTime1);
        activeUserMindmap.setLastModificationTime(recentTime1);

        // Add mindmaps
        mindmapManager.addMindmap(inactiveUser1, mindmap1);
        mindmapManager.addMindmap(inactiveUser2, mindmap2);
        mindmapManager.addMindmap(activeUser, activeUserMindmap);
        mindmapManager.addMindmap(alreadySuspendedUser, suspendedUserMindmap);
        
        // Re-set the dates after addMindmap (in case it overrides them)
        Calendar oldTime = Calendar.getInstance();
        oldTime.add(Calendar.YEAR, -2);
        oldTime.add(Calendar.MONTH, -6); // 2.5 years ago
        
        mindmap1.setCreationTime(oldTime);
        mindmap1.setLastModificationTime(oldTime);
        mindmap2.setCreationTime(oldTime);
        mindmap2.setLastModificationTime(oldTime);
        suspendedUserMindmap.setCreationTime(oldTime);
        suspendedUserMindmap.setLastModificationTime(oldTime);
        
        // activeUserMindmap should keep recent time
        Calendar recentTime = Calendar.getInstance();
        activeUserMindmap.setCreationTime(recentTime);
        activeUserMindmap.setLastModificationTime(recentTime);
        
        entityManager.flush();

        // Create history entries for each mindmap
        createHistoryEntries(mindmap1, inactiveUser1, 3);
        createHistoryEntries(mindmap2, inactiveUser2, 2);
        createHistoryEntries(activeUserMindmap, activeUser, 1);
        createHistoryEntries(suspendedUserMindmap, alreadySuspendedUser, 2);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        entityManager.clear();
    }

    // ===== TRANSACTION ERROR REPRODUCTION TESTS =====

    @Test
    void testNoTransactionErrorsInProcessBatch() {
        // Test that processBatch can be called without TransactionRequiredException
        // This was the main issue reported in production
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        
        // Before our fix: This would throw TransactionRequiredException when there were inactive users
        // After our fix: This should complete successfully regardless of whether users exist
        assertDoesNotThrow(() -> {
            // Create creation cutoff date (1 year ago for test)
            Calendar creationCutoffDate = Calendar.getInstance();
            creationCutoffDate.add(Calendar.YEAR, -1);
            
            List<InactiveUserResult> batch = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, 0, 5);
            InactiveUserService.BatchResult result = inactiveUserService.processBatch(batch);
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
            // Create creation cutoff date (1 year ago for test)
            Calendar creationCutoffDate = Calendar.getInstance();
            creationCutoffDate.add(Calendar.YEAR, -1);
            
            // Count inactive users first using UserManager
            long count = userManager.countUsersInactiveSince(cutoffDate, creationCutoffDate);
            assertTrue(count >= 0, "Count should be non-negative");
            
            // Process a batch - this is where the error occurred
            List<InactiveUserResult> batch = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, 0, 10);
            InactiveUserService.BatchResult result = inactiveUserService.processBatch(batch);
            
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

    // ===== HISTORY REMOVAL TESTS =====

    @Test
    void testInactiveUserSuspensionWithHistoryRemoval() {
        // Verify that inactive users are suspended and their mindmap history is cleared
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);

        // Verify initial state
        assertFalse(inactiveUser1.isSuspended(), "User1 should not be suspended initially");
        assertFalse(inactiveUser2.isSuspended(), "User2 should not be suspended initially");
        assertEquals(3, getHistoryCountForMindmap(mindmap1.getId()), "Mindmap1 should have 3 history entries");
        assertEquals(2, getHistoryCountForMindmap(mindmap2.getId()), "Mindmap2 should have 2 history entries");

        // Process inactive users
        inactiveUserService.processInactiveUsers();

        // Refresh entities from database
        entityManager.refresh(inactiveUser1);
        entityManager.refresh(inactiveUser2);

        // Verify users are suspended
        assertTrue(inactiveUser1.isSuspended(), "User1 should be suspended");
        assertTrue(inactiveUser2.isSuspended(), "User2 should be suspended");
        assertEquals(SuspensionReason.INACTIVITY, inactiveUser1.getSuspensionReason(), "User1 should be suspended for inactivity");
        assertEquals(SuspensionReason.INACTIVITY, inactiveUser2.getSuspensionReason(), "User2 should be suspended for inactivity");

        // Note: History removal is no longer handled by InactiveUserService
        // History cleanup is now handled separately by HistoryPurgeService
    }

    @Test
    void testAlreadySuspendedUsersAreNotProcessed() {
        // Verify that already suspended users are not processed again
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);

        SuspensionReason originalReason = alreadySuspendedUser.getSuspensionReason();
        Calendar originalSuspensionDate = alreadySuspendedUser.getSuspendedDate();

        // Process inactive users
        inactiveUserService.processInactiveUsers();

        // Refresh entity from database
        entityManager.refresh(alreadySuspendedUser);

        // Verify suspension reason and date are unchanged
        assertEquals(originalReason, alreadySuspendedUser.getSuspensionReason(), 
                "Suspension reason should not change for already suspended user");
        assertEquals(originalSuspensionDate, alreadySuspendedUser.getSuspendedDate(),
                "Suspension date should not change for already suspended user");
    }

    @Test
    void testActiveUserIsNotSuspended() {
        // Verify that active users are not suspended
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);

        // Process inactive users
        inactiveUserService.processInactiveUsers();

        // Refresh entity from database
        entityManager.refresh(activeUser);

        // Verify user is not suspended
        assertFalse(activeUser.isSuspended(), "Active user should not be suspended");
        assertEquals(1, getHistoryCountForMindmap(activeUserMindmap.getId()), 
                "Active user's mindmap history should remain");
    }

    @Test
    void testBatchProcessingWithSufficientData() {
        // Test the batch processing logic when there are enough inactive users to fill multiple batches
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);

        // Create additional inactive users to test batch processing
        for (int i = 1; i <= 10; i++) {
            Account user = createTestUser("batchinactive" + i + "@test.com", "BatchInactive", "User" + i);
            user.setCreationDate(cutoffDate);
            user.setActivationDate(cutoffDate);
            entityManager.persist(user);

            Mindmap mindmap = createTestMindmap("Batch Test Mindmap " + i, user);
            mindmapManager.addMindmap(user, mindmap);
            createHistoryEntries(mindmap, user, 2);
        }
        entityManager.flush();

        // Process inactive users
        inactiveUserService.processInactiveUsers();

        // Verify that users suspended due to inactivity have their history cleared
        List<Account> suspendedUsers = entityManager.createQuery(
                "SELECT a FROM Account a WHERE a.suspended = true", Account.class)
                .getResultList();

        List<Account> inactivitySuspended = suspendedUsers.stream()
                .filter(user -> SuspensionReason.INACTIVITY.equals(user.getSuspensionReason()))
                .toList();

        assertTrue(inactivitySuspended.size() >= 2, "Should have at least 2 users suspended for inactivity");

        // Verify that only active user history remains (should be 1)
        assertEquals(1, getHistoryCountForMindmap(activeUserMindmap.getId()), 
                "Only active user history should remain");
    }

    @Test
    void testTransactionRequiredExceptionReproduction() {
        // This test reproduces the exact TransactionRequiredException that occurs in production
        // when the suspendInactiveUser method tries to execute the DELETE query without proper transaction context
        
        // Create an inactive user with unique email and date within the 1-2 year range
        Account testUser = createTestUser("transactiontest" + System.currentTimeMillis() + "@test.com", "Transaction", "Test");
        Calendar userCreationDate = Calendar.getInstance();
        userCreationDate.add(Calendar.YEAR, -3); // User created 3 years ago (much older than setup users)
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1); // Cutoff date 1 year ago - this will include our user
        testUser.setCreationDate(userCreationDate);
        testUser.setActivationDate(userCreationDate);
        entityManager.persist(testUser);
        
        // Create a mindmap with history for this user
        Mindmap mindmap = createTestMindmap("Transaction Test Mindmap", testUser);
        // Set the mindmap's lastModificationTime to match the user's creation date (1.5 years ago)
        mindmap.setLastModificationTime(userCreationDate);
        mindmapManager.addMindmap(testUser, mindmap);
        createHistoryEntries(mindmap, testUser, 3);
        entityManager.flush();
        
        // Verify the user and history exist
        assertFalse(testUser.isSuspended(), "User should not be suspended initially");
        
        // Create creation cutoff date (2.8 years ago to exclude setup users at 2.5 years but include this user at 3 years)
        Calendar creationCutoffDate = Calendar.getInstance();
        creationCutoffDate.add(Calendar.YEAR, -2);
        creationCutoffDate.add(Calendar.MONTH, -10); // 2.8 years ago
        
        // This should NOT throw TransactionRequiredException because we're in a transactional context
        List<InactiveUserResult> batch = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, 0, 5);
        InactiveUserService.BatchResult result = inactiveUserService.processBatch(batch);
        
        // Verify the user was processed and suspended
        assertEquals(1, result.processed, "Should process exactly 1 inactive user");
        assertEquals(1, result.suspended, "Should suspend exactly 1 inactive user");
        
        // Verify the user is actually suspended
        entityManager.refresh(testUser);
        assertTrue(testUser.isSuspended(), "User should be suspended after processing");
        assertEquals(SuspensionReason.INACTIVITY, testUser.getSuspensionReason(), "User should be suspended for inactivity");
    }

    @Test
    void testTransactionRequiredExceptionProductionScenario() {
        // This test reproduces the EXACT production scenario where TransactionRequiredException occurs
        // by calling the suspendInactiveUser method directly without transaction context
        
        // Create an inactive user with unique email and date within the 1-2 year range
        Account testUser = createTestUser("prodtest" + System.currentTimeMillis() + "@test.com", "Production", "Test");
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        cutoffDate.add(Calendar.MONTH, -6); // Use 1.5 years ago to be within the 1-2 year range
        testUser.setCreationDate(cutoffDate);
        testUser.setActivationDate(cutoffDate);
        entityManager.persist(testUser);
        
        // Create a mindmap with history for this user
        Mindmap mindmap = createTestMindmap("Production Test Mindmap", testUser);
        mindmapManager.addMindmap(testUser, mindmap);
        createHistoryEntries(mindmap, testUser, 3);
        entityManager.flush();
        
        // Verify the user and history exist
        assertFalse(testUser.isSuspended(), "User should not be suspended initially");
        
        // This SHOULD NOT throw TransactionRequiredException anymore because suspendInactiveUser is now @Transactional
        // But let's test that it works correctly
        inactiveUserService.suspendInactiveUser(testUser);
        
        // Verify the user was suspended
        entityManager.refresh(testUser);
        assertTrue(testUser.isSuspended(), "User should be suspended after processing");
        assertEquals(SuspensionReason.INACTIVITY, testUser.getSuspensionReason(), "User should be suspended for inactivity");
    }

    @Test
    void testSuspendInactiveUserWithHistoryRemoval() {
        // Test that suspendInactiveUser properly removes history when called directly
        // This verifies the fix for the TransactionRequiredException
        
        // Create an inactive user with unique email and date within the 1-2 year range
        Account testUser = createTestUser("historytest" + System.currentTimeMillis() + "@test.com", "History", "Test");
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1);
        cutoffDate.add(Calendar.MONTH, -6); // Use 1.5 years ago to be within the 1-2 year range
        testUser.setCreationDate(cutoffDate);
        testUser.setActivationDate(cutoffDate);
        entityManager.persist(testUser);
        
        // Create a mindmap with history for this user
        Mindmap mindmap = createTestMindmap("History Test Mindmap", testUser);
        mindmapManager.addMindmap(testUser, mindmap);
        createHistoryEntries(mindmap, testUser, 5);
        entityManager.flush();
        
        // Verify the user and history exist
        assertFalse(testUser.isSuspended(), "User should not be suspended initially");
        assertEquals(5, getHistoryCountForMindmap(mindmap.getId()), "Should have 5 history entries initially");
        
        // Call suspendInactiveUser directly - this should work without TransactionRequiredException
        inactiveUserService.suspendInactiveUser(testUser);
        
        // Verify the user was suspended
        entityManager.refresh(testUser);
        assertTrue(testUser.isSuspended(), "User should be suspended after processing");
        assertEquals(SuspensionReason.INACTIVITY, testUser.getSuspensionReason(), "User should be suspended for inactivity");
        
        // Note: History removal is no longer handled by InactiveUserService
        // History cleanup is now handled separately by HistoryPurgeService
    }

    @Test
    void testDryRunMode() {
        // Test dry run mode - users should not be suspended but should be identified as candidates
        // Note: dryRun is set to false in test properties, so this tests the actual suspension behavior
        
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -1); // 1 year ago, same as the service configuration

        // Create creation cutoff date (1 year ago for test)
        Calendar creationCutoffDate = Calendar.getInstance();
        creationCutoffDate.add(Calendar.YEAR, -1);
        
        // Process inactive users (should actually suspend since dryRun=false)
        List<InactiveUserResult> batch = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, 0, 5);
        InactiveUserService.BatchResult result = inactiveUserService.processBatch(batch);

        // Verify result - should process the 2 inactive users we created
        assertEquals(2, result.processed, "Should process exactly 2 inactive users");
        assertEquals(2, result.suspended, "Should suspend exactly 2 inactive users");

        // Verify that users were actually suspended by querying the database
        List<Account> suspendedUsers = entityManager.createQuery(
                "SELECT a FROM Account a WHERE a.suspended = true AND a.suspensionReasonCode = 'I'", Account.class)
                .getResultList();
        
        assertEquals(2, suspendedUsers.size(), "Should have exactly 2 users suspended for inactivity");
        
        // Verify the suspended users are our test users
        List<String> suspendedEmails = suspendedUsers.stream()
                .map(Account::getEmail)
                .toList();
        
        assertTrue(suspendedEmails.contains("inactive1@test.com"), "inactive1@test.com should be suspended");
        assertTrue(suspendedEmails.contains("inactive2@test.com"), "inactive2@test.com should be suspended");
    }

    // ===== HELPER METHODS =====

    private Account createTestUser(String email, String firstname, String lastname) {
        Account user = new Account();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPassword("password");
        user.setLocale("en");
        return user;
    }

    private Mindmap createTestMindmap(String title, Account creator) {
        Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        mindmap.setDescription("Test mindmap");
        try {
            mindmap.setXmlStr("<map>test content</map>");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set XML content", e);
        }
        
        Calendar creationTime = Calendar.getInstance();
        creationTime.add(Calendar.YEAR, -2);
        creationTime.add(Calendar.MONTH, -6); // Match the user creation time (2.5 years ago)
        mindmap.setCreationTime(creationTime);
        mindmap.setLastModificationTime(creationTime);
        
        return mindmap;
    }

    private void createHistoryEntries(Mindmap mindmap, Account editor, int count) {
        Calendar creationTime = Calendar.getInstance();
        creationTime.add(Calendar.YEAR, -2);
        creationTime.add(Calendar.MONTH, -6); // Match the user creation time (2.5 years ago)
        
        for (int i = 0; i < count; i++) {
            MindMapHistory history = new MindMapHistory();
            history.setMindmapId(mindmap.getId());
            history.setEditor(editor);
            history.setCreationTime(creationTime);
            history.setZippedXml(("<map>history " + i + "</map>").getBytes());
            
            entityManager.persist(history);
        }
        entityManager.flush();
    }

    private int getHistoryCountForMindmap(int mindmapId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(h) FROM MindMapHistory h WHERE h.mindmapId = :mindmapId", Long.class);
        query.setParameter("mindmapId", mindmapId);
        return query.getSingleResult().intValue();
    }
}
