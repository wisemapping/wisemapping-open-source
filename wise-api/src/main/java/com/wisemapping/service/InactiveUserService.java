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

import com.wisemapping.dao.UserManager;
import com.wisemapping.model.Account;
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
    private MetricsService metricsService;

    @Value("${app.batch.inactive-user-suspension.inactivity-years:7}")
    private int inactivityYears;

    @Value("${app.batch.inactive-user-suspension.batch-size:100}")
    private int batchSize;

    @Value("${app.batch.inactive-user-suspension.dry-run:false}")
    private boolean dryRun;

    public void processInactiveUsers() {
        logger.info("Starting inactive user suspension process - inactivity threshold: {} years, batch size: {}, dry run: {}",
                inactivityYears, batchSize, dryRun);

        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -inactivityYears);

        // Log upfront how many users qualify
        try {
            long totalCandidates = countInactiveUsers(cutoffDate);
            logger.info("Inactive user suspension: found {} candidate users inactive since {}",
                    totalCandidates, cutoffDate.getTime());

            // One-line startup summary
            logger.info(
                    "Inactive user suspension summary: cutoffDate={}, dryRun={}, batchSize={}, inactivityYears={}, totalCandidates={}",
                    cutoffDate.getTime(), dryRun, batchSize, inactivityYears, totalCandidates);
        } catch (Exception e) {
            logger.warn("Could not compute count of inactive users prior to processing", e);
        }

        int totalProcessed = 0;
        int totalSuspended = 0;
        int offset = 0;

        List<Account> inactiveUsers;
        do {
            BatchResult result = processBatch(cutoffDate, offset, batchSize);
            totalProcessed += result.processed;
            totalSuspended += result.suspended;
            
            // Only increment offset if in dry run mode, otherwise suspended users are filtered out
            if (dryRun) {
                offset += batchSize;
            }
            
            // Check if there are more users to process
            inactiveUsers = findInactiveUsers(cutoffDate, offset, batchSize);
            
        } while (inactiveUsers.size() == batchSize);

        logger.info("Inactive user suspension process completed - Total processed: {}, Total suspended: {}", 
                totalProcessed, totalSuspended);
    }

    public BatchResult processBatch(Calendar cutoffDate, int offset, int batchSize) {
        List<Account> inactiveUsers = findInactiveUsers(cutoffDate, offset, batchSize);
        int batchProcessed = 0;
        int batchSuspended = 0;
        
        for (Account user : inactiveUsers) {
            try {
                Calendar lastLogin = findLastLoginDate(user.getId());
                Calendar lastContentActivity = findLastMindmapActivity(user.getId());

                if (dryRun) {
                    logger.info(
                            "DRY RUN - Would suspend user due to inactivity: email={}, id={}, creationDate={}, lastLogin={}, lastContentActivity={}",
                            user.getEmail(), user.getId(),
                            user.getCreationDate() != null ? user.getCreationDate().getTime() : null,
                            lastLogin != null ? lastLogin.getTime() : null,
                            lastContentActivity != null ? lastContentActivity.getTime() : null);
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
                        user.getEmail(), user.getId(), e);
                // Continue processing other users in the batch rather than failing the entire batch
            }
        }
        
        logger.debug("Batch completed - Processed: {}, Suspended: {}", batchProcessed, batchSuspended);
        return new BatchResult(batchProcessed, batchSuspended);
    }

    private static class BatchResult {
        final int processed;
        final int suspended;
        
        BatchResult(int processed, int suspended) {
            this.processed = processed;
            this.suspended = suspended;
        }
    }

    public List<Account> findInactiveUsers(Calendar cutoffDate, int offset, int limit) {
        String jpql = """
            SELECT DISTINCT a FROM com.wisemapping.model.Account a
            WHERE a.suspended = false
              AND a.activationDate IS NOT NULL
              AND a.creationDate <= :cutoffDate
              AND a.id NOT IN (
                  SELECT DISTINCT aa.user.id FROM com.wisemapping.model.AccessAuditory aa 
                  WHERE aa.loginDate >= :cutoffDate
              )
              AND a.id NOT IN (
                  SELECT DISTINCT m.creator.id FROM com.wisemapping.model.Mindmap m 
                  WHERE m.lastModificationTime >= :cutoffDate
              )
            ORDER BY a.id
            """;
        TypedQuery<Account> query = entityManager.createQuery(jpql, Account.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }


    public long countInactiveUsers(Calendar cutoffDate) {
        String jpql = """
            SELECT COUNT(DISTINCT a) FROM com.wisemapping.model.Account a
            WHERE a.suspended = false
              AND a.activationDate IS NOT NULL
              AND a.creationDate <= :cutoffDate
              AND a.id NOT IN (
                  SELECT DISTINCT aa.user.id FROM com.wisemapping.model.AccessAuditory aa 
                  WHERE aa.loginDate >= :cutoffDate
              )
              AND a.id NOT IN (
                  SELECT DISTINCT m.creator.id FROM com.wisemapping.model.Mindmap m 
                  WHERE m.lastModificationTime >= :cutoffDate
              )
            """;

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("cutoffDate", cutoffDate);

        return query.getSingleResult();
    }

    @Transactional
    private void suspendInactiveUser(Account user) {
        // Update user first to ensure consistency
        user.setSuspended(true);
        user.setSuspensionReason(SuspensionReason.INACTIVITY);
        userManager.updateUser(user);
        
        // Clear history after user is successfully updated - inline to ensure transaction context
        String deleteHistoryJpql = """
            DELETE FROM com.wisemapping.model.MindMapHistory mh 
            WHERE mh.mindmapId IN (
                SELECT m.id FROM com.wisemapping.model.Mindmap m WHERE m.creator.id = :userId
            )
            """;

        int clearedHistoryCount = entityManager.createQuery(deleteHistoryJpql)
                .setParameter("userId", user.getId())
                .executeUpdate();
        
        logger.debug("User {} suspended due to inactivity and {} history entries cleared", 
                user.getEmail(), clearedHistoryCount);
    }

    public void previewInactiveUsers() {
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.YEAR, -inactivityYears);

        long totalCount = countInactiveUsers(cutoffDate);
        logger.info("Preview: Found {} inactive users that would be suspended (inactive for {} years)", 
                totalCount, inactivityYears);

        if (totalCount > 0) {
            List<Account> sampleUsers = findInactiveUsers(cutoffDate, 0, Math.min(10, (int) totalCount));
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

    private Calendar findLastLoginDate(int userId) {
        try {
            String jpql = """
                SELECT MAX(aa.loginDate) FROM com.wisemapping.model.AccessAuditory aa
                WHERE aa.user.id = :userId
                """;
            TypedQuery<Calendar> query = entityManager.createQuery(jpql, Calendar.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.debug("Could not find last login date for user {}", userId, e);
            return null;
        }
    }

    private Calendar findLastMindmapActivity(int userId) {
        try {
            String jpql = """
                SELECT MAX(m.lastModificationTime) FROM com.wisemapping.model.Mindmap m
                WHERE m.creator.id = :userId
                """;
            TypedQuery<Calendar> query = entityManager.createQuery(jpql, Calendar.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.debug("Could not find last mindmap activity for user {}", userId, e);
            return null;
        }
    }
}