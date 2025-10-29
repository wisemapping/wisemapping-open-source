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


import com.wisemapping.exceptions.AccountDisabledException;
import com.wisemapping.exceptions.AccountSuspendedException;
import com.wisemapping.exceptions.WrongAuthenticationTypeException;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.service.MetricsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;


public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    private static final Logger logger = LogManager.getLogger();
    
    private UserDetailsService userDetailsService;
    private PasswordEncoder encoder;
    private MetricsService metricsService;

    @Override()
    public Authentication authenticate(@NotNull final Authentication auth) throws AuthenticationException {

        // All your user authentication needs
        final String email = auth.getName();
        logger.debug("[CUSTOM AUTH PROVIDER] Authenticating user: {}", email);

        final UserDetails userDetails = getUserDetailsService().loadUserByUsername(email);
        final Account user = userDetails.getUser();
        final String credentials = (String) auth.getCredentials();
        
        if (user == null) {
            throw new BadCredentialsException("User account is null for " + email);
        }
        
        logger.debug("[CUSTOM AUTH PROVIDER] Password validation starting for: {}", email);

        // Check if user is trying to login with wrong authentication method
        // Users registered with OAuth (Google/Facebook) cannot login with email/password
        if (user.getAuthenticationType() != AuthenticationType.DATABASE) {
            logger.debug("[CUSTOM AUTH PROVIDER] Wrong authentication type: {}", user.getAuthenticationType());
            throw new WrongAuthenticationTypeException(user, "Wrong authentication method");
        }

        // Validate password
        // encoder.matches(rawPassword, encodedPassword) - credentials is raw, user.getPassword() is encoded
        boolean passwordMatches = encoder.matches(credentials, user.getPassword());
        logger.debug("[CUSTOM AUTH PROVIDER] Password matches: {}", passwordMatches);
        
        if (credentials == null || !passwordMatches) {
            logger.debug("[CUSTOM AUTH PROVIDER] Authentication FAILED - bad credentials for: {}", email);
            throw new BadCredentialsException("Username/Password does not match for " + auth.getPrincipal());
        }

        // For DATABASE users, check if account is activated (email confirmed)
        // OAuth users are activated automatically during registration
        if (!user.isActive()) {
            logger.debug("[CUSTOM AUTH PROVIDER] Account not activated: {}", email);
            throw new AccountDisabledException("Account not activated for " + auth.getPrincipal());
        }

        // Check if account is suspended
        if (user.isSuspended()) {
            logger.debug("[CUSTOM AUTH PROVIDER] Account suspended: {}", email);
            throw new AccountSuspendedException("Account suspended for " + auth.getPrincipal());
        }
        
        logger.info("[CUSTOM AUTH PROVIDER] ✓ Authentication SUCCESSFUL for: {}", email);

        userDetailsService.getUserService().auditLogin(user);
        
        // Track database authentication login telemetry
        metricsService.trackUserLogin(user, "database");
        
        return new UsernamePasswordAuthenticationToken(userDetails, credentials, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setEncoder(@NotNull PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
}
