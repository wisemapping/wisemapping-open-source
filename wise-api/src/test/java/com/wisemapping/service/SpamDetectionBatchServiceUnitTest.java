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

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
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
    private TransactionTemplate transactionTemplate;

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
            return callback.doInTransaction(null);
        });

        // Set up service configuration
        ReflectionTestUtils.setField(spamDetectionBatchService, "enabled", true);
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
        when(mindmapManager.countAllPublicMindmapsSince(any(Calendar.class))).thenReturn(1L);
        when(mindmapManager.findAllPublicMindmapsSince(any(Calendar.class), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));

        // Act
        spamDetectionBatchService.processPublicMapsSpamDetection();

        // Assert
        verify(mindmapManager, atLeastOnce()).countAllPublicMindmapsSince(any(Calendar.class));
    }

    @Test
    void testProcessBatch_WithSpamDetected_ShouldMarkAsSpam() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.isSpamContent(testMindmap)).thenReturn(true);

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(1, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
        assertTrue(testMindmap.isSpamDetected());
        verify(mindmapManager, times(1)).updateMindmap(eq(testMindmap), eq(false));
    }

    @Test
    void testProcessBatch_WithNoSpamDetected_ShouldNotMarkAsSpam() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.isSpamContent(testMindmap)).thenReturn(false);

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
        assertFalse(testMindmap.isSpamDetected());
        // Version should be updated even when no spam is detected
        assertEquals(1, testMindmap.getSpamDetectionVersion());
        verify(mindmapManager, times(1)).updateMindmap(eq(testMindmap), eq(false));
    }

    @Test
    void testProcessBatch_WithAlreadySpamDetected_ShouldSkip() {
        // Arrange
        testMindmap.setSpamDetected(true);
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount); // processedCount represents total mindmaps processed
        assertEquals(0, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
        // Version should still be updated even if already marked as spam
        assertEquals(1, testMindmap.getSpamDetectionVersion());
        verify(spamDetectionService, never()).isSpamContent(any(Mindmap.class));
        verify(mindmapManager, times(1)).updateMindmap(eq(testMindmap), eq(false));
    }

    @Test
    void testProcessBatch_WithSpamDetectionException_ShouldContinueProcessing() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));
        when(spamDetectionService.isSpamContent(testMindmap)).thenThrow(new RuntimeException("Spam detection error"));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
        // When exception occurs, mindmap should not be updated
        verify(mindmapManager, never()).updateMindmap(any(Mindmap.class), anyBoolean());
    }

    @Test
    void testGetTotalMapsCount_ShouldReturnCorrectCount() {
        // Arrange
        Calendar cutoffDate = Calendar.getInstance();
        long expectedCount = 5L;
        when(mindmapManager.countAllPublicMindmapsSince(cutoffDate)).thenReturn(expectedCount);

        // Act
        long actualCount = spamDetectionBatchService.getTotalMapsCount(cutoffDate);

        // Assert
        assertEquals(expectedCount, actualCount);
        verify(mindmapManager, times(1)).countAllPublicMindmapsSince(cutoffDate);
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
        SpamDetectionBatchService.BatchResult result = new SpamDetectionBatchService.BatchResult(5, 2, 1);

        // Assert
        assertEquals(5, result.processedCount);
        assertEquals(2, result.spamDetectedCount);
        assertEquals(1, result.disabledAccountCount);
    }

    @Test
    void testBatchResult_WithZeroValues_ShouldWorkCorrectly() {
        // Act
        SpamDetectionBatchService.BatchResult result = new SpamDetectionBatchService.BatchResult(0, 0, 0);

        // Assert
        assertEquals(0, result.processedCount);
        assertEquals(0, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
    }

    @Test
    void testProcessBatch_WithSuspendedUser_ShouldMakeMapPrivate() {
        // Arrange
        testUser.setSuspended(true);
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount); // processedCount represents total mindmaps processed
        assertEquals(0, result.spamDetectedCount);
        assertEquals(1, result.disabledAccountCount);
        assertFalse(testMindmap.isPublic()); // Should be made private
        // Version should be updated when making map private due to suspended user
        assertEquals(1, testMindmap.getSpamDetectionVersion());
        verify(mindmapManager, times(1)).updateMindmap(eq(testMindmap), eq(false));
    }

    @Test
    void testProcessBatch_WithHigherVersion_ShouldSkipProcessing() {
        // Arrange
        testMindmap.setSpamDetectionVersion(2); // Higher than current version (1)
        Calendar cutoffDate = Calendar.getInstance();
        when(mindmapManager.findAllPublicMindmapsSince(eq(cutoffDate), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(testMindmap));

        // Act
        SpamDetectionBatchService.BatchResult result = spamDetectionBatchService.processBatch(cutoffDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.processedCount); // Still counts as processed
        assertEquals(0, result.spamDetectedCount);
        assertEquals(0, result.disabledAccountCount);
        // Should not call spam detection service
        verify(spamDetectionService, never()).isSpamContent(any(Mindmap.class));
        // Should not update mindmap
        verify(mindmapManager, never()).updateMindmap(any(Mindmap.class), anyBoolean());
    }

    @Test
    void testGetCurrentSpamDetectionVersion_ShouldReturnConfiguredValue() {
        // Act & Assert
        assertEquals(1, spamDetectionBatchService.getCurrentSpamDetectionVersion());
    }
}
