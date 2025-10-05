package com.wisemapping.scheduler;

import com.wisemapping.service.HistoryPurgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for cleaning up old mindmap history entries.
 * Runs daily to remove history entries older than 90 days and
 * limits the number of history entries per mindmap to 10.
 * Uses pagination to prevent memory issues with large datasets.
 */
@Component
public class HistoryCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HistoryCleanupScheduler.class);

    @Autowired
    private HistoryPurgeService historyPurgeService;

    @Value("${app.batch.history-cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.history-cleanup.startup-enabled:false}")
    private boolean startupEnabled;

    /**
     * Scheduled task that runs daily at 2:00 AM to clean up old mindmap history entries.
     * 
     * Cron expression: "0 0 2 * * *" means:
     * - 0 seconds
     * - 0 minutes
     * - 2 hours (2:00 AM)
     * - Every day of month
     * - Every month
     * - Every day of week
     */
    @Scheduled(cron = "${app.batch.history-cleanup.cron-expression:0 0 2 * * *}")
    @Async
    public void cleanupHistory() {
        if (!enabled) {
            logger.debug("History cleanup scheduler is disabled");
            return;
        }

        logger.info("Starting scheduled history cleanup task (async)");
        
        try {
            int deletedCount = historyPurgeService.purgeHistory();
            logger.info("Scheduled history cleanup task completed successfully. Deleted {} history entries", deletedCount);
        } catch (Exception e) {
            logger.error("Scheduled history cleanup task failed", e);
        }
    }

    /**
     * Startup task that runs when the application is ready.
     * Only executes if both enabled and startupEnabled are true.
     * Runs asynchronously to avoid blocking application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void processHistoryCleanupOnStartup() {
        if (!enabled) {
            logger.debug("History cleanup scheduler is disabled - skipping startup execution");
            return;
        }

        if (!startupEnabled) {
            logger.debug("History cleanup startup execution is disabled - skipping startup execution");
            return;
        }

        logger.info("Starting history cleanup task on application startup (async)");
        
        try {
            int deletedCount = historyPurgeService.purgeHistory();
            logger.info("Startup history cleanup task completed successfully. Deleted {} history entries", deletedCount);
        } catch (Exception e) {
            logger.error("Startup history cleanup task failed", e);
        }
    }
}