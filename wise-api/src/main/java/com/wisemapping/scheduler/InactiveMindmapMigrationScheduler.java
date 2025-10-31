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

import com.wisemapping.service.InactiveMindmapMigrationService;
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

/**
 * Scheduler for migrating mindmaps from inactive users to a separate table.
 * Runs on weekends to move mindmaps of inactive users to make them inaccessible.
 */
@Component
@ConditionalOnProperty(name = "app.batch.inactive-mindmap-migration.enabled", havingValue = "true", matchIfMissing = true)
public class InactiveMindmapMigrationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InactiveMindmapMigrationScheduler.class);

    @Autowired
    private InactiveMindmapMigrationService inactiveMindmapMigrationService;

    @Value("${app.batch.inactive-mindmap-migration.startup-enabled:false}")
    private boolean startupEnabled;

    /**
     * Execute inactive mindmap migration task once at application startup (async to not block startup)
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    @ConditionalOnProperty(name = "app.batch.inactive-mindmap-migration.startup-enabled", havingValue = "true", matchIfMissing = false)
    public void processInactiveMindmapMigrationOnStartup() {
        // Double check: verify startup-enabled property is actually true at runtime
        if (!startupEnabled) {
            logger.warn("Startup task enabled but startupEnabled property is false - this should not happen!");
            return;
        }
        
        logger.info("Executing inactive mindmap migration task on application startup - startupEnabled={}", startupEnabled);
        
        try {
            inactiveMindmapMigrationService.processInactiveMindmapMigration();
            logger.info("Startup inactive mindmap migration task completed.");
        } catch (Exception e) {
            logger.error("Startup inactive mindmap migration task failed", e);
        }
    }

    /**
     * Scheduled task that runs on weekends to migrate mindmaps from inactive users.
     * 
     * Cron expression: "0 0 2 * * SUN" means:
     * - 0 seconds
     * - 0 minutes  
     * - 2 hours (2:00 AM)
     * - Every day of month
     * - Every month
     * - Sunday only
     */
    @Scheduled(cron = "${app.batch.inactive-mindmap-migration.cron-expression:0 0 2 * * SUN}")
    @Async
    public void processInactiveMindmapMigration() {
        logger.info("Starting scheduled inactive mindmap migration task (async)");
        
        try {
            inactiveMindmapMigrationService.processInactiveMindmapMigration();
            logger.info("Scheduled inactive mindmap migration task completed successfully");
        } catch (Exception e) {
            logger.error("Scheduled inactive mindmap migration task failed", e);
        }
    }
}
