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

    /**
     * Process all public maps and mark them as spam if they match spam detection rules
     */
    @Transactional
    public void processPublicMapsSpamDetection() {
        if (!enabled) {
            logger.debug("Spam detection batch task is disabled");
            return;
        }

        logger.info("Starting spam detection batch task for public maps");

        try {
            // Get total count for logging
            long totalMaps = mindmapManager.countAllPublicMindmaps();
            logger.info("Starting spam detection for {} public maps in batches of {}", totalMaps, batchSize);
            
            int processedCount = 0;
            int spamDetectedCount = 0;
            int disabledAccountCount = 0;
            int offset = 0;
            
            // Process maps in batches
            while (offset < totalMaps) {
                List<Mindmap> publicMaps = mindmapManager.findAllPublicMindmaps(offset, batchSize);
                
                if (publicMaps.isEmpty()) {
                    break; // No more maps to process
                }
                
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
                    if (spamDetectionService.isSpamContent(mindmap)) {
                        // Mark as spam but keep it public
                        mindmap.setSpamDetected(true);
                        mindmapManager.updateMindmap(mindmap, false);
                        
                        spamDetectedCount++;
                        logger.warn("Marked public mindmap '{}' (ID: {}) as spam", 
                            mindmap.getTitle(), mindmap.getId());
                    }
                    
                    processedCount++;
                }
                
                offset += batchSize;
                logger.debug("Processed batch: offset={}, batchSize={}, totalProcessed={}", 
                    offset - batchSize, batchSize, processedCount);
            }

            logger.info("Spam detection batch task completed. Processed {} public maps, marked {} as spam, made {} private due to disabled accounts", 
                processedCount, spamDetectedCount, disabledAccountCount);

        } catch (Exception e) {
            logger.error("Error during spam detection batch task", e);
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
