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
import com.wisemapping.dao.UserManager;
import com.wisemapping.model.Account;
import com.wisemapping.model.InactiveUserResult;
import com.wisemapping.model.SuspensionReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Calendar;
import java.util.List;

@Service
public class InactiveUserService {

    private static final Logger logger = LoggerFactory.getLogger(InactiveUserService.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private MetricsService metricsService;

    @Value("${app.batch.inactive-user-suspension.inactivity-years:7}")
    private int inactivityYears;

    @Value("${app.batch.inactive-user-suspension.grace-period-years:1}")
    private int gracePeriodYears;

    @Value("${app.batch.inactive-user-suspension.batch-size:100}")
    private int batchSize;

    @Value("${app.batch.inactive-user-suspension.dry-run:false}")
    private boolean dryRun;

    public void processInactiveUsers() {
        logger.info("Starting inactive user suspension process - inactivity threshold: {} years, grace period: {} years, batch size: {}, dry run: {}",
                inactivityYears, gracePeriodYears, batchSize, dryRun);

        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -inactivityYears);

        // Calculate creation cutoff: inactivity years + grace period years
        int totalYears = inactivityYears + gracePeriodYears;
        Calendar creationCutoffDate = Calendar.getInstance();
        creationCutoffDate.add(Calendar.YEAR, -totalYears);

        // Log upfront how many users qualify using UserManager
        try {
            long totalCandidates = userManager.countUsersInactiveSince(cutoffDate, creationCutoffDate);
            logger.info("Inactive user suspension: found {} candidate users inactive since {} (created before {})",
                    totalCandidates, cutoffDate.getTime(), creationCutoffDate.getTime());

            // One-line startup summary
            logger.info(
                    "Inactive user suspension summary: cutoffDate={}, creationCutoffDate={}, dryRun={}, batchSize={}, inactivityYears={}, gracePeriodYears={}, totalCandidates={}",
                    cutoffDate.getTime(), creationCutoffDate.getTime(), dryRun, batchSize, inactivityYears, gracePeriodYears, totalCandidates);
            
            logger.info("Telemetry: Found {} total inactive user candidates for suspension", totalCandidates);
        } catch (Exception e) {
            logger.warn("Could not compute count of inactive users prior to processing", e);
        }

        int totalProcessed = 0;
        int totalSuspended = 0;
        int offset = 0;

        List<InactiveUserResult> inactiveUsers;
        do {
            BatchResult result = processBatch(cutoffDate, creationCutoffDate, offset, batchSize);
            totalProcessed += result.processed;
            totalSuspended += result.suspended;

            // Only increment offset if in dry run mode, otherwise suspended users are filtered out
            if (dryRun) {
                offset += batchSize;
            }

            // Check if there are more users to process using optimized query
            inactiveUsers = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, offset, batchSize);

        } while (inactiveUsers.size() == batchSize);

        logger.info("Inactive user suspension process completed - Total processed: {}, Total suspended: {}",
                totalProcessed, totalSuspended);
        
