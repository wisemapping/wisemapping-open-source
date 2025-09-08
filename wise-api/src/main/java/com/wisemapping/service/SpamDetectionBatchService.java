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

    @Value("${app.batch.spam-detection.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.spam-detection.batch-size:100}")
    private int batchSize;

    @Value("${app.batch.spam-detection.months-back:45}")
    private int monthsBack;

    /**
     * Process all public maps and mark them as spam if they match spam detection rules
     * Each batch is processed in its own transaction to avoid long-running transactions
     * This method itself is not transactional to avoid connection leaks
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
            long totalMaps = mindmapManager.countAllPublicMindmapsSince(cutoffDate);
            logger.info("Starting spam detection for {} public maps created since {} in batches of {}", totalMaps, cutoffDate.getTime(), batchSize);
            
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatch(java.util.Calendar cutoffDate, int offset, int batchSize) {
        List<Mindmap> publicMaps = mindmapManager.findAllPublicMindmapsSince(cutoffDate, offset, batchSize);
        
        if (publicMaps.isEmpty()) {
            return new BatchResult(0, 0, 0);
        }
        
        int processedCount = 0;
        int spamDetectedCount = 0;
        int disabledAccountCount = 0;
        
        for (Mindmap mindmap : publicMaps) {
            // Check if the creator account is disabled
            if (mindmap.getCreator().isSuspended()) {
                // Make the map private if the account is disabled
                mindmap.setPublic(false);
                mindmapManager.updateMindmap(mindmap, false);
                
                disabledAccountCount++;
                logger.warn("Made public mindmap '{}' (ID: {}) private due to disabled account '{}'", 
                    mindmap.getTitle(), mindmap.getId(), mindmap.getCreator().getEmail());
                continue;
            }
            
            // Skip if already marked as spam
            if (mindmap.isSpamDetected()) {
                continue;
            }
            
            // Check for spam content
            try {
                if (spamDetectionService.isSpamContent(mindmap)) {
                    // Mark as spam but keep it public
                    mindmap.setSpamDetected(true);
                    mindmapManager.updateMindmap(mindmap, false);
                    
                    spamDetectedCount++;
                    logger.warn("Marked public mindmap '{}' (ID: {}) as spam", 
                        mindmap.getTitle(), mindmap.getId());
                }
            } catch (Exception e) {
                logger.error("Error during spam detection for mindmap '{}' (ID: {}): {}", 
                    mindmap.getTitle(), mindmap.getId(), e.getMessage(), e);
                // Continue processing other mindmaps in the batch
            }
            
            processedCount++;
        }
        
        return new BatchResult(processedCount, spamDetectedCount, disabledAccountCount);
    }

    /**
     * Result class for batch processing
     */
    private static class BatchResult {
        final int processedCount;
        final int spamDetectedCount;
        final int disabledAccountCount;

        BatchResult(int processedCount, int spamDetectedCount, int disabledAccountCount) {
            this.processedCount = processedCount;
            this.spamDetectedCount = spamDetectedCount;
            this.disabledAccountCount = disabledAccountCount;
        }
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
}
