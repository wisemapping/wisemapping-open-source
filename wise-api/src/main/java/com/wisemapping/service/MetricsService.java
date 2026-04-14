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

import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamDetectionResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Centralized service for managing application telemetry metrics using Micrometer with OpenTelemetry.
 * This service provides methods to track key business metrics including:
 * - User logins
 * - User registrations (by email provider)
 * - User account activations
 * - Mindmap creation
 * - Spam detection
 * 
 * Uses Micrometer MeterRegistry which can export to OpenTelemetry without auto-instrumentation.
 */
@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${app.telemetry.enabled:false}")
    private boolean telemetryEnabled;

    // Metric names as constants to avoid typos
    private static final String USER_LOGINS = "wisemapping.api.user.auth";
    private static final String USER_LOGOUTS = "wisemapping.api.user.logouts";
    private static final String USER_REGISTRATIONS = "wisemapping.api.user.registrations";
    private static final String USER_ACTIVATIONS = "wisemapping.api.user.activations";
    private static final String USER_SUSPENSIONS = "wisemapping.api.user.suspensions";
    private static final String MINDMAPS_CREATED = "wisemapping.api.mindmaps.created";
    private static final String MINDMAPS_MADE_PUBLIC = "wisemapping.api.mindmaps.made_public";
    private static final String MINDMAPS_SHARED = "wisemapping.api.mindmaps.shared";
    private static final String SPAM_ANALYZED = "wisemapping.api.spam.analyzed";
    private static final String SPAM_DETECTED = "wisemapping.api.spam.detected";
    private static final String SPAM_PREVENTED = "wisemapping.api.spam.prevented";
    private static final String INACTIVE_USERS_PROCESSED = "wisemapping.api.inactive_users.processed";
    private static final String INACTIVE_USERS_SUSPENDED = "wisemapping.api.inactive_users.suspended";
    private static final String INACTIVE_USERS_BATCH_SUSPENDED = "wisemapping.api.inactive_users.batch_suspended";
    private static final String INACTIVE_USERS_DRY_RUN_CANDIDATES = "wisemapping.api.inactive_users.dry_run_candidates";
    private static final String INACTIVE_MINDMAPS_MIGRATED = "wisemapping.api.inactive_mindmaps.migrated";
    private static final String INACTIVE_MINDMAPS_USERS_PROCESSED = "wisemapping.api.inactive_mindmaps.users_processed";
    
    /**
     * Track a user login event
     * @param user The user who logged in
     * @param authType The authentication type used (e.g., "database", "oauth", "google")
     */
    public void trackUserLogin(@NotNull Account user, @NotNull String authType) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(USER_LOGINS)
                    .description("Total number of user logins")
                    .tag("auth_type", authType)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked login metric");
         } catch (Exception e) {
             logger.warn("Failed to track login metric: {}", e.getMessage());
         }
     }

    /**
     * Track a user logout event
     * @param user The user who logged out
     * @param logoutType The logout type (e.g., "manual", "session_expired", "admin")
     */
    public void trackUserLogout(@NotNull Account user, @NotNull String logoutType) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(USER_LOGOUTS)
                    .description("Total number of user logouts")
                    .tag("logout_type", logoutType)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked logout metric");
         } catch (Exception e) {
             logger.warn("Failed to track logout metric: {}", e.getMessage());
         }
     }

     /**
      * Track a user registration event
      * @param user The newly registered user
      */
     public void trackUserRegistration(@NotNull Account user) {
        if (!telemetryEnabled) return;
        try {
            String authType = user.getAuthenticationType() != null ? 
                String.valueOf(user.getAuthenticationType().getCode()) : "unknown";
            
            Counter.builder(USER_REGISTRATIONS)
                    .description("Total number of user registrations")
                    .tag("auth_type", authType)
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked registration metric");
         } catch (Exception e) {
             logger.warn("Failed to track registration metric: {}", e.getMessage());
         }
     }

     /**
      * Track a user account activation event
      * @param user The user whose account was activated
      */
     public void trackUserActivation(@NotNull Account user) {
        if (!telemetryEnabled) return;
        try {
             String authType = user.getAuthenticationType() != null ? 
                 String.valueOf(user.getAuthenticationType().getCode()) : "unknown";
             
             Counter.builder(USER_ACTIVATIONS)
                     .description("Total number of user account activations")
                     .tag("auth_type", authType)
                     .register(meterRegistry)
                     .increment();
             
             logger.debug("Tracked activation metric");
         } catch (Exception e) {
             logger.warn("Failed to track activation metric: {}", e.getMessage());
         }
     }

    /**
     * Track a mindmap creation event
     * @param mindmap The created mindmap
     * @param user The user who created it
     * @param creationType The type of creation (e.g., "new", "duplicate", "tutorial", "import")
     */
    public void trackMindmapCreation(@NotNull Mindmap mindmap, @NotNull Account user, @NotNull String creationType) {
        if (!telemetryEnabled) return;
        try {
            String visibility = mindmap.isPublic() ? "public" : "private";
            
            Counter.builder(MINDMAPS_CREATED)
                    .description("Total number of mindmaps created")
                    .tag("type", creationType)
                    .tag("visibility", visibility)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked mindmap creation metric");
         } catch (Exception e) {
             logger.warn("Failed to track mindmap creation metric: {}", e.getMessage());
         }
     }

     /**
      * Track when a user is suspended/disabled
      * @param user The user who was suspended
      * @param reason The suspension reason
      */
    public void trackUserSuspension(@NotNull Account user, @NotNull String reason) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(USER_SUSPENSIONS)
                    .description("Total number of users suspended")
                    .tag("reason", reason.toLowerCase())
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();

            logger.debug("Tracked user suspension metric");
        } catch (Exception e) {
            logger.warn("Failed to track user suspension metric: {}", e.getMessage());
        }
    }

    /**
     * Track when a mindmap is made public
     * @param mindmap The mindmap that was made public
     * @param user The user who made it public
     */
    public void trackMindmapMadePublic(@NotNull Mindmap mindmap, @NotNull Account user) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(MINDMAPS_MADE_PUBLIC)
                    .description("Total number of mindmaps made public")
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .tag("has_description", mindmap.getDescription() != null && !mindmap.getDescription().trim().isEmpty() ? "true" : "false")
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked mindmap made public metric");
         } catch (Exception e) {
             logger.warn("Failed to track mindmap made public metric: {}", e.getMessage());
         }
     }

     /**
      * Track when a mindmap is shared with a collaborator
      * @param mindmap The mindmap that was shared
      * @param role The collaboration role granted
      * @param sharedBy The user who shared the mindmap
      */
      public void trackMindmapShared(@NotNull Mindmap mindmap, @NotNull String role, @NotNull Account sharedBy) {
         if (!telemetryEnabled) return;
         try {
             Counter.builder(MINDMAPS_SHARED)
                     .description("Total number of mindmaps shared with collaborators")
                     .tag("role", role.toLowerCase())
                     .tag("sharer_type", String.valueOf(sharedBy.getAuthenticationType().getCode()))
                     .tag("mindmap_visibility", mindmap.isPublic() ? "public" : "private")
                     .register(meterRegistry)
                     .increment();
             
             logger.debug("Tracked mindmap shared metric");
         } catch (Exception e) {
             logger.warn("Failed to track mindmap shared metric: {}", e.getMessage());
         }
     }

    /**
     * Track a spam analysis event - unified metric that captures all spam analysis results
     * @param mindmap The mindmap that was analyzed
     * @param spamResult The spam detection result
     * @param context The context where spam analysis was performed (e.g., "creation", "update", "batch_scan")
     */
    public void trackSpamAnalysis(@NotNull Mindmap mindmap, @NotNull SpamDetectionResult spamResult, @NotNull String context) {
        if (!telemetryEnabled) return;
        try {
            String isSpam = spamResult.isSpam() ? "yes" : "no";
            String spamType = spamResult.isSpam() && spamResult.getStrategyType() != null ? 
                spamResult.getStrategyType().name() : "none";
            
            Counter.builder(SPAM_ANALYZED)
                    .description("Total number of spam analyses performed")
                    .tag("context", context)
                    .tag("is_spam", isSpam)
                    .tag("spam_type", spamType)
                    .tag("visibility", mindmap.isPublic() ? "public" : "private")
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked spam analysis: mindmap {} analyzed in context {} - spam: {} type: {}", 
                mindmap.getId(), context, isSpam, spamType);
        } catch (Exception e) {
            logger.warn("Failed to track spam analysis metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track a spam detection event
     * @param mindmap The mindmap that was checked
     * @param spamResult The spam detection result
     * @param context The context where spam was detected (e.g., "creation", "update", "batch_scan")
     */
    public void trackSpamDetection(@NotNull Mindmap mindmap, @NotNull SpamDetectionResult spamResult, @NotNull String context) {
        if (!telemetryEnabled) return;
        try {
            if (spamResult.isSpam()) {
                Counter.builder(SPAM_DETECTED)
                        .description("Total number of spam items detected")
                        .tag("context", context)
                        .tag("strategy", spamResult.getStrategyType().name())
                        .tag("visibility", mindmap.isPublic() ? "public" : "private")
                        .register(meterRegistry)
                        .increment();
                
                logger.debug("Tracked spam detection: mindmap {} marked as spam in context {} using strategy {}", 
                    mindmap.getId(), context, spamResult.getStrategyType());
            }
        } catch (Exception e) {
            logger.warn("Failed to track spam detection metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track when spam prevention blocks an action
     * @param mindmap The mindmap that was blocked
     * @param action The action that was prevented (e.g., "publish", "share")
     */
    public void trackSpamPrevention(@NotNull Mindmap mindmap, @NotNull String action) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(SPAM_PREVENTED)
                    .description("Total number of actions prevented due to spam detection")
                    .tag("action", action)
                    .tag("spam_type", mindmap.getSpamTypeCode() != null ? mindmap.getSpamTypeCode().name() : "unknown")
                    .register(meterRegistry)
                    .increment();
            
             logger.debug("Tracked spam prevention metric");
         } catch (Exception e) {
             logger.warn("Failed to track spam prevention metric: {}", e.getMessage());
         }
     }

    /**
     * Track inactive user processing metrics
     * @param processed Total number of users processed in the batch
     * @param suspended Total number of users suspended in the batch
     */
    public void trackInactiveUserProcessing(int processed, int suspended) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(INACTIVE_USERS_PROCESSED)
                    .description("Total number of inactive users processed")
                    .register(meterRegistry)
                    .increment(processed);
            
            if (suspended > 0) {
                Counter.builder(INACTIVE_USERS_SUSPENDED)
                        .description("Total number of inactive users suspended")
                        .register(meterRegistry)
                        .increment(suspended);
            }
            
            logger.debug("Tracked inactive user processing: {} processed, {} suspended", processed, suspended);
        } catch (Exception e) {
            logger.warn("Failed to track inactive user processing metrics: {}", e.getMessage());
        }
    }

    /**
     * Track batch-level inactive user suspension
     * @param suspended Number of users suspended in this batch
     */
    public void trackInactiveUserBatchSuspension(int suspended) {
        if (!telemetryEnabled) return;
        try {
            if (suspended > 0) {
                Counter.builder(INACTIVE_USERS_BATCH_SUSPENDED)
                        .description("Total number of inactive users suspended per batch")
                        .register(meterRegistry)
                        .increment(suspended);
                
                logger.debug("Tracked inactive user batch suspension: {} users", suspended);
            }
        } catch (Exception e) {
            logger.warn("Failed to track inactive user batch suspension metric: {}", e.getMessage());
        }
    }

    /**
     * Track dry run candidates for inactive user suspension
     * @param candidates Number of users identified as suspension candidates in dry run
     */
    public void trackInactiveUserDryRunCandidates(int candidates) {
        if (!telemetryEnabled) return;
        try {
            if (candidates > 0) {
                Counter.builder(INACTIVE_USERS_DRY_RUN_CANDIDATES)
                        .description("Total number of inactive users identified as suspension candidates in dry run")
                        .register(meterRegistry)
                        .increment(candidates);
                
                logger.debug("Tracked inactive user dry run candidates: {} users", candidates);
            }
        } catch (Exception e) {
            logger.warn("Failed to track inactive user dry run candidates metric: {}", e.getMessage());
        }
    }

    /**
     * Track inactive mindmap migration metrics
     * @param usersProcessed Total number of inactive users processed for migration
     * @param mindmapsMigrated Total number of mindmaps migrated to inactive table
     */
    public void trackInactiveMindmapMigration(int usersProcessed, int mindmapsMigrated) {
        if (!telemetryEnabled) return;
        try {
            Counter.builder(INACTIVE_MINDMAPS_USERS_PROCESSED)
                    .description("Total number of inactive users processed for mindmap migration")
                    .register(meterRegistry)
                    .increment(usersProcessed);
            
            if (mindmapsMigrated > 0) {
                Counter.builder(INACTIVE_MINDMAPS_MIGRATED)
                        .description("Total number of mindmaps migrated to inactive table")
                        .register(meterRegistry)
                        .increment(mindmapsMigrated);
            }
            
            logger.debug("Tracked inactive mindmap migration: {} users processed, {} mindmaps migrated", 
                        usersProcessed, mindmapsMigrated);
        } catch (Exception e) {
            logger.warn("Failed to track inactive mindmap migration metrics: {}", e.getMessage());
        }
    }

 }
