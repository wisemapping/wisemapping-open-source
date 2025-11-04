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

package com.wisemapping.dao;

import com.wisemapping.config.AppConfig;
import com.wisemapping.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to reproduce the "detached entity passed to persist" error
 * when deleting a mindmap that has labels associated with it.
 * 
 * This test reproduces the issue where:
 * 1. A mindmap has labels associated (ManyToMany with CascadeType.PERSIST)
 * 2. When deleting the mindmap, Hibernate tries to persist detached label entities
 * 3. This causes "detached entity passed to persist: com.wisemapping.model.Mindmap" error
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional
public class MindmapManagerRemoveMindmapTest {

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private EntityManager entityManager;

    private Account testUser;
    private Mindmap testMindmap;
    private MindmapLabel testLabel;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = createTestUser("test@wisemapping.com", "Test", "User");
        entityManager.persist(testUser);
        entityManager.flush();

        // Create test label
        testLabel = new MindmapLabel();
        testLabel.setTitle("Test Label");
        testLabel.setColor("#FF0000");
        testLabel.setCreator(testUser);
        entityManager.persist(testLabel);
        entityManager.flush();

        // Create test mindmap
        testMindmap = createTestMindmap("Test Mindmap", testUser);
        mindmapManager.addMindmap(testUser, testMindmap);
        entityManager.flush();

