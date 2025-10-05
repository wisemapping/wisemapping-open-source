package com.wisemapping.scheduler;

import com.wisemapping.config.AppConfig;
import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.service.HistoryPurgeService;
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
 * Integration tests for HistoryCleanupScheduler with real Spring context and database.
 * Tests the actual scheduling functionality and database interactions.
 */
@SpringBootTest(classes = {AppConfig.class})
@TestPropertySource(properties = {
        "app.batch.history-cleanup.enabled=true",
        "app.batch.history-cleanup.phase1-old-maps-years=3",
        "app.batch.history-cleanup.phase2-max-entries=4",
        "app.batch.history-cleanup.batch-size=100",
        "app.batch.history-cleanup.cron-expression=0 0 2 * * *",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class HistoryCleanupSchedulerIntegrationTest {

    @Autowired
    private HistoryCleanupScheduler historyCleanupScheduler;

    @Autowired
    private HistoryPurgeService historyPurgeService;

    @Autowired
    private MindmapManager mindmapManager;


    @Autowired
    private EntityManager entityManager;

    private Account testUser;
    private Mindmap testMindmap;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = createTestUser("historycleanup@test.com", "History", "Cleanup");
        entityManager.persist(testUser);

        // Create test mindmap
        testMindmap = createTestMindmap("History Cleanup Test Mindmap", testUser);
        entityManager.persist(testMindmap);

        entityManager.flush();
    }

    @Test
    @Transactional
    void testHistoryCleanupScheduler_WithOldHistoryEntries_ShouldCleanUp() {
        // Create old history entries (older than 90 days)
        Calendar oldDate = Calendar.getInstance();
        oldDate.add(Calendar.DAY_OF_MONTH, -100); // 100 days ago

        for (int i = 0; i < 5; i++) {
            MindMapHistory oldHistory = createHistoryEntry(testMindmap, oldDate, "Old history " + i);
            entityManager.persist(oldHistory);
        }

        // Create recent history entries (within 90 days)
        Calendar recentDate = Calendar.getInstance();
        recentDate.add(Calendar.DAY_OF_MONTH, -30); // 30 days ago

        for (int i = 0; i < 3; i++) {
            MindMapHistory recentHistory = createHistoryEntry(testMindmap, recentDate, "Recent history " + i);
            entityManager.persist(recentHistory);
        }

        entityManager.flush();

        // Verify initial state
        List<MindMapHistory> allHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertEquals(8, allHistory.size(), "Should have 8 history entries initially");

        // Run the scheduler cleanup
        historyPurgeService.purgeHistory();

        // Verify old entries were cleaned up
        List<MindMapHistory> remainingHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertTrue(remainingHistory.size() <= 4, "Should have at most 4 recent history entries remaining (Phase 2 limit)");

        // Verify all remaining entries are recent
        for (MindMapHistory history : remainingHistory) {
            assertTrue(history.getCreationTime().after(Calendar.getInstance()) ||
                      history.getCreationTime().getTimeInMillis() > Calendar.getInstance().getTimeInMillis() - (90L * 24 * 60 * 60 * 1000),
                      "All remaining history entries should be within retention period");
        }
    }

    @Test
    @Transactional
    void testHistoryCleanupScheduler_WithManyEntries_ShouldLimitPerMindmap() {
        // Create many history entries (more than phase2MaxEntries = 4)
        // The test mindmap is created recently, so it should fall under Phase 2
        Calendar baseDate = Calendar.getInstance();
        baseDate.add(Calendar.DAY_OF_MONTH, -50); // 50 days ago

        for (int i = 0; i < 15; i++) {
            Calendar entryDate = Calendar.getInstance();
            entryDate.setTime(baseDate.getTime());
            entryDate.add(Calendar.MINUTE, i); // Stagger by minutes

            MindMapHistory history = createHistoryEntry(testMindmap, entryDate, "History entry " + i);
            entityManager.persist(history);
        }

        entityManager.flush();

        // Verify initial state
        List<MindMapHistory> allHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertEquals(15, allHistory.size(), "Should have 15 history entries initially");

        // Run the scheduler cleanup
        historyPurgeService.purgeHistory();

        // Verify entries were limited to phase2MaxEntries (4)
        List<MindMapHistory> remainingHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertTrue(remainingHistory.size() <= 4, "Should have at most 4 history entries remaining (Phase 2 limit)");
        assertTrue(remainingHistory.size() > 0, "Should have at least 1 history entry remaining");

        // Verify remaining entries are the most recent ones
        for (int i = 1; i < remainingHistory.size(); i++) {
            assertTrue(remainingHistory.get(i-1).getCreationTime().getTimeInMillis() >= 
                      remainingHistory.get(i).getCreationTime().getTimeInMillis(),
                      "History entries should be ordered by creation time (newest first)");
        }
    }

    @Test
    @Transactional
    void testHistoryCleanupScheduler_WithOldMindmap_ShouldRemoveAllHistory() {
        // Create an old mindmap (older than 3 years) - should fall under Phase 1
        Account oldUser = createTestUser("olduser@test.com", "Old", "User");
        Mindmap oldMindmap = createTestMindmap("Old Mindmap", oldUser);
        
        // Set the mindmap's last modification time to 4 years ago
        Calendar fourYearsAgo = Calendar.getInstance();
        fourYearsAgo.add(Calendar.YEAR, -4);
        oldMindmap.setLastModificationTime(fourYearsAgo);
        entityManager.persist(oldMindmap);
        
        // Create history entries for the old mindmap
        for (int i = 0; i < 10; i++) {
            Calendar entryDate = Calendar.getInstance();
            entryDate.add(Calendar.DAY_OF_MONTH, -i);
            
            MindMapHistory history = createHistoryEntry(oldMindmap, entryDate, "Old history entry " + i);
            entityManager.persist(history);
        }
        
        entityManager.flush();
        
        // Verify initial state
        List<MindMapHistory> allHistory = mindmapManager.getHistoryFrom(oldMindmap.getId());
        assertEquals(10, allHistory.size(), "Should have 10 history entries initially");
        
        // Run the scheduler cleanup
        historyPurgeService.purgeHistory();
        
        // Verify all history was removed (Phase 1: old maps get all history removed)
        List<MindMapHistory> remainingHistory = mindmapManager.getHistoryFrom(oldMindmap.getId());
        assertEquals(0, remainingHistory.size(), "Should have 0 history entries remaining (Phase 1: all removed)");
    }

    @Test
    @Transactional
    void testHistoryCleanupScheduler_WithNoHistory_ShouldHandleGracefully() {
        // Verify no history entries exist
        List<MindMapHistory> initialHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertEquals(0, initialHistory.size(), "Should have no history entries initially");

        // Run the scheduler cleanup
        assertDoesNotThrow(() -> historyPurgeService.purgeHistory());

        // Verify still no history entries
        List<MindMapHistory> finalHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertEquals(0, finalHistory.size(), "Should still have no history entries after cleanup");
    }

    @Test
    @Transactional
    void testHistoryCleanupScheduler_WithMixedAges_ShouldCleanUpCorrectly() {
        // Create very old entries (should be deleted)
        Calendar veryOldDate = Calendar.getInstance();
        veryOldDate.add(Calendar.DAY_OF_MONTH, -200); // 200 days ago

        for (int i = 0; i < 3; i++) {
            MindMapHistory veryOldHistory = createHistoryEntry(testMindmap, veryOldDate, "Very old " + i);
            entityManager.persist(veryOldHistory);
        }

        // Create old entries (should be deleted)
        Calendar oldDate = Calendar.getInstance();
        oldDate.add(Calendar.DAY_OF_MONTH, -100); // 100 days ago

        for (int i = 0; i < 2; i++) {
            MindMapHistory oldHistory = createHistoryEntry(testMindmap, oldDate, "Old " + i);
            entityManager.persist(oldHistory);
        }

        // Create recent entries (should be kept)
        Calendar recentDate = Calendar.getInstance();
        recentDate.add(Calendar.DAY_OF_MONTH, -30); // 30 days ago

        for (int i = 0; i < 8; i++) {
            MindMapHistory recentHistory = createHistoryEntry(testMindmap, recentDate, "Recent " + i);
            entityManager.persist(recentHistory);
        }

        entityManager.flush();

        // Verify initial state
        List<MindMapHistory> allHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertEquals(13, allHistory.size(), "Should have 13 history entries initially");

        // Run the scheduler cleanup
        historyPurgeService.purgeHistory();

        // Verify only recent entries remain
        List<MindMapHistory> remainingHistory = mindmapManager.getHistoryFrom(testMindmap.getId());
        assertTrue(remainingHistory.size() <= 4, "Should have at most 4 recent history entries remaining (Phase 2 limit)");

        // Verify all remaining entries are recent (check by creation time being within retention period)
        Calendar retentionThreshold = Calendar.getInstance();
        retentionThreshold.add(Calendar.DAY_OF_MONTH, -90);
        
        for (MindMapHistory history : remainingHistory) {
            assertTrue(history.getCreationTime().after(retentionThreshold), 
                      "All remaining entries should be within retention period");
        }
    }

    @Test
    @Transactional
    void testHistoryCleanupService_Configuration_ShouldBeCorrect() {
        // Test service configuration
        assertTrue(historyPurgeService.isEnabled(), "History cleanup should be enabled");
        assertEquals(3, historyPurgeService.getPhase1LowerBoundaryYears(), "Phase 1 lower boundary years should be 3");
        assertEquals(4, historyPurgeService.getPhase2MaxEntries(), "Phase 2 max entries should be 4");
        assertEquals(100, historyPurgeService.getBatchSize(), "Batch size should be 100");
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
            mindmap.setXmlStr("<?xml version=\"1.0\" encoding=\"UTF-8\"?><map name=\"test\" version=\"tango\"><topic central=\"true\" text=\"Test Topic\" id=\"1\"/></map>");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set XML content", e);
        }

        return mindmap;
    }

    private MindMapHistory createHistoryEntry(Mindmap mindmap, Calendar creationTime, String description) {
        MindMapHistory history = new MindMapHistory();
        history.setMindmapId(mindmap.getId());
        history.setEditor(mindmap.getCreator());
        history.setCreationTime(creationTime);
        
        // Set XML content using the zipped XML method
        try {
            history.setZippedXml("<map>history content</map>".getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set XML content", e);
        }
        return history;
    }
}
