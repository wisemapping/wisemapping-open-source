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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Optimized version of SpamUserSuspensionService using cursor-based pagination
 * This is more efficient for large datasets as it doesn't use OFFSET
 */
@Service
public class SpamUserSuspensionServiceOptimized {

    private static final Logger logger = LoggerFactory.getLogger(SpamUserSuspensionServiceOptimized.class);

    @Autowired
    private MindmapManager mindmapManager;

    @Autowired
    private UserService userService;

    @Value("${app.batch.spam-user-suspension.enabled:true}")
    private boolean enabled;

    @Value("${app.batch.spam-user-suspension.spam-threshold:2}")
    private int spamThreshold;

    @Value("${app.batch.spam-user-suspension.months-back:3}")
    private int monthsBack;

    @Value("${app.batch.spam-user-suspension.batch-size:50}")
    private int batchSize;

    /**
     * Process users with multiple spam mindmaps using cursor-based pagination
     * This is more efficient than offset-based pagination for large datasets
     */
    @Transactional
    public void processSpamUserSuspensionOptimized() {
        if (!enabled) {
            logger.debug("Spam user suspension batch task is disabled");
            return;
        }

        logger.info("Starting optimized spam user suspension batch task with threshold: {} and months back: {}", spamThreshold, monthsBack);

        try {
            int suspendedCount = 0;
            Integer lastUserId = null; // Cursor for pagination
            boolean hasMoreData = true;

            while (hasMoreData) {
                // Use cursor-based pagination instead of offset
                List<SpamUserResult> usersWithSpamMindmaps = mindmapManager.findUsersWithSpamMindapsCursor(
                    spamThreshold, monthsBack, lastUserId, batchSize);

                if (usersWithSpamMindmaps.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                for (SpamUserResult result : usersWithSpamMindmaps) {
                    Account user = result.getUser();
                    long spamCount = result.getSpamCount();

                    if (user.isSuspended()) {
                        logger.debug("User {} is already suspended. Skipping.", user.getEmail());
                        continue;
                    }

                    // Suspend the user
                    user.suspend(SuspensionReason.ABUSE);
                    userService.updateUser(user);

                    suspendedCount++;
                    logger.warn("Suspended user {} (created: {}) due to {} spam mindmaps",
                        user.getEmail(), user.getCreationDate(), spamCount);
                }

                // Update cursor to the last processed user ID
                if (!usersWithSpamMindmaps.isEmpty()) {
                    lastUserId = usersWithSpamMindmaps.get(usersWithSpamMindmaps.size() - 1).getUser().getId();
                }

                // If we got fewer results than batch size, we've reached the end
                if (usersWithSpamMindmaps.size() < batchSize) {
                    hasMoreData = false;
                }

                logger.debug("Processed batch: lastUserId={}, batchSize={}, totalSuspended={}",
                    lastUserId, batchSize, suspendedCount);
            }

            logger.info("Optimized spam user suspension batch task completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during optimized spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Alternative: Stream-based processing for even more memory efficiency
     * This processes one user at a time without loading all into memory
     */
    @Transactional
    public void processSpamUserSuspensionStream() {
        if (!enabled) {
            logger.debug("Spam user suspension batch task is disabled");
            return;
        }

        logger.info("Starting stream-based spam user suspension batch task");

        try {
            int suspendedCount = 0;
            Integer lastUserId = null;
            boolean hasMoreData = true;

            while (hasMoreData) {
                // Process one user at a time for maximum memory efficiency
                List<SpamUserResult> singleUser = mindmapManager.findUsersWithSpamMindapsCursor(
                    spamThreshold, monthsBack, lastUserId, 1);

                if (singleUser.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                SpamUserResult result = singleUser.get(0);
                Account user = result.getUser();
                long spamCount = result.getSpamCount();

                if (!user.isSuspended()) {
                    // Suspend the user
                    user.suspend(SuspensionReason.ABUSE);
                    userService.updateUser(user);

                    suspendedCount++;
                    logger.warn("Suspended user {} (created: {}) due to {} spam mindmaps",
                        user.getEmail(), user.getCreationDate(), spamCount);
                }

                // Update cursor
                lastUserId = user.getId();

                logger.debug("Processed user: userId={}, totalSuspended={}", lastUserId, suspendedCount);
            }

            logger.info("Stream-based spam user suspension completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during stream-based spam user suspension", e);
            throw e;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSpamThreshold() {
        return spamThreshold;
    }

    public int getMonthsBack() {
        return monthsBack;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
