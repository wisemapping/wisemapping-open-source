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

    // Metric names as constants to avoid typos
    private static final String USER_LOGINS = "wisemapping.auth.session.created";
    private static final String USER_LOGOUTS = "wisemapping.auth.session.terminated";
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
        try {
            Counter.builder(USER_LOGINS)
                    .description("Total number of user logins")
                    .tag("auth_type", authType)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked login for user {} with auth type {}", user.getEmail(), authType);
        } catch (Exception e) {
            logger.warn("Failed to track login metric for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Track a user logout event
     * @param user The user who logged out
     * @param logoutType The logout type (e.g., "manual", "session_expired", "admin")
     */
    public void trackUserLogout(@NotNull Account user, @NotNull String logoutType) {
        try {
            Counter.builder(USER_LOGOUTS)
                    .description("Total number of user logouts")
                    .tag("logout_type", logoutType)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked logout for user {} with logout type {}", user.getEmail(), logoutType);
        } catch (Exception e) {
            logger.warn("Failed to track logout metric for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Track a user registration event
     * @param user The newly registered user
     * @param emailProvider The email provider (e.g., "gmail", "yahoo", "other")
     */
    public void trackUserRegistration(@NotNull Account user, @NotNull String emailProvider) {
        try {
            String authType = user.getAuthenticationType() != null ? 
                String.valueOf(user.getAuthenticationType().getCode()) : "unknown";
            
            Counter.builder(USER_REGISTRATIONS)
                    .description("Total number of user registrations")
                    .tag("email_provider", emailProvider)
                    .tag("auth_type", authType)
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked registration for user {} with email provider {}", user.getEmail(), emailProvider);
        } catch (Exception e) {
            logger.warn("Failed to track registration metric for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Track a user account activation event
     * @param user The user whose account was activated
     */
    public void trackUserActivation(@NotNull Account user) {
        try {
            String authType = user.getAuthenticationType() != null ? 
                String.valueOf(user.getAuthenticationType().getCode()) : "unknown";
            String emailProvider = extractEmailProvider(user.getEmail());
            
            Counter.builder(USER_ACTIVATIONS)
                    .description("Total number of user account activations")
                    .tag("email_provider", emailProvider)
                    .tag("auth_type", authType)
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked activation for user {} with email provider {}", user.getEmail(), emailProvider);
        } catch (Exception e) {
            logger.warn("Failed to track activation metric for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Track a mindmap creation event
     * @param mindmap The created mindmap
     * @param user The user who created it
     * @param creationType The type of creation (e.g., "new", "duplicate", "tutorial", "import")
     */
    public void trackMindmapCreation(@NotNull Mindmap mindmap, @NotNull Account user, @NotNull String creationType) {
        try {
            String visibility = mindmap.isPublic() ? "public" : "private";
            
            Counter.builder(MINDMAPS_CREATED)
                    .description("Total number of mindmaps created")
                    .tag("type", creationType)
                    .tag("visibility", visibility)
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked mindmap creation: {} by user {} (type: {}, visibility: {})", 
                mindmap.getTitle(), user.getEmail(), creationType, visibility);
        } catch (Exception e) {
            logger.warn("Failed to track mindmap creation metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track when a user is suspended/disabled
     * @param user The user who was suspended
     * @param reason The suspension reason
     */
    public void trackUserSuspension(@NotNull Account user, @NotNull String reason) {
        try {
            String emailProvider = extractEmailProvider(user.getEmail());
            
            Counter.builder(USER_SUSPENSIONS)
                    .description("Total number of users suspended")
                    .tag("reason", reason.toLowerCase())
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .tag("email_provider", emailProvider)
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked user suspension: {} suspended for reason {}", user.getEmail(), reason);
        } catch (Exception e) {
            logger.warn("Failed to track user suspension metric for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Track when a mindmap is made public
     * @param mindmap The mindmap that was made public
     * @param user The user who made it public
     */
    public void trackMindmapMadePublic(@NotNull Mindmap mindmap, @NotNull Account user) {
        try {
            Counter.builder(MINDMAPS_MADE_PUBLIC)
                    .description("Total number of mindmaps made public")
                    .tag("user_type", String.valueOf(user.getAuthenticationType().getCode()))
                    .tag("has_description", mindmap.getDescription() != null && !mindmap.getDescription().trim().isEmpty() ? "true" : "false")
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked mindmap made public: {} by user {}", mindmap.getTitle(), user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to track mindmap made public metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track when a mindmap is shared with a collaborator
     * @param mindmap The mindmap that was shared
     * @param collaboratorEmail The email of the collaborator
     * @param role The collaboration role granted
     * @param sharedBy The user who shared the mindmap
     */
    public void trackMindmapShared(@NotNull Mindmap mindmap, @NotNull String collaboratorEmail, @NotNull String role, @NotNull Account sharedBy) {
        try {
            String emailProvider = extractEmailProvider(collaboratorEmail);
            
            Counter.builder(MINDMAPS_SHARED)
                    .description("Total number of mindmaps shared with collaborators")
                    .tag("role", role.toLowerCase())
                    .tag("collaborator_email_provider", emailProvider)
                    .tag("sharer_type", String.valueOf(sharedBy.getAuthenticationType().getCode()))
                    .tag("mindmap_visibility", mindmap.isPublic() ? "public" : "private")
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked mindmap shared: {} shared with {} (role: {}) by user {}", 
                mindmap.getTitle(), collaboratorEmail, role, sharedBy.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to track mindmap shared metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track a spam analysis event - unified metric that captures all spam analysis results
     * @param mindmap The mindmap that was analyzed
     * @param spamResult The spam detection result
     * @param context The context where spam analysis was performed (e.g., "creation", "update", "batch_scan")
     */
    public void trackSpamAnalysis(@NotNull Mindmap mindmap, @NotNull SpamDetectionResult spamResult, @NotNull String context) {
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
        try {
            Counter.builder(SPAM_PREVENTED)
                    .description("Total number of actions prevented due to spam detection")
                    .tag("action", action)
                    .tag("spam_type", mindmap.getSpamTypeCode() != null ? mindmap.getSpamTypeCode().name() : "unknown")
                    .register(meterRegistry)
                    .increment();
            
            logger.debug("Tracked spam prevention: {} action blocked for mindmap {}", action, mindmap.getId());
        } catch (Exception e) {
            logger.warn("Failed to track spam prevention metric for mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
    }

    /**
     * Track inactive user processing metrics
     * @param processed Total number of users processed in the batch
     * @param suspended Total number of users suspended in the batch
     */
    public void trackInactiveUserProcessing(int processed, int suspended) {
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

    /**
     * Extract email provider from email address
     * @param email The email address
     * @return The email provider (e.g., "gmail", "yahoo", "hotmail", "other")
     */
    @NotNull
    public String extractEmailProvider(@Nullable String email) {
        if (email == null || !email.contains("@")) {
            return "other";
        }
        
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        
        // Map common email providers
        switch (domain) {
            case "gmail.com":
            case "googlemail.com":
                return "gmail";
            case "yahoo.com":
            case "yahoo.co.uk":
            case "yahoo.fr":
            case "yahoo.de":
                return "yahoo";
            case "hotmail.com":
            case "outlook.com":
            case "live.com":
                return "microsoft";
            case "aol.com":
                return "aol";
            case "icloud.com":
            case "me.com":
            case "mac.com":
                return "apple";
            default:
                // Check if it's a known business domain
                if (domain.contains("edu")) {
                    return "education";
                } else if (domain.contains("gov")) {
                    return "government";
                } else {
                    return "other";
                }
        }
    }
}
