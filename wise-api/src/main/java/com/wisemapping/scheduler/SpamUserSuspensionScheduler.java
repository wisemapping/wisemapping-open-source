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

import com.wisemapping.service.SpamUserSuspensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.spam-user-suspension.enabled", havingValue = "true", matchIfMissing = true)
public class SpamUserSuspensionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SpamUserSuspensionScheduler.class);

    @Autowired
    private SpamUserSuspensionService spamUserSuspensionService;

    @Value("${app.batch.spam-user-suspension.startup-enabled:false}")
    private boolean startupEnabled;

    /**
     * Execute spam user suspension task once at application startup (async to not block startup)
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void processSpamUserSuspensionOnStartup() {
        if (!startupEnabled) {
            return;
        }
        
        logger.info("Executing spam user suspension task on application startup");
        
        try {
            spamUserSuspensionService.processSpamUserSuspension();
            logger.info("Startup spam user suspension task completed.");
        } catch (Exception e) {
            logger.error("Startup spam user suspension task failed", e);
        }
    }

    /**
     * Scheduled task that runs once a day at 6 PM Argentina time to check for users with multiple spam mindmaps
     * and suspend them if necessary.
     * 
     * Cron expression: "0 0 18 * * *" means:
     * - 0 seconds
     * - 0 minutes  
     * - 18 hours (6 PM in Argentina timezone)
     * - Every day of month
     * - Every month
     * - Every day of week
     */
    @Scheduled(cron = "${app.batch.spam-user-suspension.cron-expression:0 0 18 * * *}", zone = "America/Argentina/Buenos_Aires")
    @Async
    public void processSpamUserSuspension() {
        logger.info("Starting scheduled spam user suspension task (async)");
        
        try {
            spamUserSuspensionService.processSpamUserSuspension();
            logger.info("Scheduled spam user suspension task completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled spam user suspension task failed", e);
        }
    }
}
