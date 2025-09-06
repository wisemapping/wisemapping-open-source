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

package com.wisemapping.scheduler;

import com.wisemapping.service.SpamUserSuspensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SpamUserSuspensionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SpamUserSuspensionScheduler.class);

    @Autowired
    private SpamUserSuspensionService spamUserSuspensionService;

    @Value("${app.batch.spam-user-suspension.enabled:true}")
    private boolean enabled;

    /**
     * Scheduled task that runs every 6 hours to check for users with multiple spam mindmaps
     * and suspend them if necessary.
     * 
     * Cron expression: "0 0 /6 * * *" means:
     * - 0 seconds
     * - 0 minutes  
     * - Every 6 hours
     * - Every day of month
     * - Every month
     * - Every day of week
     */
    @Scheduled(cron = "${app.batch.spam-user-suspension.cron-expression:0 0 */6 * * *}")
    @Async
    public void processSpamUserSuspension() {
        if (!enabled) {
            logger.debug("Spam user suspension scheduler is disabled");
            return;
        }

        logger.info("Starting scheduled spam user suspension task (async)");
        
        try {
            spamUserSuspensionService.processSpamUserSuspension();
            logger.info("Scheduled spam user suspension task completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled spam user suspension task failed", e);
        }
    }
}
