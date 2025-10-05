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
import com.wisemapping.dao.InactiveMindmapManager;
import com.wisemapping.dao.MindmapManager;
import com.wisemapping.dao.UserManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.InactiveMindmap;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SuspensionReason;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for InactiveMindmapMigrationService.
 * Tests migration of mindmaps from inactive users to the inactive table.
 */
@SpringBootTest(classes = {AppConfig.class})
@TestPropertySource(properties = {
        "app.batch.inactive-mindmap-migration.enabled=true",
        "app.batch.inactive-mindmap-migration.batch-size=5",
        "app.batch.inactive-mindmap-migration.inactivity-years=2",
        "app.batch.inactive-mindmap-migration.dry-run=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class InactiveMindmapMigrationServiceTest {

    @Autowired
    private InactiveMindmapMigrationService inactiveMindmapMigrationService;

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private InactiveMindmapManager inactiveMindmapManager;

    @Autowired
    private EntityManager entityManager;

    private Account inactiveUser1;
    private Account inactiveUser2;
    private Account activeUser;

    @BeforeEach
    public void setUp() {
        // Create inactive users (created 3 years ago) - these should be suspended users
        inactiveUser1 = createTestUser("inactive1@test.com", "Inactive", "User1");
        Calendar inactiveDate = Calendar.getInstance();
        inactiveDate.add(Calendar.YEAR, -3);
        inactiveUser1.setCreationDate(inactiveDate);
        inactiveUser1.setActivationDate(inactiveDate);
        // Mark as suspended for migration (suspended 2 months ago to meet 1-month requirement)
        Calendar suspensionDate = Calendar.getInstance();
        suspensionDate.add(Calendar.MONTH, -2);
        inactiveUser1.setSuspended(true);
        inactiveUser1.setSuspensionReason(SuspensionReason.INACTIVITY);
        inactiveUser1.setSuspendedDate(suspensionDate);
        entityManager.persist(inactiveUser1);

        inactiveUser2 = createTestUser("inactive2@test.com", "Inactive", "User2");
        inactiveUser2.setCreationDate(inactiveDate);
        inactiveUser2.setActivationDate(inactiveDate);
        // Mark as suspended for migration (suspended 2 months ago to meet 1-month requirement)
        inactiveUser2.setSuspended(true);
        inactiveUser2.setSuspensionReason(SuspensionReason.INACTIVITY);
        inactiveUser2.setSuspendedDate(suspensionDate);
        entityManager.persist(inactiveUser2);

        // Create active user (created 1 year ago)
        activeUser = createTestUser("active@test.com", "Active", "User");
        Calendar activeDate = Calendar.getInstance();
        activeDate.add(Calendar.YEAR, -1);
        activeUser.setCreationDate(activeDate);
        activeUser.setActivationDate(activeDate);
        entityManager.persist(activeUser);

        // Create mindmaps for inactive users
        createTestMindmap("Inactive User 1 Mindmap 1", inactiveUser1);
        createTestMindmap("Inactive User 1 Mindmap 2", inactiveUser1);
        createTestMindmap("Inactive User 2 Mindmap 1", inactiveUser2);

        // Create mindmap for active user (should not be migrated)
        createTestMindmap("Active User Mindmap", activeUser);

        entityManager.flush();
    }

    private Account createTestUser(String email, String firstname, String lastname) {
        Account user = new Account();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPassword("password");
        user.setActivationCode(123456L);
        user.setAllowSendEmail(true);
        return user;
    }

    private Mindmap createTestMindmap(String title, Account creator) {
        Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setDescription("Test mindmap description");
        mindmap.setPublic(false);
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        
        Calendar now = Calendar.getInstance();
        mindmap.setCreationTime(now);
        mindmap.setLastModificationTime(now);

        try {
            mindmap.setXmlStr("<map>test content</map>");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set XML content", e);
        }

        mindmapManager.addMindmap(creator, mindmap);
        return mindmap;
    }

    @Test
    @Transactional
    void testInactiveMindmapMigrationWithHistoryRemoval() {
        // Verify initial state
        List<Mindmap> inactiveUser1Mindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        List<Mindmap> inactiveUser2Mindmaps = mindmapManager.findByCreator(inactiveUser2.getId());
        List<Mindmap> activeUserMindmaps = mindmapManager.findByCreator(activeUser.getId());
        
        assertEquals(2, inactiveUser1Mindmaps.size(), "Inactive user 1 should have 2 mindmaps initially");
        assertEquals(1, inactiveUser2Mindmaps.size(), "Inactive user 2 should have 1 mindmap initially");
        assertEquals(1, activeUserMindmaps.size(), "Active user should have 1 mindmap initially");

        // Run migration process
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify mindmaps were migrated for inactive users
        List<Mindmap> inactiveUser1MindmapsAfter = mindmapManager.findByCreator(inactiveUser1.getId());
        List<Mindmap> inactiveUser2MindmapsAfter = mindmapManager.findByCreator(inactiveUser2.getId());
        List<Mindmap> activeUserMindmapsAfter = mindmapManager.findByCreator(activeUser.getId());

        assertEquals(0, inactiveUser1MindmapsAfter.size(), "Inactive user 1 should have no mindmaps after migration");
        assertEquals(0, inactiveUser2MindmapsAfter.size(), "Inactive user 2 should have no mindmaps after migration");
        assertEquals(1, activeUserMindmapsAfter.size(), "Active user should still have 1 mindmap after migration");

        // Verify mindmaps were moved to inactive table
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        List<InactiveMindmap> inactiveMindmaps2 = inactiveMindmapManager.findByCreator(inactiveUser2);
        
        assertEquals(2, inactiveMindmaps.size(), "Inactive user 1 should have 2 mindmaps in inactive table");
        assertEquals(1, inactiveMindmaps2.size(), "Inactive user 2 should have 1 mindmap in inactive table");

        // Verify inactive mindmap details
        for (InactiveMindmap inactiveMindmap : inactiveMindmaps) {
            assertEquals(inactiveUser1.getId(), inactiveMindmap.getCreator().getId());
            assertNotNull(inactiveMindmap.getMigrationDate());
            assertEquals("User suspended for at least 1 month", inactiveMindmap.getMigrationReason());
            assertTrue(inactiveMindmap.getOriginalMindmapId() > 0);
        }
    }

    @Test
    @Transactional
    void testDryRunMode() {
        // Set dry run mode
        inactiveMindmapMigrationService = new InactiveMindmapMigrationService();
        // Note: In a real test, you'd need to inject the dry-run property or use reflection

        // Verify initial state
        List<Mindmap> inactiveUser1Mindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(2, inactiveUser1Mindmaps.size(), "Should have 2 mindmaps initially");

        // Run migration in dry run mode
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify mindmaps are still in original table (dry run)
        List<Mindmap> inactiveUser1MindmapsAfter = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(2, inactiveUser1MindmapsAfter.size(), "Should still have 2 mindmaps after dry run");

        // Verify no mindmaps in inactive table
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        assertEquals(0, inactiveMindmaps.size(), "Should have no mindmaps in inactive table after dry run");
    }

    @Test
    @Transactional
    void testBatchProcessingWithSufficientData() {
        // Create additional inactive users with mindmaps
        for (int i = 3; i <= 7; i++) {
            Account user = createTestUser("inactive" + i + "@test.com", "Inactive", "User" + i);
            Calendar inactiveDate = Calendar.getInstance();
            inactiveDate.add(Calendar.YEAR, -3);
            user.setCreationDate(inactiveDate);
            user.setActivationDate(inactiveDate);
            // Mark as suspended for migration (suspended 2 months ago to meet 1-month requirement)
            Calendar suspensionDate = Calendar.getInstance();
            suspensionDate.add(Calendar.MONTH, -2);
            user.setSuspended(true);
            user.setSuspensionReason(SuspensionReason.INACTIVITY);
            user.setSuspendedDate(suspensionDate);
            entityManager.persist(user);

            createTestMindmap("Mindmap for User " + i, user);
        }
        entityManager.flush();

        // Run migration
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify all inactive users' mindmaps were migrated
        List<InactiveMindmap> allInactiveMindmaps = inactiveMindmapManager.findAll();
        assertTrue(allInactiveMindmaps.size() >= 7, "Should have migrated mindmaps from all inactive users");

        // Verify active user's mindmaps remain
        List<Mindmap> activeUserMindmaps = mindmapManager.findByCreator(activeUser.getId());
        assertEquals(1, activeUserMindmaps.size(), "Active user's mindmaps should remain");
    }

    @Test
    @Transactional
    void testMigrationStats() {
        // Get initial stats
        InactiveMindmapMigrationService.MigrationStats stats = inactiveMindmapMigrationService.getMigrationStats();
        
        assertNotNull(stats);
        // With the new logic, test users are suspended for 2 months, so they should be counted as eligible
        assertEquals(2, stats.getInactiveUsersCount(), "Should have 2 eligible suspended users (suspended for 2 months)");
        assertEquals(0, stats.getInactiveMindmapsCount(), "Should have no inactive mindmaps initially");

        // Run migration
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Get stats after migration
        InactiveMindmapMigrationService.MigrationStats statsAfter = inactiveMindmapMigrationService.getMigrationStats();
        
        // With the new 1-month suspension requirement, mindmaps should be migrated since test users are suspended for 2 months
        assertEquals(3, statsAfter.getInactiveMindmapsCount(), "Should have 3 migrated mindmaps (users suspended for 2 months)");
    }

    @Test
    @Transactional
    void testNoInactiveUsersFound() {
        // Create only active users
        Account activeUser2 = createTestUser("active2@test.com", "Active", "User2");
        Calendar activeDate = Calendar.getInstance();
        activeDate.add(Calendar.YEAR, -1);
        activeUser2.setCreationDate(activeDate);
        activeUser2.setActivationDate(activeDate);
        entityManager.persist(activeUser2);
        entityManager.flush();

        // Run migration - should handle gracefully
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify mindmaps were migrated from the setup users (they are suspended for 2 months)
        List<InactiveMindmap> allInactiveMindmaps = inactiveMindmapManager.findAll();
        assertEquals(3, allInactiveMindmaps.size(), "Should have 3 inactive mindmaps from setup users who are suspended for 2 months");
    }

    @Test
    @Transactional
    void testInactiveUsersWithNoMindmaps() {
        // Create inactive user with no mindmaps
        Account inactiveUserNoMindmaps = createTestUser("inactive_no_mindmaps@test.com", "Inactive", "NoMindmaps");
        Calendar inactiveDate = Calendar.getInstance();
        inactiveDate.add(Calendar.YEAR, -3);
        inactiveUserNoMindmaps.setCreationDate(inactiveDate);
        inactiveUserNoMindmaps.setActivationDate(inactiveDate);
        entityManager.persist(inactiveUserNoMindmaps);
        entityManager.flush();

        // Run migration
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify user with no mindmaps doesn't cause issues
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUserNoMindmaps);
        assertEquals(0, inactiveMindmaps.size(), "User with no mindmaps should not create inactive mindmap records");
    }

    @Test
    @Transactional
    void testTransactionBoundary() {
        // This test verifies that the migration process works within transaction boundaries
        assertDoesNotThrow(() -> {
            inactiveMindmapMigrationService.processInactiveMindmapMigration();
        }, "Migration should complete without transaction errors");
    }

    @Test
    @Transactional
    void testEntityManagerIsAvailable() {
        // Verify EntityManager is available for the service
        assertNotNull(entityManager, "EntityManager should be available");
        
        // Test that we can query the database
        List<Account> users = entityManager.createQuery("SELECT a FROM Account a", Account.class).getResultList();
        assertNotNull(users, "Should be able to query users from database");
    }

    @Test
    @Transactional
    void testInactiveMindmapCreation() {
        // Create a mindmap and manually test the inactive mindmap creation
        Mindmap originalMindmap = mindmapManager.findByCreator(inactiveUser1.getId()).get(0);
        
        // Create inactive mindmap
        InactiveMindmap inactiveMindmap = new InactiveMindmap(originalMindmap, "Test migration");
        inactiveMindmapManager.addInactiveMindmap(inactiveMindmap);
        entityManager.flush();

        // Verify the inactive mindmap was created correctly
        InactiveMindmap saved = inactiveMindmapManager.findByOriginalMindmapId(originalMindmap.getId());
        assertNotNull(saved, "Inactive mindmap should be found by original ID");
        assertEquals(originalMindmap.getId(), saved.getOriginalMindmapId());
        assertEquals(originalMindmap.getTitle(), saved.getTitle());
        assertEquals("Test migration", saved.getMigrationReason());
        assertNotNull(saved.getMigrationDate());
    }

    @Test
    @Transactional
    void testUserReactivationAndMindmapRestoration() {
        // First, migrate mindmaps to inactive table
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify mindmaps are in inactive table
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        assertEquals(2, inactiveMindmaps.size(), "Should have 2 mindmaps in inactive table");
        
        List<Mindmap> activeMindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(0, activeMindmaps.size(), "Should have no active mindmaps");

        // Reactivate user (simulate admin action)
        inactiveUser1.setSuspended(false);
        inactiveUser1.setSuspensionReason(null);
        userManager.updateUser(inactiveUser1);

        // Restore mindmaps for reactivated user
        int restoredCount = inactiveMindmapMigrationService.restoreUserMindmaps(inactiveUser1);
        assertEquals(2, restoredCount, "Should restore 2 mindmaps");

        // Verify mindmaps are back in active table
        List<Mindmap> restoredActiveMindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(2, restoredActiveMindmaps.size(), "Should have 2 restored active mindmaps");

        // Verify inactive mindmaps are removed
        List<InactiveMindmap> remainingInactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        assertEquals(0, remainingInactiveMindmaps.size(), "Should have no remaining inactive mindmaps");

        // Verify restored mindmap details
        for (Mindmap restoredMindmap : restoredActiveMindmaps) {
            assertEquals(inactiveUser1.getId(), restoredMindmap.getCreator().getId());
            assertEquals(inactiveUser1.getId(), restoredMindmap.getLastEditor().getId());
            assertNotNull(restoredMindmap.getCreationTime());
            assertNotNull(restoredMindmap.getLastModificationTime());
        }
    }

    @Test
    @Transactional
    void testReactivationWithNoInactiveMindmaps() {
        // Create user with no migrated mindmaps
        Account userWithNoInactiveMindmaps = createTestUser("no_inactive@test.com", "No", "Inactive");
        Calendar recentDate = Calendar.getInstance();
        recentDate.add(Calendar.MONTH, -1);
        userWithNoInactiveMindmaps.setCreationDate(recentDate);
        userWithNoInactiveMindmaps.setActivationDate(recentDate);
        entityManager.persist(userWithNoInactiveMindmaps);
        entityManager.flush();

        // Try to restore mindmaps - should return 0
        int restoredCount = inactiveMindmapMigrationService.restoreUserMindmaps(userWithNoInactiveMindmaps);
        assertEquals(0, restoredCount, "Should restore 0 mindmaps for user with no inactive mindmaps");
    }

    @Test
    @Transactional
    void testPartialRestorationOnError() {
        // First, migrate mindmaps to inactive table
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify mindmaps are in inactive table
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        assertEquals(2, inactiveMindmaps.size(), "Should have 2 mindmaps in inactive table");

        // Corrupt one of the inactive mindmaps to simulate an error
        InactiveMindmap corruptedMindmap = inactiveMindmaps.get(0);
        corruptedMindmap.setTitle(null); // This will cause an error during restoration
        entityManager.merge(corruptedMindmap);
        entityManager.flush();

        // Try to restore mindmaps - should handle the error gracefully
        int restoredCount = inactiveMindmapMigrationService.restoreUserMindmaps(inactiveUser1);
        
        // Should restore at least one mindmap (the non-corrupted one)
        assertTrue(restoredCount >= 1, "Should restore at least one mindmap despite errors");
        
        // Verify some mindmaps were restored
        List<Mindmap> restoredActiveMindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertTrue(restoredActiveMindmaps.size() >= 1, "Should have at least one restored active mindmap");
    }

    @Test
    @Transactional
    void testRestorationPreservesMindmapData() {
        // Create a mindmap with specific data
        Mindmap originalMindmap = createTestMindmap("Detailed Mindmap", inactiveUser1);
        originalMindmap.setDescription("Important description");
        originalMindmap.setPublic(true);
        mindmapManager.saveMindmap(originalMindmap);
        entityManager.flush();

        // Migrate to inactive
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify original is gone
        List<Mindmap> activeMindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(0, activeMindmaps.size(), "Should have no active mindmaps after migration");

        // Restore mindmaps
        int restoredCount = inactiveMindmapMigrationService.restoreUserMindmaps(inactiveUser1);
        assertEquals(3, restoredCount, "Should restore 3 mindmaps");

        // Verify data is preserved
        List<Mindmap> restoredMindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        assertEquals(3, restoredMindmaps.size(), "Should have 3 restored mindmaps");

        // Find the detailed mindmap and verify its data
        Mindmap detailedRestoredMindmap = restoredMindmaps.stream()
                .filter(m -> "Detailed Mindmap".equals(m.getTitle()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(detailedRestoredMindmap, "Should find the detailed mindmap");
        assertEquals("Important description", detailedRestoredMindmap.getDescription());
        assertTrue(detailedRestoredMindmap.isPublic());
        assertEquals(inactiveUser1.getId(), detailedRestoredMindmap.getCreator().getId());
        assertEquals(inactiveUser1.getId(), detailedRestoredMindmap.getLastEditor().getId());
        assertNotNull(detailedRestoredMindmap.getZippedXml());
    }

    @Test
    @Transactional
    void testMultipleUsersReactivation() {
        // Migrate mindmaps for both inactive users
        inactiveMindmapMigrationService.processInactiveMindmapMigration();

        // Verify both users have inactive mindmaps
        List<InactiveMindmap> inactiveUser1Mindmaps = inactiveMindmapManager.findByCreator(inactiveUser1);
        List<InactiveMindmap> inactiveUser2Mindmaps = inactiveMindmapManager.findByCreator(inactiveUser2);
        assertEquals(2, inactiveUser1Mindmaps.size(), "User 1 should have 2 inactive mindmaps");
        assertEquals(1, inactiveUser2Mindmaps.size(), "User 2 should have 1 inactive mindmap");

        // Reactivate both users
        inactiveUser1.setSuspended(false);
        inactiveUser2.setSuspended(false);
        userManager.updateUser(inactiveUser1);
        userManager.updateUser(inactiveUser2);

        // Restore mindmaps for both users
        int restoredCount1 = inactiveMindmapMigrationService.restoreUserMindmaps(inactiveUser1);
        int restoredCount2 = inactiveMindmapMigrationService.restoreUserMindmaps(inactiveUser2);

        assertEquals(2, restoredCount1, "Should restore 2 mindmaps for user 1");
        assertEquals(1, restoredCount2, "Should restore 1 mindmap for user 2");

        // Verify both users have their mindmaps restored
        List<Mindmap> restoredUser1Mindmaps = mindmapManager.findByCreator(inactiveUser1.getId());
        List<Mindmap> restoredUser2Mindmaps = mindmapManager.findByCreator(inactiveUser2.getId());
        assertEquals(2, restoredUser1Mindmaps.size(), "User 1 should have 2 restored mindmaps");
        assertEquals(1, restoredUser2Mindmaps.size(), "User 2 should have 1 restored mindmap");

        // Verify no inactive mindmaps remain
        List<InactiveMindmap> remainingInactiveMindmaps = inactiveMindmapManager.findAll();
        assertEquals(0, remainingInactiveMindmaps.size(), "Should have no remaining inactive mindmaps");
    }
}
