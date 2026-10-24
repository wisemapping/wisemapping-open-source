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
package com.wisemapping.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decides whether local (database) accounts can sign in with email and password.
 *
 * <p>When the generic OpenID Connect provider (registration id "oidc") is configured, it becomes
 * the way users sign in: database password login, self-registration and password reset are
 * disabled, so every login goes through the identity provider and its policies (MFA, ...).
 * Existing local accounts move to OIDC through the account-link confirmation on their first
 * OIDC login.
 */
@Component
public class LocalLoginPolicy {

    private static final Logger logger = LoggerFactory.getLogger(LocalLoginPolicy.class);

    @Value("${spring.security.oauth2.client.registration.oidc.client-id:}")
    private String oidcClientId;

    @PostConstruct
    void logPolicy() {
        if (!isLocalLoginEnabled()) {
            logger.info("OpenID Connect is configured: email/password login, registration and password reset are disabled for local accounts");
        }
    }

    public boolean isLocalLoginEnabled() {
        return oidcClientId == null || oidcClientId.isBlank();
    }
}
