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
    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 500;

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
            logger.info("History cleanup is disabled");
            return 0;
        }

        final int safeBatchSize = resolveBatchSize();
        // Log summary information at the beginning
        logger.info("Starting two-phase history cleanup - Phase 1: remove all history for maps {} to {} years old, Phase 2: limit history to {} entries for maps {} to {} years old, batch size: {}",
                phase1UpperBoundaryYears, phase1LowerBoundaryYears, phase2MaxEntries, phase2UpperBoundaryYears, phase2LowerBoundaryYears, safeBatchSize);

        try {
            // Create context for the cleanup operation
            HistoryCleanupContext context = new HistoryCleanupContext(phase1LowerBoundaryYears,
                    phase1UpperBoundaryYears,
                    phase2LowerBoundaryYears, phase2MaxEntries, safeBatchSize);

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
            logger.debug("Found {} mindmaps with history entries (sample of first 5: {})", 
                       sampleMindmapIds.size(), sampleMindmapIds);
            
            // Also check total mindmaps for debugging
            long totalMindmaps = mindmapManager.countAllMindmaps();
            logger.debug("Database stats: {} total mindmaps", totalMindmaps);
            
            if (sampleMindmapIds.isEmpty()) {
                logger.warn("No mindmaps with history found - this is unexpected if history tracking is enabled!");
                logger.debug("Phase 1 would normally process old mindmaps (3-17 years old), but they need history entries to exist first");
                logger.debug("Phase 2 would normally process recent mindmaps (newer than 1 year) to limit history entries");
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error querying mindmaps with history", e);
            return 0;
        }

        // Process mindmaps in batches using the composed strategies
        int totalDeleted = processMindmapsWithStrategies(context, phase1Handler);
        logger.info("History cleanup completed: {} total maps processed, {} Phase 1 maps (history removed), {} Phase 2 maps (history limited), {} maps skipped, {} history entries deleted total", 
                context.getTotalProcessed(), context.getPhase1Processed(), context.getPhase2Processed(), context.getTotalSkipped(), totalDeleted);
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
        return clampBatchSize(false);
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
            logger.debug("Fetching batch of mindmaps with history - offset: {}, batchSize: {}", offset, context.getBatchSize());
            mindmapIds = mindmapManager.getMindmapIdsWithHistory(offset, context.getBatchSize());
            logger.debug("Retrieved {} mindmap IDs in this batch", mindmapIds.size());

            for (Integer mindmapId : mindmapIds) {
                context.incrementProcessed();

                Calendar lastModificationTime = mindmapManager.getMindmapLastModificationTime(mindmapId);

                if (lastModificationTime != null) {
                    logger.debug("Processing mindmap {} with lastModificationTime: {}", mindmapId, lastModificationTime.getTime());
                    // Process through the chain of responsibility
                    int deleted = processMindmapThroughChain(context, firstHandler, mindmapId, lastModificationTime);
                    context.addDeleted(deleted);
                    if (deleted > 0) {
                        logger.debug("Deleted {} history entries for mindmap {}", deleted, mindmapId);
                    } else {
                        logger.debug("No history entries deleted for mindmap {} - no phase could handle it", mindmapId);
                    }
                } else {
                    logger.debug("Skipping mindmap {} - no lastModificationTime", mindmapId);
                    context.incrementSkipped();
                }
            }

            // Log progress after each batch
            logger.info("Batch completed: {} total maps processed so far, {} Phase 1, {} Phase 2, {} skipped, {} history entries deleted", 
                    context.getTotalProcessed(), context.getPhase1Processed(), context.getPhase2Processed(), 
                    context.getTotalSkipped(), context.getTotalDeleted());

            offset += context.getBatchSize();
        } while (mindmapIds.size() == context.getBatchSize()); // Continue while we get a full batch

        logger.debug("Two-phase history cleanup batch processing completed: processed={}, skipped={}, deleted={}",
                context.getTotalProcessed(), context.getTotalSkipped(), context.getTotalDeleted());

        return context.getTotalDeleted();
    }

    /**
     * Process a mindmap through the chain of responsibility handlers.
     * 
     * @param context              the cleanup context
     * @param firstHandler         the first handler in the chain
     * @param mindmapId            the mindmap ID
     * @param lastModificationTime the last modification time
     * @return number of history entries deleted
     */
    private int processMindmapThroughChain(HistoryCleanupContext context, HistoryCleanupHandler firstHandler, int mindmapId,
            Calendar lastModificationTime) {
        HistoryCleanupHandler currentHandler = firstHandler;
        int handlerIndex = 1; // 1 for Phase1, 2 for Phase2

        while (currentHandler != null) {
            if (currentHandler.canHandle(mindmapId, lastModificationTime)) {
                int deleted = currentHandler.processCleanup(mindmapId, lastModificationTime);
                // Track which phase processed this map
                if (handlerIndex == 1) {
                    context.incrementPhase1Processed();
                } else if (handlerIndex == 2) {
                    context.incrementPhase2Processed();
                }
                return deleted;
            }
            // Move to next handler in the chain
            if (currentHandler instanceof AbstractHistoryCleanupHandler) {
                currentHandler = ((AbstractHistoryCleanupHandler) currentHandler).getNext();
                handlerIndex++;
            } else {
                break; // No more handlers in the chain
            }
        }

        return 0; // No handler could process this mindmap
    }

    private int resolveBatchSize() {
        return clampBatchSize(true);
    }

    private int clampBatchSize(boolean logAdjustments) {
        int normalized = batchSize;
        if (normalized < MIN_BATCH_SIZE) {
            if (logAdjustments) {
                logger.warn("History cleanup batch size {} is too small. Using minimum {}.", normalized, MIN_BATCH_SIZE);
            }
            normalized = MIN_BATCH_SIZE;
        }
        if (normalized > MAX_BATCH_SIZE) {
            if (logAdjustments) {
                logger.warn("History cleanup batch size {} exceeds safe limit {}. Using cap.", normalized, MAX_BATCH_SIZE);
            }
            normalized = MAX_BATCH_SIZE;
        }
        return normalized;
    }
}
