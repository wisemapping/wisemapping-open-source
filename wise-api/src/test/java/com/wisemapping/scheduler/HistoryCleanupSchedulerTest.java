package com.wisemapping.scheduler;

import com.wisemapping.service.HistoryPurgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryCleanupScheduler
 */
@ExtendWith(MockitoExtension.class)
class HistoryCleanupSchedulerTest {

    @Mock
    private HistoryPurgeService historyPurgeService;

    @InjectMocks
    private HistoryCleanupScheduler historyCleanupScheduler;

    @BeforeEach
    void setUp() {
        // Set default configuration values
        ReflectionTestUtils.setField(historyCleanupScheduler, "enabled", true);
        ReflectionTestUtils.setField(historyCleanupScheduler, "startupEnabled", false);
    }

    @Test
    void testCleanupHistory_WhenEnabled_ShouldCallService() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(25);

        // Act
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, times(1)).purgeHistory();
    }

    @Test
    void testCleanupHistory_WhenDisabled_ShouldNotCallService() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupScheduler, "enabled", false);

        // Act
        historyCleanupScheduler.cleanupHistory();

        // Assert
        verify(historyPurgeService, never()).purgeHistory();
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
    void testCleanupHistory_WhenEnabledWithCustomConfiguration_ShouldWork() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupScheduler, "enabled", true);
        when(historyPurgeService.purgeHistory()).thenReturn(50);

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

    @Test
    void testCleanupHistory_EnabledDisabledToggle_ShouldRespectCurrentState() {
        // Arrange
        when(historyPurgeService.purgeHistory()).thenReturn(15);

        // Act & Assert - First call when enabled
        historyCleanupScheduler.cleanupHistory();
        verify(historyPurgeService, times(1)).purgeHistory();

        // Disable and call again
        ReflectionTestUtils.setField(historyCleanupScheduler, "enabled", false);
        historyCleanupScheduler.cleanupHistory();
        verify(historyPurgeService, times(1)).purgeHistory(); // Still only 1 call

        // Re-enable and call again
        ReflectionTestUtils.setField(historyCleanupScheduler, "enabled", true);
        historyCleanupScheduler.cleanupHistory();
        verify(historyPurgeService, times(2)).purgeHistory(); // Now 2 calls
    }
}
