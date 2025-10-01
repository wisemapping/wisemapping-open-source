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

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindmapSpamInfo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SpamDetectionBatchServiceUnitTest {

    @Mock
    private MindmapManager mindmapManager;

    @Mock
    private SpamDetectionService spamDetectionService;

    @Mock
    private MetricsService metricsService;
    
    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SpamDetectionBatchService spamDetectionBatchService;

    private Account testUser;
    private Mindmap testMindmap;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new Account();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstname("Test");
        testUser.setLastname("User");
        testUser.setSuspended(false);
        testUser.setCreationDate(Calendar.getInstance());

        testMindmap = new Mindmap();
        testMindmap.setId(1);
        testMindmap.setTitle("Test Mindmap");
        testMindmap.setDescription("Test Description");
        testMindmap.setPublic(true);
        testMindmap.setSpamDetected(false);
        testMindmap.setSpamDetectionVersion(0); // Set to 0 so it will be processed (0 < current version 1)
        testMindmap.setCreator(testUser);
        testMindmap.setCreationTime(Calendar.getInstance());
        testMindmap.setLastModificationTime(Calendar.getInstance());
        
        try {
            testMindmap.setXmlStr("<map><topic text=\"Test Topic\"/></map>");
        } catch (Exception e) {
            // Ignore for test setup
        }
        
        // Set up TransactionTemplate mock to execute the callback directly
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            org.springframework.transaction.TransactionStatus mockStatus = mock(org.springframework.transaction.TransactionStatus.class);
            return callback.doInTransaction(mockStatus);
        });

        // Set up EntityManager mock for native SQL queries
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(mock(jakarta.persistence.Query.class));

        // Set up service configuration
        ReflectionTestUtils.setField(spamDetectionBatchService, "enabled", true);
        ReflectionTestUtils.setField(spamDetectionBatchService, "metricsService", metricsService);
        ReflectionTestUtils.setField(spamDetectionBatchService, "batchSize", 10);
        ReflectionTestUtils.setField(spamDetectionBatchService, "monthsBack", 1);
        ReflectionTestUtils.setField(spamDetectionBatchService, "currentSpamDetectionVersion", 1);
    }

    @Test
    void testProcessPublicMapsSpamDetection_WhenDisabled_ShouldNotExecute() {
        // Arrange
        ReflectionTestUtils.setField(spamDetectionBatchService, "enabled", false);

        // Act
        spamDetectionBatchService.processPublicMapsSpamDetection();

        // Assert
        verify(mindmapManager, never()).countAllPublicMindmapsSince(any(Calendar.class));
    }

    @Test
    void testProcessPublicMapsSpamDetection_WhenEnabled_ShouldExecute() {
        // Arrange
        when(mindmapManager.countPublicMindmapsNeedingSpamDetection(any(Calendar.class), anyInt())).thenReturn(1L);
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(any(Calendar.class), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.detectSpam(testMindmap, "batch_scan")).thenReturn(
            com.wisemapping.service.spam.SpamDetectionResult.notSpam());

        // Act
        spamDetectionBatchService.processPublicMapsSpamDetection();

        // Assert
        verify(mindmapManager, atLeastOnce()).countPublicMindmapsNeedingSpamDetection(any(Calendar.class), anyInt());
    }

    @Test
    void testProcessBatch_WithSpamDetected_ShouldMarkAsSpam() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(eq(cutoffDate), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.detectSpam(testMindmap, "batch_scan")).thenReturn(
            com.wisemapping.service.spam.SpamDetectionResult.spam("Test spam", "Test details", com.wisemapping.model.SpamStrategyType.CONTACT_INFO));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(1, result.spamDetectedCount);
        // Verify that updateMindmapSpamInfo was called with spam detected
        verify(mindmapManager, times(1)).updateMindmapSpamInfo(any(MindmapSpamInfo.class));
    }

    @Test
    void testProcessBatch_WithNoSpamDetected_ShouldUpdateVersion() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(eq(cutoffDate), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.detectSpam(testMindmap, "batch_scan")).thenReturn(
            com.wisemapping.service.spam.SpamDetectionResult.notSpam());

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        // Verify that updateMindmapSpamInfo was called to track processing version even when no spam is detected
        verify(mindmapManager, times(1)).updateMindmapSpamInfo(any(MindmapSpamInfo.class));
    }

    @Test
    void testProcessBatch_WithSpamDetectionException_ShouldContinueProcessing() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(eq(cutoffDate), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.detectSpam(testMindmap, "batch_scan")).thenThrow(new RuntimeException("Spam detection error"));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        // When exception occurs, mindmap should not be updated
        verify(mindmapManager, never()).updateMindmap(any(Mindmap.class), anyBoolean());
    }

    @Test
    void testGetTotalMapsCount_ShouldReturnCorrectCount() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        long expectedCount = 5L;
        when(mindmapManager.countPublicMindmapsNeedingSpamDetection(cutoffDate, 1)).thenReturn(expectedCount);

        // Act
        long actualCount = spamDetectionBatchService.getTotalMapsCount(cutoffDate);

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(mindmapManager, times(1)).countPublicMindmapsNeedingSpamDetection(cutoffDate, 1);
    }

    @Test
    void testIsEnabled_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(spamDetectionBatchService.isEnabled());
    }

    @Test
    void testGetBatchSize_ShouldReturnConfiguredValue() {
        // Act & Assert
        assertEquals(10, spamDetectionBatchService.getBatchSize());
    }

    @Test
    void testBatchResult_Constructor_ShouldSetValuesCorrectly() {
        // Act
        SpamDetectionBatchService.BatchResult result = new SpamDetectionBatchService.BatchResult(5, 2);

        // Assert
        assertEquals(5, result.processedCount);
        assertEquals(2, result.spamDetectedCount);
    }

    @Test
    void testBatchResult_WithZeroValues_ShouldWorkCorrectly() {
        // Act
        SpamDetectionBatchService.BatchResult result = new SpamDetectionBatchService.BatchResult(0, 0);

        // Assert
        assertEquals(0, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
    }

    @Test
    void testProcessBatch_WithSuspendedUser_ShouldProcessNormally() {
        // Arrange - suspended users are now handled the same as regular users
        testUser.setSuspended(true);
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(eq(cutoffDate), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.detectSpam(testMindmap, "batch_scan")).thenReturn(
            com.wisemapping.service.spam.SpamDetectionResult.notSpam());

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        // Verify that no special handling is done for suspended users
        // The mindmap should be processed normally for spam detection and version tracking
        verify(mindmapManager, times(1)).updateMindmapSpamInfo(any(MindmapSpamInfo.class));
    }

    @Test
    void testProcessBatch_WithHigherVersion_ShouldSkipProcessing() {
        // Arrange
        testMindmap.setSpamDetectionVersion(2); // Higher than current version (1)
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findPublicMindmapsNeedingSpamDetection(eq(cutoffDate), anyInt(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList()); // No mindmaps returned because version >= current

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.processedCount); // No mindmaps processed because none returned by query
        assertEquals(0, result.spamDetectedCount);
        // Should not call spam detection service
        verify(spamDetectionService, never()).detectSpam(any(Mindmap.class));
        // Should not update mindmap
        verify(mindmapManager, never()).updateMindmap(any(Mindmap.class), anyBoolean());
    }

    @Test
    void testGetCurrentSpamDetectionVersion_ShouldReturnConfiguredValue() {
        // Act & Assert
        assertEquals(1, spamDetectionBatchService.getCurrentSpamDetectionVersion());
    }
}
