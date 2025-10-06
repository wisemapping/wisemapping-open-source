package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

/**
 * Service responsible for purging old mindmap history entries using a two-phase
 * approach.
 * 
 * Phase 1: For maps between lower and upper boundary years, removes all history
 * Phase 2: For maps newer than lower boundary, keeps only a limited number of
 * recent entries
 * 
 * Uses batch processing to prevent memory issues with large datasets.
 */
@Service
public class HistoryPurgeService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryPurgeService.class);

    @Autowired
    private MindmapManager mindmapManager;

    @Value("${app.batch.history-cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.history-cleanup.phase1-lower-boundary-years:3}")
    private int phase1LowerBoundaryYears;

    @Value("${app.batch.history-cleanup.phase1-upper-boundary-years:17}")
    private int phase1UpperBoundaryYears;

    @Value("${app.batch.history-cleanup.phase2-lower-boundary-years:1}")
    private int phase2LowerBoundaryYears;

    @Value("${app.batch.history-cleanup.phase2-upper-boundary-years:0.5}")
    private double phase2UpperBoundaryYears;

    @Value("${app.batch.history-cleanup.phase2-max-entries:4}")
    private int phase2MaxEntries;

    @Value("${app.batch.history-cleanup.batch-size:100}")
    private int batchSize;

    /**
     * Purge old mindmap history entries using two-phase approach.
     *
     * Phase 1: For maps between phase1LowerBoundaryYears (2 years) and
     * phase1UpperBoundaryYears (1 year), removes all history
     * Phase 2: For maps between phase2LowerBoundaryYears (1 year) and phase2UpperBoundaryYears (6 months), keeps only
     * phase2MaxEntries recent entries
     *
     * @return number of history entries deleted
     */
    public int purgeHistory() {
        if (!enabled) {
            logger.debug("History cleanup is disabled");
            return 0;
        }

        logger.info(
                "Starting two-phase history cleanup - phase1 lower boundary: {} years, phase1 upper boundary: {} years, phase2 lower boundary: {} years, phase2 upper boundary: {} years, phase2 max entries: {}, batch size: {}",
                phase1LowerBoundaryYears, phase1UpperBoundaryYears, phase2LowerBoundaryYears, phase2UpperBoundaryYears, phase2MaxEntries,
                batchSize);

        try {
            // Create context for the cleanup operation
            HistoryCleanupContext context = new HistoryCleanupContext(phase1LowerBoundaryYears,
                    phase1UpperBoundaryYears,
                    phase2LowerBoundaryYears, phase2MaxEntries, batchSize);

            // Set up the chain of responsibility
            final AbstractHistoryCleanupHandler phase1Handler = new Phase1HistoryCleanupHandler(
                    mindmapManager, phase1LowerBoundaryYears, phase1UpperBoundaryYears);
            final AbstractHistoryCleanupHandler phase2Handler = new Phase2HistoryCleanupHandler(
                    mindmapManager, phase2LowerBoundaryYears, phase2UpperBoundaryYears, phase2MaxEntries);

            // Chain the handlers: Phase1 -> Phase2
            phase1Handler.setNext(phase2Handler);

        // First, let's check if there are any mindmaps with history at all
        try {
            List<Integer> sampleMindmapIds = mindmapManager.getMindmapIdsWithHistory(0, 5);
            logger.info("Found {} mindmaps with history entries (sample of first 5: {})", 
                       sampleMindmapIds.size(), sampleMindmapIds);
            
            // Also check total mindmaps for debugging
            long totalMindmaps = mindmapManager.countAllMindmaps();
            logger.info("Database stats: {} total mindmaps", totalMindmaps);
            
            if (sampleMindmapIds.isEmpty()) {
                logger.warn("No mindmaps with history found - this is unexpected if history tracking is enabled!");
                logger.info("Phase 1 would normally process old mindmaps (3-17 years old), but they need history entries to exist first");
                logger.info("Phase 2 would normally process recent mindmaps (newer than 1 year) to limit history entries");
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error querying mindmaps with history", e);
            return 0;
        }

        // Process mindmaps in batches using the composed strategies
        int totalDeleted = processMindmapsWithStrategies(context, phase1Handler);
        logger.info("Two-phase history cleanup completed successfully. Deleted {} history entries", totalDeleted);
        return totalDeleted;

    } catch (Exception e) {
        logger.error("History cleanup failed", e);
        throw new RuntimeException("History cleanup failed", e);
    }
    }

    /**
     * Check if history cleanup is enabled
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the configured phase 1 lower boundary in years
     * 
     * @return phase 1 lower boundary years
     */
    public int getPhase1LowerBoundaryYears() {
        return phase1LowerBoundaryYears;
    }

    /**
     * Get the configured phase 1 upper boundary in years
     * 
     * @return phase 1 upper boundary years
     */
    public int getPhase1UpperBoundaryYears() {
        return phase1UpperBoundaryYears;
    }


    /**
     * Get the maximum number of entries to keep for phase 2 (recent maps)
     * 
     * @return phase 2 max entries
     */
    public int getPhase2MaxEntries() {
        return phase2MaxEntries;
    }

    /**
     * Get the batch size for processing mindmaps
     * 
     * @return batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Process mindmaps in batches using the composed strategies
     * 
     * @param context      the cleanup context
     * @param firstHandler the first handler in the chain
     * @return total number of history entries deleted
     */
    private int processMindmapsWithStrategies(HistoryCleanupContext context, HistoryCleanupHandler firstHandler) {
        int offset = 0;
        List<Integer> mindmapIds;

        do {
            // Get a batch of unique mindmap IDs that have history
            logger.info("Fetching batch of mindmaps with history - offset: {}, batchSize: {}", offset, context.getBatchSize());
            mindmapIds = mindmapManager.getMindmapIdsWithHistory(offset, context.getBatchSize());
            logger.info("Retrieved {} mindmap IDs in this batch", mindmapIds.size());

            for (Integer mindmapId : mindmapIds) {
                context.incrementProcessed();

                Calendar lastModificationTime = mindmapManager.getMindmapLastModificationTime(mindmapId);

                if (lastModificationTime != null) {
                    logger.info("Processing mindmap {} with lastModificationTime: {}", mindmapId, lastModificationTime.getTime());
                    // Process through the chain of responsibility
                    int deleted = processMindmapThroughChain(firstHandler, mindmapId, lastModificationTime);
                    context.addDeleted(deleted);
                    if (deleted > 0) {
                        logger.info("Deleted {} history entries for mindmap {}", deleted, mindmapId);
                    } else {
                        logger.info("No history entries deleted for mindmap {} - no phase could handle it", mindmapId);
                    }
                } else {
                    logger.info("Skipping mindmap {} - no lastModificationTime", mindmapId);
                    context.incrementSkipped();
                }
            }

            offset += context.getBatchSize();
        } while (mindmapIds.size() == context.getBatchSize()); // Continue while we get a full batch

        logger.info("Two-phase history cleanup completed: processed={}, skipped={}, deleted={}",
                context.getTotalProcessed(), context.getTotalSkipped(), context.getTotalDeleted());

        return context.getTotalDeleted();
    }

    /**
     * Process a mindmap through the chain of responsibility handlers.
     * 
     * @param firstHandler         the first handler in the chain
     * @param mindmapId            the mindmap ID
     * @param lastModificationTime the last modification time
     * @return number of history entries deleted
     */
    private int processMindmapThroughChain(HistoryCleanupHandler firstHandler, int mindmapId,
            Calendar lastModificationTime) {
        HistoryCleanupHandler currentHandler = firstHandler;

        while (currentHandler != null) {
            if (currentHandler.canHandle(mindmapId, lastModificationTime)) {
                return currentHandler.processCleanup(mindmapId, lastModificationTime);
            }
            // Move to next handler in the chain
            if (currentHandler instanceof AbstractHistoryCleanupHandler) {
                currentHandler = ((AbstractHistoryCleanupHandler) currentHandler).getNext();
            } else {
                break; // No more handlers in the chain
            }
        }

        return 0; // No handler could process this mindmap
    }
}