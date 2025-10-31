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

package com.wisemapping.scheduler;

import com.wisemapping.service.SpamDetectionBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.spam-detection.enabled", havingValue = "true", matchIfMissing = true)
public class SpamDetectionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SpamDetectionScheduler.class);

    @Autowired
    private SpamDetectionBatchService spamDetectionBatchService;

    /**
     * Execute spam detection task once at application startup (async to not block startup)
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    @ConditionalOnProperty(name = "app.batch.spam-detection.startup-enabled", havingValue = "true", matchIfMissing = false)
    public void processSpamDetectionOnStartup() {
        logger.info("Executing spam detection task on application startup.");
        
        try {
            spamDetectionBatchService.processPublicMapsSpamDetection();
            logger.info("Startup spam detection task completed.");
        } catch (Exception e) {
            logger.error("Startup spam detection task failed", e);
        }
    }

    /**
     * Scheduled task that runs every 24 hours to check all public maps for spam content
     * and mark them as spam if detected.
     *
     * Cron expression: "0 0 0 * * *" means:
     * - 0 seconds
     * - 0 minutes
     * - 0 hours (midnight)
     * - Every day of month
     * - Every month
     * - Every day of week
     */
    @Scheduled(cron = "${app.batch.spam-detection.cron-expression:0 0 0 * * *}")
    @Async
    public void processSpamDetection() {
        logger.info("Executing scheduled spam detection task for public maps (async)...");
        
        try {
            spamDetectionBatchService.processPublicMapsSpamDetection();
            logger.info("Scheduled spam detection task completed.");
        } catch (Exception e) {
            logger.error("Scheduled spam detection task failed", e);
        }
    }
}
