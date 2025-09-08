package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * Service responsible for cleaning up old mindmap history entries.
 * Removes history entries older than a specified number of days and
 * limits the number of history entries per mindmap using pagination
 * to prevent memory issues with large datasets.
 */
@Service
public class HistoryCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryCleanupService.class);

    @Autowired
    private MindmapManager mindmapManager;

    @Value("${app.batch.history-cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.history-cleanup.retention-days:90}")
    private int retentionDays;

    @Value("${app.batch.history-cleanup.max-entries-per-map:10}")
    private int maxEntriesPerMap;

    @Value("${app.batch.history-cleanup.batch-size:100}")
    private int batchSize;

    /**
     * Clean up old mindmap history entries.
     * Removes entries older than the configured retention period and
     * limits the number of entries per mindmap using pagination.
     *
     * @return number of history entries deleted
     */
    public int cleanupHistory() {
        if (!enabled) {
            logger.debug("History cleanup is disabled");
            return 0;
        }

        logger.info("Starting history cleanup task - retention: {} days, max entries per map: {}, batch size: {}", 
            retentionDays, maxEntriesPerMap, batchSize);

        try {
            // Calculate cutoff date
            Calendar cutoffDate = Calendar.getInstance();
            cutoffDate.add(Calendar.DAY_OF_MONTH, -retentionDays);

            // Perform cleanup with pagination
            int deletedCount = mindmapManager.cleanupOldMindmapHistory(cutoffDate, maxEntriesPerMap, batchSize);

            logger.info("History cleanup completed successfully. Deleted {} history entries", deletedCount);
            return deletedCount;

        } catch (Exception e) {
            logger.error("History cleanup failed", e);
            throw new RuntimeException("History cleanup failed", e);
        }
    }

    /**
     * Check if history cleanup is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the configured retention period in days
     * @return retention days
     */
    public int getRetentionDays() {
        return retentionDays;
    }

    /**
     * Get the maximum number of entries to keep per mindmap
     * @return max entries per map
     */
    public int getMaxEntriesPerMap() {
        return maxEntriesPerMap;
    }

    /**
     * Get the batch size for processing mindmaps
     * @return batch size
     */
    public int getBatchSize() {
        return batchSize;
    }
}