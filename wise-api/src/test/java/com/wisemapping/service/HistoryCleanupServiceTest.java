package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryCleanupService
 */
@ExtendWith(MockitoExtension.class)
class HistoryCleanupServiceTest {

    @Mock
    private MindmapManager mindmapManager;

    @InjectMocks
    private HistoryCleanupService historyCleanupService;

    @BeforeEach
    void setUp() {
        // Set default configuration values
        ReflectionTestUtils.setField(historyCleanupService, "enabled", true);
        ReflectionTestUtils.setField(historyCleanupService, "retentionDays", 90);
        ReflectionTestUtils.setField(historyCleanupService, "maxEntriesPerMap", 10);
        ReflectionTestUtils.setField(historyCleanupService, "batchSize", 100);
    }

    @Test
    void testCleanupHistory_WhenEnabled_ShouldCallMindmapManager() {
        // Arrange
        when(mindmapManager.cleanupOldMindmapHistory(any(Calendar.class), eq(10), eq(100)))
            .thenReturn(25);

        // Act
        int result = historyCleanupService.cleanupHistory();

        // Assert
        assertEquals(25, result);
        verify(mindmapManager, times(1)).cleanupOldMindmapHistory(any(Calendar.class), eq(10), eq(100));
    }

    @Test
    void testCleanupHistory_WhenDisabled_ShouldReturnZero() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupService, "enabled", false);

        // Act
        int result = historyCleanupService.cleanupHistory();

        // Assert
        assertEquals(0, result);
        verify(mindmapManager, never()).cleanupOldMindmapHistory(any(Calendar.class), anyInt(), anyInt());
    }

    @Test
    void testCleanupHistory_WhenExceptionThrown_ShouldRethrowRuntimeException() {
        // Arrange
        when(mindmapManager.cleanupOldMindmapHistory(any(Calendar.class), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> historyCleanupService.cleanupHistory());
        
        assertEquals("History cleanup failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Database error", exception.getCause().getMessage());
    }

    @Test
    void testCleanupHistory_ShouldCalculateCorrectCutoffDate() {
        // Arrange
        when(mindmapManager.cleanupOldMindmapHistory(any(Calendar.class), eq(10), eq(100)))
            .thenReturn(5);

        // Act
        historyCleanupService.cleanupHistory();

        // Assert
        verify(mindmapManager, times(1)).cleanupOldMindmapHistory(any(Calendar.class), eq(10), eq(100));
        
        // Verify the cutoff date is approximately 90 days ago
        verify(mindmapManager).cleanupOldMindmapHistory(argThat(calendar -> {
            Calendar expectedCutoff = Calendar.getInstance();
            expectedCutoff.add(Calendar.DAY_OF_MONTH, -90);
            
            // Allow for small time differences (within 1 minute)
            long timeDiff = Math.abs(calendar.getTimeInMillis() - expectedCutoff.getTimeInMillis());
            return timeDiff < 60000; // 1 minute in milliseconds
        }), eq(10), eq(100));
    }

    @Test
    void testIsEnabled_ShouldReturnCorrectValue() {
        // Test enabled
        ReflectionTestUtils.setField(historyCleanupService, "enabled", true);
        assertTrue(historyCleanupService.isEnabled());

        // Test disabled
        ReflectionTestUtils.setField(historyCleanupService, "enabled", false);
        assertFalse(historyCleanupService.isEnabled());
    }

    @Test
    void testGetRetentionDays_ShouldReturnCorrectValue() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupService, "retentionDays", 120);

        // Act & Assert
        assertEquals(120, historyCleanupService.getRetentionDays());
    }

    @Test
    void testGetMaxEntriesPerMap_ShouldReturnCorrectValue() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupService, "maxEntriesPerMap", 15);

        // Act & Assert
        assertEquals(15, historyCleanupService.getMaxEntriesPerMap());
    }

    @Test
    void testGetBatchSize_ShouldReturnCorrectValue() {
        // Arrange
        ReflectionTestUtils.setField(historyCleanupService, "batchSize", 50);

        // Act & Assert
        assertEquals(50, historyCleanupService.getBatchSize());
    }
}