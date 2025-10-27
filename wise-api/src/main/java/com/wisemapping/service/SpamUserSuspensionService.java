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
import com.wisemapping.model.SpamRatioUserResult;
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

    @Value("${app.batch.spam-user-suspension.months-back:72}")
    private int monthsBack;

    @Value("${app.batch.spam-user-suspension.batch-size:50}")
    private int batchSize;

    @Value("${app.batch.spam-user-suspension.public-spam-ratio-threshold:0.75}")
    private double publicSpamRatioThreshold;

    @Value("${app.batch.spam-user-suspension.min-any-spam-count:6}")
    private int minAnySpamCount;

    /**
     * Process users with multiple spam mindmaps and suspend them if necessary
     * Suspends users based on two criteria:
     * 1. Users with >= 75% of public maps marked as spam
     * 2. Users with 6+ spam maps (public or private)
     * Each batch is processed in its own transaction to avoid long-running transactions
     */
    public void processSpamUserSuspension() {
        if (!enabled) {
            logger.debug("Spam user suspension batch task is disabled");
            return;
        }

        try {
            long totalUsersPublicSpamRatio = getTotalUsersWithPublicSpamRatio();
            long totalUsersAnySpam = getTotalUsersWithAnySpam();
            
            // Quick summary line for easy log searching
            logger.info("🚀 SPAM SUSPENSION STARTED: {} users with high public spam ratio, {} users with any spam", 
                totalUsersPublicSpamRatio, totalUsersAnySpam);
            
            logger.info("\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "SPAM USER SUSPENSION TASK STARTED\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "START SUMMARY:\n" +
                "  → Users with High Public Spam Ratio: {}\n" +
                "  → Users with Any Spam: {}\n" +
                "  → Accounts Created Since: Last {} months (6 years)\n" +
                "\n" +
                "Suspension Criteria:\n" +
                "  → Condition 1: >= {}% of public maps are spam\n" +
                "  → Condition 2: >= {} spam maps (public or private)\n" +
                "  → Batch Size: {}\n" +
                "════════════════════════════════════════════════════════════════",
                totalUsersPublicSpamRatio, totalUsersAnySpam, monthsBack, 
                (int)(publicSpamRatioThreshold * 100), minAnySpamCount, batchSize);

            int suspendedCountPublicRatio = 0;
            int suspendedCountAny = 0;

            // Process Condition 1: Users with >= 75% public spam ratio
            logger.info("\n▶ Processing Condition 1: Users with >= {}% public spam ratio", 
                (int)(publicSpamRatioThreshold * 100));
            suspendedCountPublicRatio = processPublicSpamRatioCondition(totalUsersPublicSpamRatio);

            // Process Condition 2: Users with 6+ spam maps (any visibility)
            logger.info("\n▶ Processing Condition 2: Users with >= {} spam maps (any visibility)", minAnySpamCount);
            suspendedCountAny = processAnySpamCondition(totalUsersAnySpam);

            int totalSuspended = suspendedCountPublicRatio + suspendedCountAny;

            // Quick summary line for easy log searching
            logger.info("✅ SPAM SUSPENSION COMPLETED: {} total suspended ({} public spam ratio, {} any spam)", 
                totalSuspended, suspendedCountPublicRatio, suspendedCountAny);
            
            // Final summary
            logger.info("\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "SPAM USER SUSPENSION TASK COMPLETED\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "END SUMMARY:\n" +
                "  ✓ Total Users Suspended: {} users\n" +
                "    - Condition 1 (public spam ratio): {} users\n" +
                "    - Condition 2 (any spam): {} users\n" +
                "\n" +
                "Applied Criteria:\n" +
                "  → Condition 1: >= {}% of public maps are spam\n" +
                "  → Condition 2: >= {} spam maps (any visibility)\n" +
                "  → Account age: Last {} months (6 years)\n" +
                "════════════════════════════════════════════════════════════════",
                totalSuspended, 
                suspendedCountPublicRatio,
                suspendedCountAny,
                (int)(publicSpamRatioThreshold * 100), 
                minAnySpamCount, 
                monthsBack);

        } catch (Exception e) {
            logger.error("Error during spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Process public spam ratio condition by iterating through batches of users
     * @param totalUsers total number of users to process
     * @return number of users suspended
     */
    private int processPublicSpamRatioCondition(long totalUsers) {
        int suspendedCount = 0;
        int offset = 0;
        int batchNumber = 0;
        String conditionName = "Public Spam Ratio >= " + (int)(publicSpamRatioThreshold * 100) + "%";

        while (offset < totalUsers) {
            try {
                batchNumber++;
                logger.info("Processing {} batch #{} (offset: {}, size: {})", conditionName, batchNumber, offset, batchSize);
                
                List<SpamRatioUserResult> users = mindmapManager.findUsersWithHighPublicSpamRatio(
                    publicSpamRatioThreshold, monthsBack, offset, batchSize);
                
                int batchSuspendedCount = processRatioBatch(users, conditionName);
                suspendedCount += batchSuspendedCount;

                if (users.isEmpty()) {
                    break; // No more users to process
                }
                
                // Note: We continue even if batchSuspendedCount == 0 because later batches might have unsuspended users

                offset += batchSize;
            } catch (Exception e) {
                logger.error("Error processing {} batch #{} at offset {}: {}", conditionName, batchNumber, offset, e.getMessage(), e);
                // Continue with next batch instead of failing completely
                offset += batchSize;
            }
        }

        logger.info("✓ {} processing complete: {} users suspended", conditionName, suspendedCount);
        return suspendedCount;
    }

    /**
     * Process any spam condition by iterating through batches of users
     * @param totalUsers total number of users to process
     * @return number of users suspended
     */
    private int processAnySpamCondition(long totalUsers) {
        int suspendedCount = 0;
        int offset = 0;
        int batchNumber = 0;
        String conditionName = ">= " + minAnySpamCount + " spam maps (any visibility)";

        while (offset < totalUsers) {
            try {
                batchNumber++;
                logger.info("Processing {} batch #{} (offset: {}, size: {})", conditionName, batchNumber, offset, batchSize);
                
                List<SpamUserResult> users = mindmapManager.findUsersWithAnySpamMaps(
                    minAnySpamCount, monthsBack, offset, batchSize);
                
                int batchSuspendedCount = processBatch(users, conditionName);
                suspendedCount += batchSuspendedCount;

                if (users.isEmpty()) {
                    break; // No more users to process
                }
                
                // Note: We continue even if batchSuspendedCount == 0 because later batches might have unsuspended users

                offset += batchSize;
            } catch (Exception e) {
                logger.error("Error processing {} batch #{} at offset {}: {}", conditionName, batchNumber, offset, e.getMessage(), e);
                // Continue with next batch instead of failing completely
                offset += batchSize;
            }
        }

        logger.info("✓ {} processing complete: {} users suspended", conditionName, suspendedCount);
        return suspendedCount;
    }

    /**
     * Process a batch of users with spam ratio and suspend them based on spam criteria
     * @param usersWithSpamRatio list of users with spam ratio to process
     * @param conditionName name of the condition for logging
     * @return number of users suspended in this batch
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processRatioBatch(List<SpamRatioUserResult> usersWithSpamRatio, String conditionName) {
        if (usersWithSpamRatio.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        int skippedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();
        StringBuilder suspensionSummary = new StringBuilder();
        suspensionSummary.append("\n========================================\n");
        suspensionSummary.append(String.format("USER SUSPENSION BATCH REPORT (%s)\n", conditionName));
        suspensionSummary.append("========================================\n");

        for (SpamRatioUserResult result : usersWithSpamRatio) {
            try {
                Account user = result.getUser();
                long spamCount = result.getSpamCount();
                long totalCount = result.getTotalCount();
                double spamRatio = totalCount > 0 ? (spamCount * 100.0 / totalCount) : 0.0;

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
                    "  - Public Spam Maps: %d / %d (%.1f%%)\n" +
                    "  - Suspension Reason: ABUSE\n" +
                    "  - Criteria: %s\n",
                    suspendedCount,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getCreationDate() != null ? user.getCreationDate().getTime() : "Unknown",
                    spamCount,
                    totalCount,
                    spamRatio,
                    conditionName
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
        suspensionSummary.append(String.format("  - Total Users Processed: %d\n", usersWithSpamRatio.size()));
        suspensionSummary.append(String.format("  - Users Suspended: %d\n", suspendedCount));
        suspensionSummary.append(String.format("  - Users Skipped (already suspended): %d\n", skippedCount));
        suspensionSummary.append("========================================\n");
        
        logger.info(suspensionSummary.toString());

        return suspendedCount;
    }

    /**
     * Process a batch of users and suspend them based on spam criteria
     * @param usersWithSpamMaps list of users with spam maps to process
     * @param conditionName name of the condition for logging
     * @return number of users suspended in this batch
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int processBatch(List<SpamUserResult> usersWithSpamMaps, String conditionName) {
        if (usersWithSpamMaps.isEmpty()) {
            return 0;
        }

        int suspendedCount = 0;
        int skippedCount = 0;
        
        // Collect users that need to be suspended
        List<Account> usersToSuspend = new ArrayList<>();
        StringBuilder suspensionSummary = new StringBuilder();
        suspensionSummary.append("\n========================================\n");
        suspensionSummary.append(String.format("USER SUSPENSION BATCH REPORT (%s)\n", conditionName));
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
                    "  - Criteria: %s\n",
                    suspendedCount,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getCreationDate() != null ? user.getCreationDate().getTime() : "Unknown",
                    spamCount,
                    conditionName
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
        suspensionSummary.append("========================================\n");
        
        logger.info(suspensionSummary.toString());

        return suspendedCount;
    }

    /**
     * Get total count of users with high public spam ratio (transactional)
     */
    @Transactional(readOnly = true)
    public long getTotalUsersWithPublicSpamRatio() {
        return mindmapManager.countUsersWithHighPublicSpamRatio(publicSpamRatioThreshold, monthsBack);
    }

    /**
     * Get total count of users with spam maps (any visibility) (transactional)
     */
    @Transactional(readOnly = true)
    public long getTotalUsersWithAnySpam() {
        return mindmapManager.countUsersWithAnySpamMaps(minAnySpamCount, monthsBack);
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
     * Get the public spam ratio threshold
     */
    public double getPublicSpamRatioThreshold() {
        return publicSpamRatioThreshold;
    }

    /**
     * Get the minimum any spam count threshold
     */
    public int getMinAnySpamCount() {
        return minAnySpamCount;
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
