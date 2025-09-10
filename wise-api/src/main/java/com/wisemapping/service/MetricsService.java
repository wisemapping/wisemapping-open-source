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

import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
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
    private static final String USER_LOGINS = "wisemapping.api.user.logins";
    private static final String USER_REGISTRATIONS = "wisemapping.api.user.registrations";
    private static final String MINDMAPS_CREATED = "wisemapping.api.mindmaps.created";
    private static final String SPAM_DETECTED = "wisemapping.api.spam.detected";
    private static final String SPAM_PREVENTED = "wisemapping.api.spam.prevented";
    
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

    /**
     * Get the authentication type as a string for metrics
     * @param authenticationType The authentication type enum
     * @return String representation for metrics
     */
    @NotNull
    public String getAuthTypeString(@Nullable AuthenticationType authenticationType) {
        if (authenticationType == null) {
            return "unknown";
        }
        return String.valueOf(authenticationType.getCode()).toLowerCase();
    }
}