        // Track telemetry metrics for inactive users marked
        metricsService.trackInactiveUserProcessing(totalProcessed, totalSuspended);
        logger.info("Telemetry: Marked {} inactive users for suspension out of {} processed", totalSuspended, totalProcessed);
    }

    @Transactional
    public BatchResult processBatch(Calendar cutoffDate, Calendar creationCutoffDate, int offset, int batchSize) {
        // Use optimized query that gets all data in one go
        List<InactiveUserResult> inactiveUsers = userManager.findInactiveUsersWithActivity(cutoffDate, creationCutoffDate, offset, batchSize);
        int batchProcessed = 0;
        int batchSuspended = 0;

        for (InactiveUserResult result : inactiveUsers) {
            try {
                Account user = result.getUser();
                Calendar lastLogin = result.getLastLogin();
                Calendar lastContentActivity = result.getLastActivity();

                if (dryRun) {
                    logger.info(
                            "DRY RUN - Would suspend user due to inactivity: email={}, id={}, creationDate={}, lastLogin={}, lastContentActivity={}",
                            user.getEmail(), user.getId(),
                            user.getCreationDate() != null ? user.getCreationDate().getTime() : null,
                            lastLogin != null ? lastLogin.getTime() : null,
                            lastContentActivity != null ? lastContentActivity.getTime() : null);
                    
                    // Track dry run telemetry
                    metricsService.trackInactiveUserDryRunCandidates(1);
                    batchSuspended++; // Count as would-be suspended for dry run metrics
                } else {
                    suspendInactiveUser(user);

                    metricsService.trackUserSuspension(user, "inactivity");
                    logger.info(
                            "Suspended user due to inactivity: email={}, id={}, creationDate={}, lastLogin={}, lastContentActivity={}",
                            user.getEmail(), user.getId(),
                            user.getCreationDate() != null ? user.getCreationDate().getTime() : null,
                            lastLogin != null ? lastLogin.getTime() : null,
                            lastContentActivity != null ? lastContentActivity.getTime() : null);
                    batchSuspended++;
                }
                batchProcessed++;
            } catch (Exception e) {
                logger.error("Failed to process inactive user: {} (ID: {}) - continuing with batch",
                        result.getUser().getEmail(), result.getUser().getId(), e);
                // Continue processing other users in the batch rather than failing the entire batch
            }
        }

        logger.debug("Batch completed - Processed: {}, Suspended: {}", batchProcessed, batchSuspended);
        
        // Track batch-level telemetry
        metricsService.trackInactiveUserBatchSuspension(batchSuspended);
        
        return new BatchResult(batchProcessed, batchSuspended);
    }

    public static class BatchResult {
        final int processed;
        final int suspended;

        BatchResult(int processed, int suspended) {
            this.processed = processed;
            this.suspended = suspended;
        }
    }

    // Removed findInactiveUsers and countInactiveUsers methods - now using UserManager.findUsersInactiveSince() and UserManager.countUsersInactiveSince()
    // This follows proper separation of concerns: UserManager handles data access, InactiveUserService handles business logic

    public void suspendInactiveUser(Account user) {
        // Update user to suspend them due to inactivity
        user.setSuspended(true);
        user.setSuspensionReason(SuspensionReason.INACTIVITY);
        userManager.updateUser(user);

        logger.debug("User {} suspended due to inactivity", user.getEmail());
    }

    public void previewInactiveUsers() {
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -inactivityYears);

        // Calculate creation cutoff: inactivity years + grace period years
        int totalYears = inactivityYears + gracePeriodYears;
        Calendar creationCutoffDate = Calendar.getInstance();
        creationCutoffDate.add(Calendar.YEAR, -totalYears);

        long totalCount = userManager.countUsersInactiveSince(cutoffDate, creationCutoffDate);
        logger.info("Preview: Found {} inactive users that would be suspended (inactive for {} years, created more than {} years ago)",
                totalCount, inactivityYears, totalYears);

        if (totalCount > 0) {
            List<Account> sampleUsers = userManager.findUsersInactiveSince(cutoffDate, creationCutoffDate, 0, Math.min(10, (int) totalCount));
            logger.info("Sample of users that would be suspended:");
            for (Account user : sampleUsers) {
                logger.info("- {} (ID: {}, Created: {})",
                        user.getEmail(), user.getId(), user.getCreationDate());
            }

            if (totalCount > 10) {
                logger.info("... and {} more users", totalCount - 10);
            }
        }
    }


    /**
     * Alternative JPA approach: Find user activity using entity relationships
     * This method demonstrates a more object-oriented approach to finding user activity
     * 
     * Note: This method is provided as an example of advanced JPA usage but is not currently used
     * in the main processing flow. It could be used as an alternative to the separate
     * findLastLoginDate and findLastMindmapActivity methods.
     */
    @SuppressWarnings("unused")
    private Calendar findLastUserActivity(Account user) {
        try {
            // Use JPA entity relationships to find both login and mindmap activity
            String jpql = """
                    SELECT GREATEST(
                        COALESCE((SELECT MAX(aa.loginDate) FROM com.wisemapping.model.AccessAuditory aa WHERE aa.user.id = :userId), '1900-01-01'),
                        COALESCE((SELECT MAX(m.lastModificationTime) FROM com.wisemapping.model.Mindmap m WHERE m.creator.id = :userId), '1900-01-01')
                    )
                    """;
            TypedQuery<Calendar> query = entityManager.createQuery(jpql, Calendar.class);
            query.setParameter("userId", user.getId());
            
            Calendar result = query.getSingleResult();
            return result;
        } catch (Exception e) {
            logger.debug("Could not find last user activity for user {}", user.getId(), e);
            return null;
        }
    }

    /**
     * Advanced JPA approach: Find inactive users using UserManager
     * This method demonstrates proper separation of concerns by delegating data access to UserManager
     * while keeping business logic in the service layer
     */
    public List<Account> findInactiveUsersWithCriteriaAPI(Calendar cutoffDate, Calendar creationCutoffDate, int offset, int limit) {
        // Use UserManager for JPA-oriented data access - this is the proper JPA approach
        return userManager.findUsersInactiveSince(cutoffDate, creationCutoffDate, offset, limit);
    }

    /**
     * Enhanced JPA approach: Bulk update using entity state management
     * This method demonstrates JPA's ability to handle bulk operations efficiently
     */
    @Transactional
    public int bulkSuspendInactiveUsers(Calendar cutoffDate, Calendar creationCutoffDate, int batchSize) {
        // Use UserManager for JPA-oriented data access
        List<Account> usersToSuspend = userManager.findUsersInactiveSince(cutoffDate, creationCutoffDate, 0, batchSize);
        
        int suspendedCount = 0;
        for (Account user : usersToSuspend) {
            // Use userManager.updateUser for reliable entity state management
            user.setSuspended(true);
            user.setSuspensionReason(SuspensionReason.INACTIVITY);
            userManager.updateUser(user);
            suspendedCount++;
        }
        
        return suspendedCount;
    }
}