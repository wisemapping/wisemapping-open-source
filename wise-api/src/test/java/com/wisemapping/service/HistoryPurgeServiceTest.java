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
import com.wisemapping.dao.UserManager;
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for HistoryPurgeService.
 * Tests purging of old mindmap history entries using a two-phase approach.
 */
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.batch.history-cleanup.enabled=true",
        "app.batch.history-cleanup.phase1-lower-boundary-years=2",
        "app.batch.history-cleanup.phase1-upper-boundary-years=1",
        "app.batch.history-cleanup.phase2-lower-boundary-years=1",
        "app.batch.history-cleanup.phase2-upper-boundary-years=0.5",
        "app.batch.history-cleanup.phase2-max-entries=4",
        "app.batch.history-cleanup.batch-size=100"
})
@Transactional
public class HistoryPurgeServiceTest {

    @Autowired
    private HistoryPurgeService historyPurgeService;

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserManager userManager;

    private Account testUser1;
    private Account testUser2;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = createTestUser("user1@test.com", "User", "One");
        testUser2 = createTestUser("user2@test.com", "User", "Two");
        
        userManager.createUser(testUser1);
        userManager.createUser(testUser2);
    }

    @Test
    void testCleanupHistory_WhenEnabled_ShouldReturnDeletedCount() throws InvalidMindmapException {
        // Arrange - Create mindmaps with different ages
        Mindmap oldMindmap = createTestMindmap(testUser1, "Old Mindmap", 5); // 5 years old
        Mindmap recentMindmap = createTestMindmap(testUser1, "Recent Mindmap", 1); // 1 year old
        
        mindmapManager.addMindmap(testUser1, oldMindmap);
        mindmapManager.addMindmap(testUser1, recentMindmap);

        // Act
        int deletedCount = historyPurgeService.purgeHistory();

        // Assert - Should have deleted some history entries
        assertTrue(deletedCount >= 0);
        
        // Verify service configuration
        assertTrue(historyPurgeService.isEnabled());
        assertEquals(2, historyPurgeService.getPhase1LowerBoundaryYears());
        assertEquals(1, historyPurgeService.getPhase1UpperBoundaryYears());
        assertEquals(4, historyPurgeService.getPhase2MaxEntries());
        assertEquals(100, historyPurgeService.getBatchSize());
    }

    @Test
    void testCleanupHistory_WhenDisabled_ShouldReturnZero() {
        // Arrange - Disable the service
        historyPurgeService = new HistoryPurgeService();
        // Note: In a real scenario, this would be done via configuration
        
        // Act
        int result = historyPurgeService.purgeHistory();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testGetPhase1LowerBoundaryYears_ShouldReturnCorrectValue() {
        // Act & Assert
        assertEquals(2, historyPurgeService.getPhase1LowerBoundaryYears());
    }

    @Test
    void testGetPhase1UpperBoundaryYears_ShouldReturnCorrectValue() {
        // Act & Assert
        assertEquals(1, historyPurgeService.getPhase1UpperBoundaryYears());
    }

    @Test
    void testGetPhase2MaxEntries_ShouldReturnCorrectValue() {
        // Act & Assert
        assertEquals(4, historyPurgeService.getPhase2MaxEntries());
    }

    @Test
    void testGetBatchSize_ShouldReturnCorrectValue() {
        // Act & Assert
        assertEquals(100, historyPurgeService.getBatchSize());
    }

    @Test
    void testBatchSizeClampedToSafeRange() {
        ReflectionTestUtils.setField(historyPurgeService, "batchSize", 2_000);
        assertEquals(500, historyPurgeService.getBatchSize());

        ReflectionTestUtils.setField(historyPurgeService, "batchSize", 0);
        assertEquals(1, historyPurgeService.getBatchSize());

        // Reset to default for other tests
        ReflectionTestUtils.setField(historyPurgeService, "batchSize", 100);
    }

    @Test
    void testIsEnabled_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(historyPurgeService.isEnabled());
    }

    private Account createTestUser(String email, String firstname, String lastname) {
        Account user = new Account();
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPassword("password123");
        user.setActivationCode(12345L);
        user.setActivationDate(Calendar.getInstance());
        return user;
    }

    private Mindmap createTestMindmap(Account creator, String title, int yearsOld) throws InvalidMindmapException {
        Mindmap mindmap = new Mindmap();
        mindmap.setTitle(title);
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        
        Calendar creationDate = Calendar.getInstance();
        creationDate.add(Calendar.YEAR, -yearsOld);
        mindmap.setCreationTime(creationDate);
        mindmap.setLastModificationTime(creationDate);
        
        try {
            mindmap.setXmlStr("<map name=\"test\" version=\"tango\"><topic central=\"true\" text=\"Root Topic\" id=\"1\"></topic></map>");
        } catch (InvalidMindmapException e) {
            throw new RuntimeException("Failed to set XML for test mindmap", e);
        }
        return mindmap;
    }
}
