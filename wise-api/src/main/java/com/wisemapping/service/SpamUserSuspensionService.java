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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpamUserSuspensionService {

    private static final Logger logger = LoggerFactory.getLogger(SpamUserSuspensionService.class);

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

    @Value("${app.batch.spam-user-suspension.min-spam-count:3}")
    private int minSpamCount;

    @Value("${app.batch.spam-user-suspension.spam-ratio-threshold:0.5}")
    private double spamRatioThreshold;

    @Value("${app.batch.spam-user-suspension.use-ratio-based:true}")
    private boolean useRatioBased;

    /**
     * Process users with multiple spam mindmaps and suspend them if necessary
     */
    @Transactional
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
     * Process users using ratio-based suspension (spam maps / total maps)
     */
    @Transactional
    public void processSpamUserSuspensionByRatio() {
        logger.info("Starting ratio-based spam user suspension batch task with min spam count: {}, ratio threshold: {}%, and months back: {}", 
            minSpamCount, spamRatioThreshold * 100, monthsBack);

        try {
            long totalUsers = mindmapManager.countUsersWithHighSpamRatio(minSpamCount, spamRatioThreshold, monthsBack);
            logger.info("Starting ratio-based spam user suspension for {} users in batches of {}", totalUsers, batchSize);

            int suspendedCount = 0;
            int offset = 0;

            while (offset < totalUsers) {
                List<SpamRatioUserResult> usersWithHighSpamRatio = mindmapManager.findUsersWithHighSpamRatio(
                    minSpamCount, spamRatioThreshold, monthsBack, offset, batchSize);

                if (usersWithHighSpamRatio.isEmpty()) {
                    break;
                }

                for (SpamRatioUserResult result : usersWithHighSpamRatio) {
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
                    userService.updateUser(user);

                    suspendedCount++;
                    logger.warn("Suspended user {} (created: {}) due to {} spam mindmaps out of {} total ({}% spam ratio)",
                        user.getEmail(), user.getCreationDate(), spamCount, totalCount, 
                        String.format("%.1f", spamRatio * 100));
                }

                offset += batchSize;
                logger.debug("Processed batch: offset={}, batchSize={}, totalSuspended={}",
                    offset - batchSize, batchSize, suspendedCount);
            }

            logger.info("Ratio-based spam user suspension batch task completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during ratio-based spam user suspension batch task", e);
            throw e;
        }
    }

    /**
     * Process users using count-based suspension (legacy method)
     */
    @Transactional
    public void processSpamUserSuspensionByCount() {
        logger.info("Starting count-based spam user suspension batch task with threshold: {} and months back: {}", spamThreshold, monthsBack);

        try {
            long totalUsers = mindmapManager.countUsersWithSpamMindaps(spamThreshold, monthsBack);
            logger.info("Starting count-based spam user suspension for {} users in batches of {}", totalUsers, batchSize);

            int suspendedCount = 0;
            int offset = 0;

            while (offset < totalUsers) {
                List<SpamUserResult> usersWithSpamMindmaps = mindmapManager.findUsersWithSpamMindaps(spamThreshold, monthsBack, offset, batchSize);

                if (usersWithSpamMindmaps.isEmpty()) {
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

                offset += batchSize;
                logger.debug("Processed batch: offset={}, batchSize={}, totalSuspended={}",
                    offset - batchSize, batchSize, suspendedCount);
            }

            logger.info("Count-based spam user suspension batch task completed. Suspended {} users", suspendedCount);

        } catch (Exception e) {
            logger.error("Error during count-based spam user suspension batch task", e);
            throw e;
        }
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
}
