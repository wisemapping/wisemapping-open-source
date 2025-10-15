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

import com.wisemapping.dao.MindmapManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.SpamUserResult;
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

    @Value("${app.batch.spam-user-suspension.months-back:36}")
    private int monthsBack;

    @Value("${app.batch.spam-user-suspension.batch-size:50}")
    private int batchSize;

    @Value("${app.batch.spam-user-suspension.min-total-maps:6}")
    private int minTotalMaps;

    @Value("${app.batch.spam-user-suspension.min-spam-count:3}")
    private int minSpamCount;

    /**
     * Process users with multiple spam mindmaps and suspend them if necessary
     * Suspends users who have more than minTotalMaps public maps AND at least minSpamCount spam maps
     * Each batch is processed in its own transaction to avoid long-running transactions
     */
    public void processSpamUserSuspension() {
        if (!enabled) {
            logger.debug("Spam user suspension batch task is disabled");
            return;
        }

        try {
            long totalUsers = getTotalUsersWithMinimumMapsAndSpam();
            
            // Quick summary line for easy log searching
            logger.info("🚀 SPAM SUSPENSION STARTED: {} users to process", totalUsers);
            
            logger.info("\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "SPAM USER SUSPENSION TASK STARTED\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "START SUMMARY:\n" +
                "  → Total Users to Process: {}\n" +
                "  → Accounts Created Since: Last {} months (3 years)\n" +
                "\n" +
                "Suspension Criteria:\n" +
                "  → Minimum Total Public Maps: >{}\n" +
                "  → Minimum Spam Maps: >={}\n" +
                "  → Batch Size: {}\n" +
                "════════════════════════════════════════════════════════════════",
                totalUsers, monthsBack, minTotalMaps, minSpamCount, batchSize);

            int suspendedCount = 0;
            int offset = 0;
            int batchNumber = 0;

            while (offset < totalUsers) {
                try {
                    batchNumber++;
                    logger.info("Processing batch #{} (offset: {}, size: {})", batchNumber, offset, batchSize);
                    
                    int batchSuspendedCount = processMinimumMapsBatch(offset, batchSize);
                    suspendedCount += batchSuspendedCount;

                    if (batchSuspendedCount == 0) {
                        break; // No more users to process
                    }

                    offset += batchSize;
                } catch (Exception e) {
                    logger.error("Error processing batch #{} at offset {}: {}", batchNumber, offset, e.getMessage(), e);
                    // Continue with next batch instead of failing completely
                    offset += batchSize;
                }
            }

            // Quick summary line for easy log searching
            logger.info("✅ SPAM SUSPENSION COMPLETED: {} users processed, {} users suspended", totalUsers, suspendedCount);
            
            // Final summary
            logger.info("\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "SPAM USER SUSPENSION TASK COMPLETED\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "END SUMMARY:\n" +
                "  ✓ Total Users Processed: {} users\n" +
                "  ✓ Total Users Suspended: {} users\n" +
                "  ✓ Success Rate: {}%\n" +
                "  ✓ Total Batches: {}\n" +
                "\n" +
                "Applied Criteria:\n" +
                "  → >{}  total public maps AND >={} spam maps\n" +
                "  → Account age: Last {} months (3 years)\n" +
                "════════════════════════════════════════════════════════════════",
                totalUsers, 
                suspendedCount, 
                totalUsers > 0 ? String.format("%.1f", (suspendedCount * 100.0 / totalUsers)) : "0.0",
                batchNumber, 
                minTotalMaps, 
                minSpamCount, 
                monthsBack);

        } catch (Exception e) {
            logger.error("Error during spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Process a single batch of users for minimum maps-based suspension in its own transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processMinimumMapsBatch(int offset, int batchSize) {
        List<SpamUserResult> usersWithSpamMaps = mindmapManager.findUsersWithMinimumMapsAndSpam(
            minTotalMaps, minSpamCount, monthsBack, offset, batchSize);

        if (usersWithSpamMaps.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        int skippedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();
        StringBuilder suspensionSummary = new StringBuilder();
        suspensionSummary.append("\n========================================\n");
        suspensionSummary.append("USER SUSPENSION BATCH REPORT\n");
        suspensionSummary.append("========================================\n");

        for (SpamUserResult result : usersWithSpamMaps) {
            try {
                Account user = result.getUser();
                long spamCount = result.getSpamCount();

                if (user.isSuspended()) {
                    logger.debug("User {} is already suspended. Skipping.", user.getEmail());
                    skippedCount++;
                    continue;
                }

                // Suspend the user
                user.suspend(SuspensionReason.ABUSE);
                usersToSuspend.add(user);
                
                // Track user suspension
                metricsService.trackUserSuspension(user, "ABUSE");

                suspendedCount++;
                
                // Detailed log for each suspended user
                String suspensionDetail = String.format(
                    "SUSPENDED USER #%d:\n" +
                    "  - User ID: %d\n" +
                    "  - Email: %s\n" +
                    "  - Full Name: %s\n" +
                    "  - Account Created: %s\n" +
                    "  - Spam Maps Count: %d\n" +
                    "  - Suspension Reason: ABUSE\n" +
                    "  - Criteria: Has >%d total public maps AND >=%d spam maps\n",
                    suspendedCount,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getCreationDate() != null ? user.getCreationDate().getTime() : "Unknown",
                    spamCount,
                    minTotalMaps,
                    minSpamCount
                );
                
                logger.info(suspensionDetail);
                suspensionSummary.append(suspensionDetail).append("\n");
                
            } catch (Exception e) {
                logger.error("Error suspending user {}: {}", result.getUser().getEmail(), e.getMessage(), e);
                // Continue processing other users in the batch
            }
        }
        
        // Update all users in a single transaction if there are any updates
        if (!usersToSuspend.isEmpty()) {
            updateUsersInTransaction(usersToSuspend);
        }

        // Summary log
        suspensionSummary.append("========================================\n");
        suspensionSummary.append(String.format("BATCH SUMMARY:\n"));
        suspensionSummary.append(String.format("  - Total Users Processed: %d\n", usersWithSpamMaps.size()));
        suspensionSummary.append(String.format("  - Users Suspended: %d\n", suspendedCount));
        suspensionSummary.append(String.format("  - Users Skipped (already suspended): %d\n", skippedCount));
        suspensionSummary.append(String.format("  - Batch Offset: %d\n", offset));
        suspensionSummary.append("========================================\n");
        
        logger.info(suspensionSummary.toString());

        return suspendedCount;
    }

    /**
     * Get total count of users with minimum maps and spam (transactional)
     */
    @Transactional(readOnly = true)
    public long getTotalUsersWithMinimumMapsAndSpam() {
        return mindmapManager.countUsersWithMinimumMapsAndSpam(minTotalMaps, minSpamCount, monthsBack);
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
     * Get the minimum total maps threshold
     */
    public int getMinTotalMaps() {
        return minTotalMaps;
    }

    /**
     * Get the minimum spam count threshold
     */
    public int getMinSpamCount() {
        return minSpamCount;
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
