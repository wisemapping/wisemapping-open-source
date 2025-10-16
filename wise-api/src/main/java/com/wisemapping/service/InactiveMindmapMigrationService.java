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

import com.wisemapping.dao.InactiveMindmapManager;
import com.wisemapping.dao.MindmapManager;
import com.wisemapping.dao.UserManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.InactiveMindmap;
import com.wisemapping.model.Mindmap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.List;

/**
 * Service for migrating mindmaps of inactive users to a separate table.
 * This makes the mindmaps inaccessible through normal application flows.
 */
@Service
public class InactiveMindmapMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(InactiveMindmapMigrationService.class);

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private InactiveMindmapManager inactiveMindmapManager;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.batch.inactive-mindmap-migration.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.inactive-mindmap-migration.batch-size:25}")
    private int batchSize;

    @Value("${app.batch.inactive-mindmap-migration.mindmap-batch-size:10}")
    private int mindmapBatchSize;

    @Value("${app.batch.inactive-mindmap-migration.dry-run:false}")
    private boolean dryRun;

    @Value("${app.batch.inactive-mindmap-migration.minimum-suspension-days:30}")
    private int minimumSuspensionDays;

    /**
     * Process migration of mindmaps from inactive users.
     * Each batch is processed in its own transaction to avoid long-running transactions.
     */
    public void processInactiveMindmapMigration() {
        if (!enabled) {
            logger.debug("Inactive mindmap migration batch task is disabled");
            return;
        }

        logger.info("Starting inactive mindmap migration process - user batch size: {}, mindmap batch size: {}, dry run: {}", 
                   batchSize, mindmapBatchSize, dryRun);

        // Process suspended users in memory-efficient batches
        int offset = 0;
        int totalMigrated = 0;
        int totalUsersProcessed = 0;
        int batchNumber = 1;

        logger.info("Starting memory-efficient batch processing of users suspended for inactivity (batch size: {})", batchSize);

        while (true) {
            // Load only the current batch of users suspended for inactivity to minimize memory usage
            List<Account> batch = userManager.findUsersSuspendedForInactivity(offset, batchSize);
            
            if (batch.isEmpty()) {
                break; // No more users to process
            }

            logger.debug("Processing batch {}: {} users suspended for inactivity (offset: {})", batchNumber, batch.size(), offset);

            int batchMigrated = processBatch(batch);
            totalMigrated += batchMigrated;
            totalUsersProcessed += batch.size();

            // Flush changes to database after each batch to ensure persistence and free memory
            entityManager.flush();
            entityManager.clear(); // Clear the persistence context to free memory

            logger.debug("Completed batch {}: migrated {} mindmaps from {} users", 
                        batchNumber, batchMigrated, batch.size());

            // Move to next batch
            offset += batchSize;
            batchNumber++;
        }

        if (totalUsersProcessed == 0) {
            logger.info("No users suspended for inactivity found for mindmap migration");
            return;
        }

        logger.info("Inactive mindmap migration process completed - Total users processed: {}, Total mindmaps migrated: {}", 
                   totalUsersProcessed, totalMigrated);

        // Track telemetry
        metricsService.trackInactiveMindmapMigration(totalUsersProcessed, totalMigrated);
    }

    /**
     * Process a batch of suspended users and migrate their mindmaps.
     * Only processes users who have been suspended for at least the configured minimum days.
     * @param users batch of suspended users
     * @return number of mindmaps migrated in this batch
     */
    public int processBatch(List<Account> users) {
        int batchMigrated = 0;
        
        // Calculate the minimum suspension date based on configuration
        Calendar minimumSuspensionDate = Calendar.getInstance();
        minimumSuspensionDate.add(Calendar.DAY_OF_MONTH, -minimumSuspensionDays);

        for (Account user : users) {
            try {
                // Check if user has been suspended for at least the configured minimum days
                if (user.getSuspendedDate() == null || user.getSuspendedDate().after(minimumSuspensionDate)) {
                    logger.debug("Skipping user {} - suspended for less than {} days (suspended: {})", 
                               user.getEmail(), minimumSuspensionDays, user.getSuspendedDate());
                    continue;
                }
                
                // Use MindmapManager to find all mindmaps created by this user
                List<Mindmap> userMindmaps = mindmapManager.findByCreator(user.getId());
                
                if (userMindmaps.isEmpty()) {
                    logger.debug("User {} has no mindmaps to migrate", user.getEmail());
                    continue;
                }

                logger.debug("Migrating {} mindmaps for user {} (ID: {}) in batches of {}", 
                           userMindmaps.size(), user.getEmail(), user.getId(), mindmapBatchSize);

                // Process mindmaps in smaller batches to manage memory usage
                int userMigrated = processUserMindmapsInBatches(user, userMindmaps);
                batchMigrated += userMigrated;

                logger.info("Migrated {} mindmaps for inactive user: email={}, id={}, creationDate={}", 
                           userMindmaps.size(), user.getEmail(), user.getId(), user.getCreationDate());

            } catch (Exception e) {
                logger.error("Failed to migrate mindmaps for user: {} (ID: {}) - continuing with batch", 
                           user.getEmail(), user.getId(), e);
            }
        }

        return batchMigrated;
    }

    /**
     * Process mindmaps for a single user in smaller batches to manage memory usage.
     * @param user the user whose mindmaps are being migrated
     * @param userMindmaps list of mindmaps to migrate
     * @return number of mindmaps migrated for this user
     */
    private int processUserMindmapsInBatches(Account user, List<Mindmap> userMindmaps) {
        int userMigrated = 0;
        
        // Process mindmaps in smaller batches to manage memory
        for (int i = 0; i < userMindmaps.size(); i += mindmapBatchSize) {
            int endIndex = Math.min(i + mindmapBatchSize, userMindmaps.size());
            List<Mindmap> mindmapBatch = userMindmaps.subList(i, endIndex);
            
            logger.debug("Processing mindmap batch {}/{} for user {} ({} mindmaps)", 
                        (i / mindmapBatchSize) + 1, 
                        (userMindmaps.size() + mindmapBatchSize - 1) / mindmapBatchSize,
                        user.getEmail(), mindmapBatch.size());
            
            for (Mindmap mindmap : mindmapBatch) {
                if (!dryRun) {
                    // Store mindmap ID and title for logging (entity may become detached)
                    final int mindmapId = mindmap.getId();
                    final String mindmapTitle = mindmap.getTitle();
                    
                    // Process migration in a separate transaction to ensure consistency
                    transactionTemplate.execute(status -> {
                        // Reload the mindmap within the transaction to avoid detached entity issues
                        Mindmap managedMindmap = mindmapManager.getMindmapById(mindmapId);
                        
                        // Create inactive mindmap record using InactiveMindmapManager
                        InactiveMindmap inactiveMindmap = new InactiveMindmap(
                            managedMindmap, 
                            "User suspended for at least " + minimumSuspensionDays + " days"
                        );
                        inactiveMindmapManager.addInactiveMindmap(inactiveMindmap);

                        // Remove the original mindmap using MindmapManager
                        mindmapManager.removeMindmap(managedMindmap);
                        
                        return null;
                    });
                    
                    userMigrated++;
                    
                    logger.debug("Migrated mindmap '{}' (ID: {}) for inactive user {}", 
                               mindmapTitle, mindmapId, user.getEmail());
                } else {
                    logger.debug("DRY RUN: Would migrate mindmap '{}' (ID: {}) for inactive user {}", 
                               mindmap.getTitle(), mindmap.getId(), user.getEmail());
                    userMigrated++;
                }
            }
            
            // Flush and clear after each mindmap batch to free memory
            entityManager.flush();
            entityManager.clear();
        }
        
        return userMigrated;
    }

    /**
     * Restore mindmaps for a reactivated user by moving them back from inactive table to active table.
     * This method should be called when an admin reactivates a previously suspended user.
     * 
     * @param user the user being reactivated
     * @return number of mindmaps restored
     */
    @Transactional
    public int restoreUserMindmaps(@NotNull Account user) {
        assert user != null : "user is null";
        
        logger.info("Restoring mindmaps for reactivated user: email={}, id={}", user.getEmail(), user.getId());

        // Find all inactive mindmaps for this user
        List<InactiveMindmap> inactiveMindmaps = inactiveMindmapManager.findByCreator(user);
        
        if (inactiveMindmaps.isEmpty()) {
            logger.debug("User {} has no inactive mindmaps to restore", user.getEmail());
            return 0;
        }

        int restoredCount = 0;
        
        for (InactiveMindmap inactiveMindmap : inactiveMindmaps) {
            try {
                // Create a new active mindmap from the inactive one
                Mindmap restoredMindmap = createMindmapFromInactive(inactiveMindmap, user);
                
                // Save the restored mindmap using MindmapManager
                mindmapManager.addMindmap(user, restoredMindmap);
                
                // Remove the inactive mindmap record
                inactiveMindmapManager.removeInactiveMindmap(inactiveMindmap);
                
                restoredCount++;
                
                logger.debug("Restored mindmap '{}' (original ID: {}) for reactivated user {}", 
                           restoredMindmap.getTitle(), inactiveMindmap.getOriginalMindmapId(), user.getEmail());

            } catch (Exception e) {
                logger.error("Failed to restore mindmap '{}' for user {} - continuing with other mindmaps", 
                           inactiveMindmap.getTitle(), user.getEmail(), e);
            }
        }

        logger.info("Restored {} mindmaps for reactivated user: email={}, id={}", 
                   restoredCount, user.getEmail(), user.getId());

        // Track telemetry for restoration
        if (restoredCount > 0) {
            metricsService.trackInactiveMindmapMigration(1, -restoredCount); // Negative count indicates restoration
        }

        return restoredCount;
    }

    /**
     * Create a new active Mindmap from an InactiveMindmap record.
     * 
     * @param inactiveMindmap the inactive mindmap to restore
     * @param user the user being reactivated
     * @return a new active Mindmap
     */
    private Mindmap createMindmapFromInactive(InactiveMindmap inactiveMindmap, Account user) {
        Mindmap mindmap = new Mindmap();
        
        // Copy all the data from the inactive mindmap
        mindmap.setTitle(inactiveMindmap.getTitle());
        mindmap.setDescription(inactiveMindmap.getDescription());
        mindmap.setPublic(inactiveMindmap.isPublic());
        mindmap.setCreator(user); // Use the reactivated user as creator
        mindmap.setLastEditor(user); // Use the reactivated user as last editor
        mindmap.setCreationTime(inactiveMindmap.getCreationTime());
        mindmap.setLastModificationTime(Calendar.getInstance()); // Update modification time to now
        mindmap.setZippedXml(inactiveMindmap.getZippedXml());
        
        return mindmap;
    }

    /**
     * Get statistics about suspended users and migrated mindmaps.
     * @return migration statistics
     */
    public MigrationStats getMigrationStats() {
        // Calculate the minimum suspension date based on configuration
        Calendar minimumSuspensionDate = Calendar.getInstance();
        minimumSuspensionDate.add(Calendar.DAY_OF_MONTH, -minimumSuspensionDays);

        // Count users suspended for inactivity who have been suspended for at least the configured minimum days
        List<Account> allSuspendedUsers = userManager.findUsersSuspendedForInactivity(0, Integer.MAX_VALUE);
        long eligibleSuspendedUsersCount = allSuspendedUsers.stream()
                .filter(user -> user.getSuspendedDate() != null && !user.getSuspendedDate().after(minimumSuspensionDate))
                .count();
        
        long migratedMindmapsCount = inactiveMindmapManager.countAllInactiveMindmaps();

        return new MigrationStats(eligibleSuspendedUsersCount, migratedMindmapsCount, minimumSuspensionDate);
    }

    /**
     * Statistics class for migration information.
     */
    public static class MigrationStats {
        private final long inactiveUsersCount;
        private final long inactiveMindmapsCount;
        private final Calendar cutoffDate;

        public MigrationStats(long inactiveUsersCount, long inactiveMindmapsCount, Calendar cutoffDate) {
            this.inactiveUsersCount = inactiveUsersCount;
            this.inactiveMindmapsCount = inactiveMindmapsCount;
            this.cutoffDate = cutoffDate;
        }

        public long getInactiveUsersCount() {
            return inactiveUsersCount;
        }

        public long getInactiveMindmapsCount() {
            return inactiveMindmapsCount;
        }

        public Calendar getCutoffDate() {
            return cutoffDate;
        }

        @Override
        public String toString() {
            return "MigrationStats{" +
                    "inactiveUsersCount=" + inactiveUsersCount +
                    ", inactiveMindmapsCount=" + inactiveMindmapsCount +
                    ", cutoffDate=" + cutoffDate +
                    '}';
        }
    }
}
