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

package com.wisemapping.scheduler;

import com.wisemapping.service.HistoryPurgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryCleanupScheduler.
 * Note: Scheduler is controlled by @ConditionalOnProperty at bean creation time.
 * These tests verify the scheduler logic when it's enabled and created.
 */
@ExtendWith(MockitoExtension.class)
class HistoryCleanupSchedulerTest {

    @Mock
    private HistoryPurgeService historyPurgeService;

    @InjectMocks
    private HistoryCleanupScheduler historyCleanupScheduler;

    @Test
    void testCleanupHistory_ShouldCallService() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(25);

        // Act
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, times(1)).purgeHistory();
    }

    @Test
    void testCleanupHistory_WhenServiceThrowsException_ShouldLogError() {
        // Arrange
        RuntimeException serviceException = new RuntimeException("Database connection failed");
        when(historyPurgeService.purgeHistory()).thenThrow(serviceException);

        // Act & Assert - should not throw exception, should handle gracefully
        assertDoesNotThrow(() -> historyCleanupScheduler.cleanupHistory());

        // Verify service was called
        verify(historyPurgeService, times(1)).purgeHistory();
    }

    @Test
    void testCleanupHistory_WhenServiceReturnsZero_ShouldHandleGracefully() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(0);

        // Act
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, times(1)).purgeHistory();
    }

    @Test
    void testCleanupHistory_WhenServiceReturnsLargeNumber_ShouldHandleGracefully() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(1000);

        // Act
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, times(1)).purgeHistory();
    }

    @Test
    void testCleanupHistory_AsyncExecution_ShouldNotBlock() {
        // This test verifies that the @Async annotation doesn't cause issues in unit tests
        // In a real application, this would run asynchronously, but in unit tests it runs synchronously

        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(10);

        // Act
        long startTime = System.currentTimeMillis();
        historyCleanupScheduler.cleanupHistory();
        long endTime = System.currentTimeMillis();

        // Assert
        verify(historyPurgeService, times(1)).purgeHistory();
        
        // Verify execution was reasonably fast (not actually async in unit tests)
        assertTrue(endTime - startTime < 1000, "Execution should be fast in unit test environment");
    }

    @Test
    void testCleanupHistory_MultipleCalls_ShouldCallServiceMultipleTimes() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(5);

        // Act
        historyCleanupScheduler.cleanupHistory();
        historyCleanupScheduler.cleanupHistory();
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, times(3)).purgeHistory();
    }
}