        // Associate label with mindmap
        testMindmap.addLabel(testLabel);
        mindmapManager.updateMindmap(testMindmap, false);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to simulate detached state
    }

    @Test
    public void testRemoveMindmapWithMultipleLabels_ShouldNotThrowDetachedEntityError() {
        // Create additional labels
        final MindmapLabel label2 = new MindmapLabel();
        label2.setTitle("Test Label 2");
        label2.setColor("#00FF00");
        label2.setCreator(testUser);
        entityManager.persist(label2);
        entityManager.flush();

        final MindmapLabel label3 = new MindmapLabel();
        label3.setTitle("Test Label 3");
        label3.setColor("#0000FF");
        label3.setCreator(testUser);
        entityManager.persist(label3);
        entityManager.flush();

        // Reload mindmap and add multiple labels
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        loadedMindmap.addLabel(label2);
        loadedMindmap.addLabel(label3);
        mindmapManager.updateMindmap(loadedMindmap, false);
        entityManager.flush();
        
        // Verify mindmap has multiple labels
        final Mindmap mindmapWithLabels = entityManager.find(Mindmap.class, testMindmap.getId());
        assertTrue(mindmapWithLabels.getLabels().size() >= 3, "Mindmap should have at least 3 labels");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(mindmapWithLabels);
            entityManager.flush();
        }, "Deleting mindmap with multiple labels should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithCollaborations_ShouldNotThrowDetachedEntityError() {
        // Create collaborator user
        final Account collaborator = createTestUser("collaborator@wisemapping.com", "Collaborator", "User");
        entityManager.persist(collaborator);
        entityManager.flush();

        // Reload mindmap and add collaboration
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Create collaboration
        mindmapManager.findOrCreateCollaboration(
            loadedMindmap, 
            collaborator, 
            CollaborationRole.EDITOR
        );
        entityManager.flush();
        
        // Verify mindmap has collaboration
        final Mindmap mindmapWithCollab = entityManager.find(Mindmap.class, testMindmap.getId());
        assertFalse(mindmapWithCollab.getCollaborations().isEmpty(), "Mindmap should have collaborations");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(mindmapWithCollab);
            entityManager.flush();
        }, "Deleting mindmap with collaborations should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithHistory_ShouldNotThrowDetachedEntityError() {
        // Reload mindmap and create history
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Update mindmap with history enabled
        mindmapManager.updateMindmap(loadedMindmap, true); // true = save history
        entityManager.flush();
        
        // Update again to create more history entries
        loadedMindmap.setTitle("Updated Title");
        mindmapManager.updateMindmap(loadedMindmap, true);
        entityManager.flush();
        
        // Verify history exists
        final java.util.List<MindMapHistory> history = mindmapManager.getHistoryFrom(loadedMindmap.getId());
        assertFalse(history.isEmpty(), "Mindmap should have history entries");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(loadedMindmap);
            entityManager.flush();
        }, "Deleting mindmap with history should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithLabelsAndCollaborations_ShouldNotThrowDetachedEntityError() {
        // Create collaborator user
        final Account collaborator = createTestUser("collaborator2@wisemapping.com", "Collaborator", "User2");
        entityManager.persist(collaborator);
        entityManager.flush();

        // Create additional label
        final MindmapLabel label2 = new MindmapLabel();
        label2.setTitle("Test Label 2");
        label2.setColor("#00FF00");
        label2.setCreator(testUser);
        entityManager.persist(label2);
        entityManager.flush();

        // Reload mindmap and add both labels and collaboration
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Add label
        loadedMindmap.addLabel(label2);
        
        // Create collaboration
        mindmapManager.findOrCreateCollaboration(
            loadedMindmap, 
            collaborator, 
            CollaborationRole.VIEWER
        );
        
        mindmapManager.updateMindmap(loadedMindmap, false);
        entityManager.flush();
        
        // Verify mindmap has both labels and collaborations
        final Mindmap mindmapWithBoth = entityManager.find(Mindmap.class, testMindmap.getId());
        assertFalse(mindmapWithBoth.getLabels().isEmpty(), "Mindmap should have labels");
        assertFalse(mindmapWithBoth.getCollaborations().isEmpty(), "Mindmap should have collaborations");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(mindmapWithBoth);
            entityManager.flush();
        }, "Deleting mindmap with labels and collaborations should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithLabelsCollaborationsAndHistory_ShouldNotThrowDetachedEntityError() {
        // Create collaborator users
        final Account collaborator1 = createTestUser("collaborator3@wisemapping.com", "Collaborator", "User3");
        final Account collaborator2 = createTestUser("collaborator4@wisemapping.com", "Collaborator", "User4");
        entityManager.persist(collaborator1);
        entityManager.persist(collaborator2);
        entityManager.flush();

        // Create additional labels
        final MindmapLabel label2 = new MindmapLabel();
        label2.setTitle("Test Label 2");
        label2.setColor("#00FF00");
        label2.setCreator(testUser);
        entityManager.persist(label2);
        entityManager.flush();

        final MindmapLabel label3 = new MindmapLabel();
        label3.setTitle("Test Label 3");
        label3.setColor("#0000FF");
        label3.setCreator(testUser);
        entityManager.persist(label3);
        entityManager.flush();

        // Reload mindmap and add labels, collaborations, and history
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Add labels
        loadedMindmap.addLabel(label2);
        loadedMindmap.addLabel(label3);
        
        // Create collaborations
        mindmapManager.findOrCreateCollaboration(
            loadedMindmap, 
            collaborator1, 
            CollaborationRole.EDITOR
        );
        mindmapManager.findOrCreateCollaboration(
            loadedMindmap, 
            collaborator2, 
            CollaborationRole.VIEWER
        );
        
        // Update with history enabled
        mindmapManager.updateMindmap(loadedMindmap, true);
        entityManager.flush();
        
        // Update again to create more history
        loadedMindmap.setTitle("Updated Title with Everything");
        mindmapManager.updateMindmap(loadedMindmap, true);
        entityManager.flush();
        
        // Verify mindmap has everything
        final Mindmap mindmapWithEverything = entityManager.find(Mindmap.class, testMindmap.getId());
        assertFalse(mindmapWithEverything.getLabels().isEmpty(), "Mindmap should have labels");
        assertFalse(mindmapWithEverything.getCollaborations().isEmpty(), "Mindmap should have collaborations");
        final java.util.List<MindMapHistory> history = mindmapManager.getHistoryFrom(mindmapWithEverything.getId());
        assertFalse(history.isEmpty(), "Mindmap should have history entries");
        
        System.out.println("Mindmap has:");
        System.out.println("  - " + mindmapWithEverything.getLabels().size() + " labels");
        System.out.println("  - " + mindmapWithEverything.getCollaborations().size() + " collaborations");
        System.out.println("  - " + history.size() + " history entries");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(mindmapWithEverything);
            entityManager.flush();
        }, "Deleting mindmap with labels, collaborations, and history should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithLabels_ShouldNotThrowDetachedEntityError() {
        // Reload mindmap to ensure it's managed
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Verify mindmap has labels
        assertFalse(loadedMindmap.getLabels().isEmpty(), "Mindmap should have labels");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(loadedMindmap);
            entityManager.flush();
        }, "Deleting mindmap with labels should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithLabels_AfterReloading_ShouldNotThrowDetachedEntityError() {
        // This test simulates the scenario where the mindmap is reloaded
        // and then deleted, which can cause the labels collection to be loaded
        // with potentially detached entities
        
        // Reload mindmap to ensure it's managed
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Access labels collection to trigger lazy loading
        // This simulates the scenario where labels are loaded
        final int labelCount = loadedMindmap.getLabels().size();
        assertTrue(labelCount > 0, "Mindmap should have labels");
        
        // Clear persistence context to simulate detached state
        entityManager.clear();
        
        // Reload mindmap again (this is what happens in removeMindmap)
        final Mindmap freshMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(freshMindmap, "Fresh mindmap should exist");
        
        // This should NOT throw "detached entity passed to persist" error
        assertDoesNotThrow(() -> {
            mindmapManager.removeMindmap(freshMindmap);
            entityManager.flush();
        }, "Deleting mindmap with labels after reloading should not throw detached entity error");

        // Verify mindmap was deleted
        final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNull(deletedMindmap, "Mindmap should be deleted");
    }

    @Test
    public void testRemoveMindmapWithLabels_ReproducesDetachedEntityError() {
        // This test is designed to reproduce the "detached entity passed to persist" error
        // The issue occurs when calling removeMindmap on a mindmap that has labels loaded.
        // Inside removeMindmap:
        // 1. Native SQL deletes label associations from database
        // 2. Mindmap is detached and reloaded
        // 3. When reloading, if the labels collection gets loaded (from @Fetch(FetchMode.JOIN) or cache),
        //    it might still have references to labels even though associations are deleted
        // 4. When removing the fresh mindmap, Hibernate sees CascadeType.PERSIST on labels
        //    and tries to persist them, but they are detached
        
        // Load mindmap and ensure labels collection is loaded
        final Mindmap loadedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
        assertNotNull(loadedMindmap, "Mindmap should exist");
        
        // Force load the labels collection by accessing it
        // This ensures the labels are loaded into the persistence context
        final int labelCount = loadedMindmap.getLabels().size();
        assertTrue(labelCount > 0, "Mindmap should have labels");
        System.out.println("Loaded mindmap has " + labelCount + " labels");
        
        // Verify labels are in persistence context
        for (MindmapLabel label : loadedMindmap.getLabels()) {
            assertTrue(entityManager.contains(label), "Label should be in persistence context");
        }
        
        // Now call removeMindmap with the loaded mindmap (which has labels)
        // This is the real scenario - someone calls removeMindmap on a mindmap with labels loaded
        // Inside removeMindmap, it will:
        // 1. Delete label associations via native SQL
        // 2. Detach and reload the mindmap
        // 3. Try to remove the fresh mindmap
        // The error should occur when trying to remove the fresh mindmap if labels are still in the collection
        try {
            mindmapManager.removeMindmap(loadedMindmap);
            entityManager.flush();
            
            // If we get here without error, the issue might not be reproducible in this test setup
            // But let's verify the mindmap was deleted
            final Mindmap deletedMindmap = entityManager.find(Mindmap.class, testMindmap.getId());
            assertNull(deletedMindmap, "Mindmap should be deleted");
            
            // If no error occurred, print a message
            System.out.println("No error occurred - issue might not be reproducible in this test scenario");
            System.out.println("This might be because:");
            System.out.println("1. HSQLDB handles things differently than MySQL");
            System.out.println("2. Second-level cache is disabled in tests");
            System.out.println("3. The labels collection is empty after reload in removeMindmap");
            System.out.println("4. The @Fetch(FetchMode.JOIN) on labels might not load them after association deletion");
        } catch (org.hibernate.PersistentObjectException | org.springframework.dao.InvalidDataAccessApiUsageException e) {
            // This is the error we're trying to reproduce
            if (e.getMessage() != null && e.getMessage().contains("detached entity passed to persist")) {
                // Good! We reproduced the issue
                System.out.println("=========================================");
                System.out.println("SUCCESSFULLY REPRODUCED THE ISSUE!");
                System.out.println("=========================================");
                System.out.println("Error: " + e.getMessage());
                System.out.println("Error class: " + e.getClass().getName());
                System.out.println("Stack trace:");
                e.printStackTrace();
                System.out.println("=========================================");
                System.out.println("This confirms the bug exists and needs to be fixed");
                // Re-throw to fail the test and show the error
                throw new AssertionError("Detached entity error reproduced - this is the bug we're fixing", e);
            } else {
                // Different error - re-throw it
                System.out.println("Different error occurred: " + e.getMessage());
                System.out.println("Error class: " + e.getClass().getName());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            // Catch any other exception
            System.out.println("Unexpected error occurred: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    private Account createTestUser(String email, String firstname, String lastname) {
        final Account user = new Account();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPassword("password123");
        user.setCreationDate(Calendar.getInstance());
        user.setAuthenticationType(AuthenticationType.DATABASE);
        return user;
    }

    private Mindmap createTestMindmap(String title, Account creator) {
        final Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        mindmap.setCreationTime(Calendar.getInstance());
        mindmap.setLastModificationTime(Calendar.getInstance());
        mindmap.setPublic(false);
        try {
            mindmap.setXmlStr("<map version=\"tango\"><topic central=\"true\" text=\"" + title + "\"/></map>");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set XML content", e);
        }
        return mindmap;
    }
}

