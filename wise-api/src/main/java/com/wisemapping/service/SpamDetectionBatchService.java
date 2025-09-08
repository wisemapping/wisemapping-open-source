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
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.MindmapSpamInfo;
import com.wisemapping.service.spam.SpamDetectionResult;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private SpamDetectionService spamDetectionService;

    @Autowired
    private EntityManager entityManager;
    

    @Value("${app.batch.spam-detection.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.spam-detection.batch-size:100}")
    private int batchSize;

    @Value("${app.batch.spam-detection.months-back:45}")
    private int monthsBack;

    @Value("${app.batch.spam-detection.version:1}")
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

        logger.info("Starting spam detection batch task for public maps created since {} months ago", monthsBack);

        try {
            // Calculate cutoff date (monthsBack months ago)
            java.util.Calendar cutoffDate = java.util.Calendar.getInstance();
            cutoffDate.add(java.util.Calendar.MONTH, -monthsBack);
            
            // Get total count for logging
            long totalMaps = getTotalMapsCount(cutoffDate);
            logger.info("Starting spam detection for {} public maps created since {} in batches of {} (current version: {})", totalMaps, cutoffDate.getTime(), batchSize, currentSpamDetectionVersion);
            
            int processedCount = 0;
            int spamDetectedCount = 0;
            int disabledAccountCount = 0;
            int offset = 0;
            
            // Process maps in batches, each batch in its own transaction
            while (offset < totalMaps) {
                try {
                    BatchResult result = processBatch(cutoffDate, offset, batchSize);
                    processedCount += result.processedCount;
                    spamDetectedCount += result.spamDetectedCount;
                    disabledAccountCount += result.disabledAccountCount;
                    
                    if (result.processedCount == 0) {
                        break; // No more maps to process
                    }
                    
                    offset += batchSize;
                    logger.debug("Processed batch: offset={}, batchSize={}, totalProcessed={}", 
                        offset - batchSize, batchSize, processedCount);
                } catch (Exception e) {
                    logger.error("Error processing batch at offset {}: {}", offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += batchSize;
                }
            }

            logger.info("Spam detection batch task completed. Processed {} public maps, marked {} as spam, made {} private due to disabled accounts", 
                processedCount, spamDetectedCount, disabledAccountCount);

        } catch (Exception e) {
            logger.error("Error during spam detection batch task", e);
        }
    }

    /**
     * Process a single batch of mindmaps in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BatchResult processBatch(java.util.Calendar cutoffDate, int offset, int batchSize) {
        List<Mindmap> publicMaps = mindmapManager.findPublicMindmapsNeedingSpamDetection(cutoffDate, currentSpamDetectionVersion, offset, batchSize);
        
        if (publicMaps.isEmpty()) {
            return new BatchResult(0, 0, 0);
        }
        
        int processedCount = 0;
        int spamDetectedCount = 0;
        int disabledAccountCount = 0;
        
        for (Mindmap mindmap : publicMaps) {
            try {
                // Check if the creator account is disabled
                if (mindmap.getCreator().isSuspended()) {
                    // Make the map private if the account is disabled
                    // Use native SQL to avoid cascade conflicts with spam info
                    try {
                        // Flush any pending changes and clear the persistence context to avoid conflicts
                        entityManager.flush();
                        entityManager.clear();
                        
                        String updateSql = "UPDATE MINDMAP SET public = false WHERE id = ?";
                        int updatedRows = entityManager.createNativeQuery(updateSql)
                            .setParameter(1, mindmap.getId())
                            .executeUpdate();
                        
                        if (updatedRows == 0) {
                            logger.warn("No rows updated for mindmap '{}' (ID: {}) - mindmap may not exist or already private", 
                                mindmap.getTitle(), mindmap.getId());
                        }
                        
                        // Only update spam info if the mindmap is already marked as spam
                        if (mindmap.isSpamDetected()) {
                            MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);
                            spamInfo.setSpamDetected(true);
                            spamInfo.setSpamDetectionVersion(currentSpamDetectionVersion);
                            mindmapManager.updateMindmapSpamInfo(spamInfo);
                        }
                    } catch (Exception updateException) {
                        logger.error("Failed to make mindmap '{}' (ID: {}) private due to disabled account: {}", 
                            mindmap.getTitle(), mindmap.getId(), updateException.getMessage(), updateException);
                            logger.error(updateException.getMessage(),updateException);
                        
                    }
                    disabledAccountCount++;
                    logger.warn("Made public mindmap '{}' (ID: {}) private due to disabled account '{}'", 
                        mindmap.getTitle(), mindmap.getId(), mindmap.getCreator().getEmail());
                } else if (!mindmap.isSpamDetected()) {
                    // Check for spam content only if not already marked as spam
                    SpamDetectionResult spamResult = spamDetectionService.detectSpam(mindmap);
                    if (spamResult.isSpam()) {
                        // Mark as spam but keep it public - "last win" strategy
                        MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);
                        spamInfo.setSpamDetected(true);
                        spamInfo.setSpamDetectionVersion(currentSpamDetectionVersion);
                        
                        // Set spam type code from the strategy name
                        String strategyName = spamResult.getStrategyName();
                        if (strategyName != null) {
                            spamInfo.setSpamTypeCode(strategyName);
                        }
                        
                        // Update with "last win" strategy - will overwrite any existing data
                        try {
                            mindmapManager.updateMindmapSpamInfo(spamInfo);
                            spamDetectedCount++;
                            logger.warn("Marked public mindmap '{}' (ID: {}) as spam with type: {} (last win)", 
                                mindmap.getTitle(), mindmap.getId(), spamInfo.getSpamTypeCode());
                        } catch (Exception updateException) {
                            logger.error("Failed to update spam info for mindmap '{}' (ID: {}): {}", 
                                mindmap.getTitle(), mindmap.getId(), updateException.getMessage());
                            // Continue processing - this is a best effort operation
                        }
                    } else {
                        // No spam detected - no need to update MINDMAP_SPAM_INFO table
                        logger.debug("No spam detected in mindmap '{}' (ID: {}) - skipping spam info update", 
                            mindmap.getTitle(), mindmap.getId());
                    }
                } else {
                    // Already marked as spam, just update the version - "last win" strategy
                    try {
                        MindmapSpamInfo spamInfo = new MindmapSpamInfo(mindmap);
                        spamInfo.setSpamDetected(true);
                        spamInfo.setSpamDetectionVersion(currentSpamDetectionVersion);
                        mindmapManager.updateMindmapSpamInfo(spamInfo);
                    } catch (Exception updateException) {
                        logger.error("Failed to update spam detection version for already-spam mindmap '{}' (ID: {}): {}", 
                            mindmap.getTitle(), mindmap.getId(), updateException.getMessage());
                        // Continue processing - this is a best effort operation
                    }
                }
                
                processedCount++;
            } catch (Exception e) {
                logger.error("Error processing mindmap '{}' (ID: {}): {}", 
                    mindmap.getTitle(), mindmap.getId(), e.getMessage(), e);
                // Continue processing other mindmaps in the batch
                processedCount++;
            }
        }
        
        return new BatchResult(processedCount, spamDetectedCount, disabledAccountCount);
    }

    

    /**
     * Result class for batch processing
     */
    public static class BatchResult {
        public final int processedCount;
        public final int spamDetectedCount;
        public final int disabledAccountCount;

        BatchResult(int processedCount, int spamDetectedCount, int disabledAccountCount) {
            this.processedCount = processedCount;
            this.spamDetectedCount = spamDetectedCount;
            this.disabledAccountCount = disabledAccountCount;
        }
    }

    /**
     * Get total count of public mindmaps since cutoff date (transactional)
     */
    @Transactional(readOnly = true)
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
        return batchSize;
    }

    /**
     * Get the current spam detection version
     */
    public int getCurrentSpamDetectionVersion() {
        return currentSpamDetectionVersion;
    }
}
