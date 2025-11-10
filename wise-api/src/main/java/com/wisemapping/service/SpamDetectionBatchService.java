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
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindmapSpamInfo;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.service.spam.SpamDetectionResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpamDetectionBatchService {

    private static final Logger logger = LoggerFactory.getLogger(SpamDetectionBatchService.class);
    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 500;

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private SpamDetectionService spamDetectionService;

    @Autowired
    private MetricsService metricsService;


    @Value("${app.batch.spam-detection.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.spam-detection.batch-size:50}")
    private int batchSize;

    @Value("${app.batch.spam-detection.months-back:48}")
    private int monthsBack;

    @Value("${app.batch.spam-detection.version:5}")
    private int currentSpamDetectionVersion;

    /**
     * Process all public maps and mark them as spam if they match spam detection rules
     * Each batch is processed in its own transaction to avoid long-running transactions
     * This method itself is not transactional to avoid connection leaks
     */
    public void processPublicMapsSpamDetection() {
        if (!enabled) {
            logger.debug("Spam detection batch task is disabled");
            return;
        }

        final int safeBatchSize = resolveBatchSize();
        logger.info("Starting spam detection batch task for public maps created since {} months ago", monthsBack);

        try {
            // Calculate cutoff date (monthsBack months ago)
            java.util.Calendar cutoffDate = java.util.Calendar.getInstance();
            cutoffDate.add(java.util.Calendar.MONTH, -monthsBack);

            // Get total count for logging
            long totalMaps = getTotalMapsCount(cutoffDate);
            logger.info("Starting spam detection for {} public maps created since {} in batches of {} (current version: {})",
                    totalMaps, cutoffDate.getTime(), safeBatchSize, currentSpamDetectionVersion);

            int processedCount = 0;
            int spamDetectedCount = 0;
            int offset = 0;

            // Process maps in batches, each batch in its own transaction
            while (offset < totalMaps) {
                try {
                    BatchResult result = processBatch(cutoffDate, offset, safeBatchSize);
                    processedCount += result.processedCount;
                    spamDetectedCount += result.spamDetectedCount;

                    if (result.processedCount == 0) {
                        break; // No more maps to process
                    }

                    offset += safeBatchSize;
                    logger.debug("Processed batch: offset={}, batchSize={}, totalProcessed={}",
                            offset - safeBatchSize, safeBatchSize, processedCount);
                } catch (Exception e) {
                    logger.error("Error processing batch at offset {}: {}", offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += safeBatchSize;
                }
            }

            logger.info("Spam detection batch task completed. Processed {} public maps, marked {} as spam",
                    processedCount, spamDetectedCount);

        } catch (Exception e) {
            logger.error("Error during spam detection batch task", e);
        }
    }

    /**
     * Process a single batch of mindmaps in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatch(java.util.Calendar cutoffDate, int offset, int batchSize) {
        List<Mindmap> publicMaps = mindmapManager.findPublicMindmapsNeedingSpamDetection(cutoffDate, currentSpamDetectionVersion, offset, batchSize);

        if (publicMaps.isEmpty()) {
            return new BatchResult(0, 0);
        }

        int processedCount = 0;
        int spamDetectedCount = 0;

        for (Mindmap mindmap : publicMaps) {
            try {
                SpamStrategyType spamTypeCode = null;

                // Check for spam content only if not already marked as spam
                SpamDetectionResult spamResult = spamDetectionService.detectSpam(mindmap, "batch_scan");
                if (spamResult.isSpam()) {
                    // Get strategy name as enum
                    spamTypeCode = spamResult.getStrategyType();
                    spamDetectedCount++;
                    logger.warn("Marked public mindmap '{}' (ID: {}) as spam with type: {} (strategy: {}) (last win)",
                            mindmap.getTitle(), mindmap.getId(), spamTypeCode, spamResult.getStrategyType());

                    // Track spam detection (null-safe)
                    metricsService.trackSpamDetection(mindmap, spamResult, "batch_scan");
                } else {
                    // No spam detected - still need to update version to track processing
                    logger.debug("No spam detected in mindmap '{}' (ID: {}) - updating version only",
                            mindmap.getTitle(), mindmap.getId());
                }

                // Always update spam info to track processing version
                String spamDescription = spamResult.isSpam() ? spamResult.getDetails() : null;
                updateSpamInfo(mindmap, spamTypeCode, spamDescription);
                processedCount++;
            } catch (Exception e) {
                logger.error("Error processing mindmap '{}' (ID: {}): {}",
                        mindmap.getTitle(), mindmap.getId(), e.getMessage(), e);
                // Continue processing other mindmaps in the batch
                processedCount++;
            }
        }

        return new BatchResult(processedCount, spamDetectedCount);
    }

    /**
     * Helper method to update spam info for a mindmap
     * Handles both new and existing MindmapSpamInfo entities
     */
    protected void updateSpamInfo(Mindmap mindmap, @Nullable SpamStrategyType spamTypeCode, @Nullable String description) {
        try {
            MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);

            // Set spam detection status and version
            boolean isSpamDetected = (spamTypeCode != null);
            spamInfo.setSpamDetected(isSpamDetected);
            spamInfo.setSpamDetectionVersion(currentSpamDetectionVersion);

            // Set spam type code if provided (only when spam is detected)
            if (isSpamDetected) {
                spamInfo.setSpamTypeCode(spamTypeCode);
                spamInfo.setSpamDescription(description);
            }

            mindmapManager.updateMindmapSpamInfo(spamInfo);
        } catch (Exception updateException) {
            logger.error("Failed to update spam info for mindmap '{}' (ID: {}): {}",
                    mindmap.getTitle(), mindmap.getId(), updateException.getMessage(), updateException);
        }
    }


    /**
     * Result class for batch processing
     */
    public static class BatchResult {
        public final int processedCount;
        public final int spamDetectedCount;

        BatchResult(int processedCount, int spamDetectedCount) {
            this.processedCount = processedCount;
            this.spamDetectedCount = spamDetectedCount;
        }
    }

    /**
     * Get total count of public mindmaps since cutoff date (transactional)
     */
    public long getTotalMapsCount(java.util.Calendar cutoffDate) {
        return mindmapManager.countPublicMindmapsNeedingSpamDetection(cutoffDate, currentSpamDetectionVersion);
    }

    /**
     * Check if the service is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the batch size configuration
     */
    public int getBatchSize() {
        return clampBatchSize(false);
    }

    /**
     * Get the current spam detection version
     */
    public int getCurrentSpamDetectionVersion() {
        return currentSpamDetectionVersion;
    }

    private int resolveBatchSize() {
        return clampBatchSize(true);
    }

    private int clampBatchSize(boolean logAdjustments) {
        int normalized = batchSize;
        if (normalized < MIN_BATCH_SIZE) {
            if (logAdjustments) {
                logger.warn("Spam detection batch size {} is too small. Using minimum {}.", normalized, MIN_BATCH_SIZE);
            }
            normalized = MIN_BATCH_SIZE;
        }
        if (normalized > MAX_BATCH_SIZE) {
            if (logAdjustments) {
                logger.warn("Spam detection batch size {} exceeds safe limit {}. Using cap.", normalized, MAX_BATCH_SIZE);
            }
            normalized = MAX_BATCH_SIZE;
        }
        return normalized;
    }
}
