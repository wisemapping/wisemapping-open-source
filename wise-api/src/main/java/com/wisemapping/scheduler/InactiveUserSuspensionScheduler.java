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

import com.wisemapping.service.InactiveUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.inactive-user-suspension.enabled", havingValue = "true", matchIfMissing = true)
public class InactiveUserSuspensionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InactiveUserSuspensionScheduler.class);

    @Autowired
    private InactiveUserService inactiveUserService;

    @Value("${app.batch.inactive-user-suspension.preview-enabled:false}")
    private boolean previewEnabled;

    /**
     * Kick off the inactive user suspension process once the application is ready.
     * Runs asynchronously so it will not block application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    @ConditionalOnProperty(name = "app.batch.inactive-user-suspension.startup-enabled", havingValue = "true", matchIfMissing = false)
    public void processInactiveUserSuspensionOnStartup() {
        logger.info("Starting startup inactive user suspension task (async)");
        try {
            inactiveUserService.processInactiveUsers();
            logger.info("Startup inactive user suspension task completed successfully");
        } catch (Exception e) {
            logger.error("Startup inactive user suspension task failed", e);
        }
    }

    /**
     * Scheduled task that runs every Saturday at 02:00 AM to suspend inactive users.
     * 
     * Cron expression: "0 0 2 * * SAT" means:
     * - 0 seconds
     * - 0 minutes  
     * - 2 hours (2:00 AM)
     * - Every day of month
     * - Every month
     * - Saturday only
     */
    @Scheduled(cron = "${app.batch.inactive-user-suspension.cron-expression:0 0 2 * * SAT}")
    @Async
    public void processInactiveUserSuspension() {
        logger.info("Starting scheduled inactive user suspension task (async)");
        
        try {
            inactiveUserService.processInactiveUsers();
            logger.info("Scheduled inactive user suspension task completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled inactive user suspension task failed", e);
        }
    }

    /**
     * Optional preview task that runs every Friday at 10:00 AM to show what users would be suspended.
     * This helps administrators preview the impact before the actual suspension runs.
     * 
     * Cron expression: "0 0 10 * * FRI" means:
     * - 0 seconds
     * - 0 minutes  
     * - 10 hours (10:00 AM)
     * - Every day of month
     * - Every month
     * - Friday only
     */
    @Scheduled(cron = "${app.batch.inactive-user-suspension.preview-cron-expression:0 0 10 * * FRI}")
    @Async
    @ConditionalOnProperty(name = "app.batch.inactive-user-suspension.preview-enabled", havingValue = "true")
    public void previewInactiveUserSuspension() {
        logger.info("Starting scheduled inactive user suspension preview (async)");
        
        try {
            inactiveUserService.previewInactiveUsers();
            logger.info("Scheduled inactive user suspension preview completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled inactive user suspension preview failed", e);
        }
    }
}