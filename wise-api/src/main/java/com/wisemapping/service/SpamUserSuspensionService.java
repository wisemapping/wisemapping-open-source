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
import com.wisemapping.model.Account;
import com.wisemapping.model.SpamUserResult;
import com.wisemapping.model.SpamRatioUserResult;
import com.wisemapping.model.SuspensionReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpamUserSuspensionService {

    private static final Logger logger = LoggerFactory.getLogger(SpamUserSuspensionService.class);

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MetricsService metricsService;

    @Value("${app.batch.spam-user-suspension.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.spam-user-suspension.spam-threshold:2}")
    private int spamThreshold;

    @Value("${app.batch.spam-user-suspension.months-back:3}")
    private int monthsBack;

    @Value("${app.batch.spam-user-suspension.batch-size:50}")
    private int batchSize;

    @Value("${app.batch.spam-user-suspension.min-spam-count:3}")
    private int minSpamCount;

    @Value("${app.batch.spam-user-suspension.spam-ratio-threshold:0.5}")
    private double spamRatioThreshold;

    @Value("${app.batch.spam-user-suspension.use-ratio-based:true}")
    private boolean useRatioBased;

    /**
     * Process users with multiple spam mindmaps and suspend them if necessary
     * Each batch is processed in its own transaction to avoid long-running transactions
     */
    public void processSpamUserSuspension() {
        if (!enabled) {
            logger.debug("Spam user suspension batch task is disabled");
            return;
        }

        if (useRatioBased) {
            processSpamUserSuspensionByRatio();
        } else {
            processSpamUserSuspensionByCount();
        }
    }

    /**
     * Process users using ratio-based suspension (spam public maps / total public maps)
     * Each batch is processed in its own transaction to avoid long-running transactions
     * This method itself is not transactional to avoid connection leaks
     */
    public void processSpamUserSuspensionByRatio() {
        logger.info("Starting ratio-based spam user suspension batch task with min spam count: {}, ratio threshold: {}%, and months back: {} (public maps only)", 
            minSpamCount, spamRatioThreshold * 100, monthsBack);

        try {
            long totalUsers = getTotalUsersWithHighSpamRatio();
            logger.info("Starting ratio-based spam user suspension for {} users in batches of {}", totalUsers, batchSize);

            int suspendedCount = 0;
            int offset = 0;

            while (offset < totalUsers) {
                try {
                    int batchSuspendedCount = processRatioBatch(offset, batchSize);
                    suspendedCount += batchSuspendedCount;

                    if (batchSuspendedCount == 0) {
                        break; // No more users to process
                    }

                    offset += batchSize;
                    logger.debug("Processed batch: offset={}, batchSize={}, totalSuspended={}",
                        offset - batchSize, batchSize, suspendedCount);
                } catch (Exception e) {
                    logger.error("Error processing ratio batch at offset {}: {}", offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += batchSize;
                }
            }

            logger.info("Ratio-based spam user suspension batch task completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during ratio-based spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Process a single batch of users for ratio-based suspension in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processRatioBatch(int offset, int batchSize) {
        List<SpamRatioUserResult> usersWithHighSpamRatio = mindmapManager.findUsersWithHighSpamRatio(
            minSpamCount, spamRatioThreshold, monthsBack, offset, batchSize);

        if (usersWithHighSpamRatio.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();

        for (SpamRatioUserResult result : usersWithHighSpamRatio) {
            try {
                Account user = result.getUser();
                long spamCount = result.getSpamCount();
                long totalCount = result.getTotalCount();
                double spamRatio = result.getSpamRatio();

                if (user.isSuspended()) {
                    logger.debug("User {} is already suspended. Skipping.", user.getEmail());
                    continue;
                }

                // Suspend the user
                user.suspend(SuspensionReason.ABUSE);
                usersToSuspend.add(user);
                
                // Track user suspension
                metricsService.trackUserSuspension(user, "ABUSE");

                suspendedCount++;
                logger.warn("Suspended user {} (created: {}) due to {} spam public mindmaps out of {} total public ({}% spam ratio)",
                    user.getEmail(), user.getCreationDate(), spamCount, totalCount, 
                    String.format("%.1f", spamRatio * 100));
            } catch (Exception e) {
                logger.error("Error suspending user {}: {}", result.getUser().getEmail(), e.getMessage(), e);
                // Continue processing other users in the batch
            }
        }
        
        // Update all users in a single transaction if there are any updates
        if (!usersToSuspend.isEmpty()) {
            updateUsersInTransaction(usersToSuspend);
        }

        return suspendedCount;
    }

    /**
     * Process users using count-based suspension (legacy method) - public maps only
     * Each batch is processed in its own transaction to avoid long-running transactions
     * This method itself is not transactional to avoid connection leaks
     */
    public void processSpamUserSuspensionByCount() {
        logger.info("Starting count-based spam user suspension batch task with threshold: {} and months back: {} (public maps only)", spamThreshold, monthsBack);

        try {
            long totalUsers = getTotalUsersWithSpamMindmaps();
            logger.info("Starting count-based spam user suspension for {} users in batches of {}", totalUsers, batchSize);

            int suspendedCount = 0;
            int offset = 0;

            while (offset < totalUsers) {
                try {
                    int batchSuspendedCount = processCountBatch(offset, batchSize);
                    suspendedCount += batchSuspendedCount;

                    if (batchSuspendedCount == 0) {
                        break; // No more users to process
                    }

                    offset += batchSize;
                    logger.debug("Processed batch: offset={}, batchSize={}, totalSuspended={}",
                        offset - batchSize, batchSize, suspendedCount);
                } catch (Exception e) {
                    logger.error("Error processing count batch at offset {}: {}", offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += batchSize;
                }
            }

            logger.info("Count-based spam user suspension batch task completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during count-based spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Process a single batch of users for count-based suspension in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processCountBatch(int offset, int batchSize) {
        List<SpamUserResult> usersWithSpamMindmaps = mindmapManager.findUsersWithSpamMindaps(spamThreshold, monthsBack, offset, batchSize);

        if (usersWithSpamMindmaps.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();

        for (SpamUserResult result : usersWithSpamMindmaps) {
            try {
                Account user = result.getUser();
                long spamCount = result.getSpamCount();

                if (user.isSuspended()) {
                    logger.debug("User {} is already suspended. Skipping.", user.getEmail());
                    continue;
                }

                // Suspend the user
                user.suspend(SuspensionReason.ABUSE);
                usersToSuspend.add(user);
                
                // Track user suspension
                metricsService.trackUserSuspension(user, "ABUSE");

                suspendedCount++;
                logger.warn("Suspended user {} (created: {}) due to {} spam public mindmaps",
                    user.getEmail(), user.getCreationDate(), spamCount);
            } catch (Exception e) {
                logger.error("Error suspending user {}: {}", result.getUser().getEmail(), e.getMessage(), e);
                // Continue processing other users in the batch
            }
        }
        
        // Update all users in a single transaction if there are any updates
        if (!usersToSuspend.isEmpty()) {
            updateUsersInTransaction(usersToSuspend);
        }

        return suspendedCount;
    }

    /**
     * Get the current spam threshold
     */
    public int getSpamThreshold() {
        return spamThreshold;
    }

    /**
     * Check if the service is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the months back configuration
     */
    public int getMonthsBack() {
        return monthsBack;
    }

    /**
     * Get the batch size configuration
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Get the minimum spam count for ratio-based suspension
     */
    public int getMinSpamCount() {
        return minSpamCount;
    }

    /**
     * Get the spam ratio threshold (0.0 to 1.0)
     */
    public double getSpamRatioThreshold() {
        return spamRatioThreshold;
    }

    /**
     * Check if ratio-based suspension is enabled
     */
    public boolean isUseRatioBased() {
        return useRatioBased;
    }

    /**
     * Get total count of users with high spam ratio (transactional)
     */
    @Transactional(readOnly = true)
    public long getTotalUsersWithHighSpamRatio() {
        return mindmapManager.countUsersWithHighSpamRatio(minSpamCount, spamRatioThreshold, monthsBack);
    }

    /**
     * Get total count of users with spam mindmaps (transactional)
     */
    @Transactional(readOnly = true)
    public long getTotalUsersWithSpamMindmaps() {
        return mindmapManager.countUsersWithSpamMindaps(spamThreshold, monthsBack);
    }

    /**
     * Suspend users who have public maps marked as spam by specific detection strategies
     * @param spamTypeCodes array of spam type codes to filter by (e.g., "FewNodesWithContent", "UserBehavior")
     * @param monthsBack number of months to look back for account creation
     * @param suspensionReason the reason for suspension
     * @return number of users suspended
     */
    public int suspendUsersWithPublicSpamMapsByType(String[] spamTypeCodes, int monthsBack, SuspensionReason suspensionReason) {
        if (!enabled) {
            logger.debug("Spam user suspension is disabled");
            return 0;
        }

        if (spamTypeCodes == null || spamTypeCodes.length == 0) {
            logger.warn("No spam type codes provided for suspension");
            return 0;
        }

        logger.info("Starting suspension of users with public spam maps by type: {} (months back: {})", 
            String.join(", ", spamTypeCodes), monthsBack);

        try {
            long totalUsers = mindmapManager.countUsersWithPublicSpamMapsByType(spamTypeCodes, monthsBack);
            logger.info("Found {} users with public spam maps of specified types", totalUsers);

            if (totalUsers == 0) {
                return 0;
            }

            int suspendedCount = 0;
            int offset = 0;

            while (offset < totalUsers) {
                try {
                    int batchSuspendedCount = processSpamTypeBatch(spamTypeCodes, monthsBack, offset, batchSize, suspensionReason);
                    suspendedCount += batchSuspendedCount;

                    if (batchSuspendedCount == 0) {
                        break; // No more users to process
                    }

                    offset += batchSize;
                    logger.debug("Processed batch: offset={}, batchSize={}, totalSuspended={}",
                        offset - batchSize, batchSize, suspendedCount);
                } catch (Exception e) {
                    logger.error("Error processing spam type batch at offset {}: {}", offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += batchSize;
                }
            }

            logger.info("Spam type-based user suspension completed. Suspended {} users", suspendedCount);
            return suspendedCount;

        } catch (Exception e) {
            logger.error("Error during spam type-based user suspension", e);
            throw e;
        }
    }

    /**
     * Process a single batch of users for spam type-based suspension in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processSpamTypeBatch(String[] spamTypeCodes, int monthsBack, int offset, int batchSize, SuspensionReason suspensionReason) {
        List<SpamUserResult> usersWithSpamMaps = mindmapManager.findUsersWithPublicSpamMapsByType(spamTypeCodes, monthsBack, offset, batchSize);

        if (usersWithSpamMaps.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();

        for (SpamUserResult result : usersWithSpamMaps) {
            try {
                Account user = result.getUser();
                long spamCount = result.getSpamCount();

                if (user.isSuspended()) {
                    logger.debug("User {} is already suspended. Skipping.", user.getEmail());
                    continue;
                }

                // Suspend the user
                user.suspend(suspensionReason);
                usersToSuspend.add(user);
                
                // Track user suspension
                metricsService.trackUserSuspension(user, suspensionReason.name());

                suspendedCount++;
                logger.warn("Suspended user {} (created: {}) due to {} public spam mindmaps of types: {}",
                    user.getEmail(), user.getCreationDate(), spamCount, String.join(", ", spamTypeCodes));
            } catch (Exception e) {
                logger.error("Error suspending user {}: {}", result.getUser().getEmail(), e.getMessage(), e);
                // Continue processing other users in the batch
            }
        }
        
        // Update all users in a single transaction if there are any updates
        if (!usersToSuspend.isEmpty()) {
            updateUsersInTransaction(usersToSuspend);
        }

        return suspendedCount;
    }
    
    /**
     * Update multiple users in a single transaction to ensure proper transaction context
     * Uses TransactionTemplate to programmatically manage transactions in async context
     */
    public void updateUsersInTransaction(List<Account> users) {
        transactionTemplate.execute(status -> {
            try {
                for (Account user : users) {
                    userService.updateUser(user);
                }
                logger.debug("Successfully updated {} users in transaction", users.size());
                return null;
            } catch (Exception e) {
                logger.error("Error updating {} users in transaction: {}", users.size(), e.getMessage(), e);
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}
