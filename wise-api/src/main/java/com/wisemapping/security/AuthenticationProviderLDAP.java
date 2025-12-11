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

import com.wisemapping.config.LdapProperties;
import com.wisemapping.exceptions.AccountSuspendedException;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LDAP Authentication Provider for WiseMapping.
 *
 * This provider handles authentication against an external LDAP server and
 * automatically synchronizes user information with the WiseMapping database.
 *
 * IMPORTANT: This provider is designed to work alongside the DATABASE provider.
 * - If LDAP server is unreachable (network error), this provider returns null
 *   to allow the next provider (DATABASE) to attempt authentication.
 * - If LDAP authentication fails (wrong credentials), it throws BadCredentialsException.
 * - If the user exists in DB with a different auth type, it throws BadCredentialsException.
 */
public class AuthenticationProviderLDAP implements AuthenticationProvider {

    private static final Logger logger = LogManager.getLogger();

    private final LdapProperties ldapProperties;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final MetricsService metricsService;
    private final LdapAuthenticationProvider delegateProvider;
    private final LdapContextSource contextSource;
    private volatile boolean ldapAvailable = true;

    /**
     * Creates a new LDAP Authentication Provider.
     */
    public AuthenticationProviderLDAP(
            @NotNull LdapProperties ldapProperties,
            @NotNull UserService userService,
            @NotNull UserDetailsService userDetailsService,
            @NotNull MetricsService metricsService) {

        this.ldapProperties = ldapProperties;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.metricsService = metricsService;

        // Initialize LDAP context source with configuration from properties
        this.contextSource = createContextSource();

        // Create the delegate Spring Security LDAP provider
        this.delegateProvider = createDelegateProvider();

        logger.info("LDAP Authentication Provider initialized with URL: {}", ldapProperties.getUrl());
    }

    /**
     * Creates and configures the LDAP context source.
     */
    private LdapContextSource createContextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(ldapProperties.getUrl());
        source.setBase(ldapProperties.getBaseDn());
        source.setPooled(ldapProperties.isPooled());

        // Set manager credentials if provided
        if (ldapProperties.hasManagerCredentials()) {
            source.setUserDn(ldapProperties.getManagerDn());
            source.setPassword(ldapProperties.getManagerPassword());
            logger.debug("LDAP configured with manager DN: {}", ldapProperties.getManagerDn());
        } else {
            logger.debug("LDAP configured for anonymous binding");
        }

        // Set connection timeouts
        Map<String, Object> envProps = new HashMap<>();
        envProps.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapProperties.getConnectTimeout()));
        envProps.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldapProperties.getReadTimeout()));
        source.setBaseEnvironmentProperties(envProps);

        try {
            source.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("Failed to initialize LDAP context source: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize LDAP context source", e);
        }

        return source;
    }

    /**
     * Creates the delegate Spring Security LDAP authentication provider.
     */
    private LdapAuthenticationProvider createDelegateProvider() {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);

        // Configure user DN patterns for direct bind
        if (ldapProperties.getUserDnPatterns() != null && !ldapProperties.getUserDnPatterns().isEmpty()) {
            authenticator.setUserDnPatterns(new String[]{ldapProperties.getUserDnPatterns()});
        }

        // Configure user search for finding users
        if (ldapProperties.getUserSearchFilter() != null && !ldapProperties.getUserSearchFilter().isEmpty()) {
            FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                    ldapProperties.getUserSearchBase(),
                    ldapProperties.getUserSearchFilter(),
                    contextSource
            );
            authenticator.setUserSearch(userSearch);
        }

        try {
            authenticator.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("Failed to initialize LDAP authenticator: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize LDAP authenticator", e);
        }

        LdapAuthoritiesPopulator authoritiesPopulator = (userData, username) ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        logger.debug("Attempting LDAP authentication for user: {}", username);

        if (password == null || password.isEmpty()) {
            // Let the next provider handle empty passwords
            logger.debug("Empty password, skipping LDAP authentication for user: {}", username);
            return null;
        }

        // Check if user exists in database with a non-LDAP auth type
        // If so, skip LDAP and let the appropriate provider handle it
        Account existingUser = userService.getUserBy(username);
        if (existingUser != null && existingUser.getAuthenticationType() != AuthenticationType.LDAP) {
            logger.debug("User {} exists with auth type {}, skipping LDAP",
                    username, existingUser.getAuthenticationType());
            return null; // Let DATABASE or OAuth provider handle this user
        }

        try {
            // Attempt LDAP authentication
            Authentication ldapAuth = delegateProvider.authenticate(authentication);

            if (ldapAuth == null || !ldapAuth.isAuthenticated()) {
                logger.debug("LDAP authentication returned null/unauthenticated for user: {}", username);
                return null;
            }

            logger.info("LDAP authentication successful for user: {}", username);
            ldapAvailable = true;

            // Extract user attributes from LDAP
            DirContextOperations ldapUser = (DirContextOperations) ldapAuth.getPrincipal();
            LdapUserInfo userInfo = extractUserInfo(ldapUser, username);

            logger.debug("Extracted LDAP user info - email: {}, firstname: {}, lastname: {}",
                    userInfo.email, userInfo.firstname, userInfo.lastname);

            // Synchronize user with WiseMapping database
            Account account = synchronizeUser(userInfo);

            // Create and return authenticated token with WiseMapping UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(account.getEmail());

            // Track LDAP login metrics
            metricsService.trackUserLogin(account, "ldap");

            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    password,
                    userDetails.getAuthorities()
            );

        } catch (CommunicationException e) {
            // LDAP server is unreachable - allow fallback to next provider
            logger.warn("LDAP server unreachable ({}), allowing fallback to next provider", e.getMessage());
            ldapAvailable = false;
            return null; // Return null to let ProviderManager try the next provider

        } catch (InternalAuthenticationServiceException e) {
            // This usually wraps network/communication errors
            Throwable cause = e.getCause();
            if (isNetworkError(e)) {
                logger.warn("LDAP communication error ({}), allowing fallback to next provider", e.getMessage());
                ldapAvailable = false;
                return null; // Allow fallback
            }
            // Re-throw other internal errors
            throw e;

        } catch (AuthenticationServiceException e) {
            // Network/service errors - allow fallback
            logger.warn("LDAP service error ({}), allowing fallback to next provider", e.getMessage());
            ldapAvailable = false;
            return null;

        } catch (org.springframework.ldap.AuthenticationException e) {
            // Invalid LDAP credentials - this is a definitive failure for LDAP users
            logger.debug("LDAP authentication failed for user {}: invalid credentials", username);
            // Only throw if user is known to be an LDAP user
            if (existingUser != null && existingUser.getAuthenticationType() == AuthenticationType.LDAP) {
                throw new BadCredentialsException("Invalid LDAP credentials", e);
            }
            return null; // Allow fallback for unknown users

        } catch (BadCredentialsException e) {
            // Bad credentials from LDAP
            logger.debug("LDAP bad credentials for user {}", username);
            if (existingUser != null && existingUser.getAuthenticationType() == AuthenticationType.LDAP) {
                throw e;
            }
            return null;

        } catch (AccountSuspendedException e) {
            logger.warn("Suspended account attempted LDAP login: {}", username);
            throw new DisabledException("Account is suspended", e);

        } catch (Exception e) {
            // Check if it's a network-related error wrapped in another exception
            if (isNetworkError(e)) {
                logger.warn("LDAP network error ({}), allowing fallback to next provider", e.getMessage());
                ldapAvailable = false;
                return null;
            }

            logger.error("Unexpected error during LDAP authentication for user {}: {}", username, e.getMessage(), e);
            // For unexpected errors, allow fallback to not break authentication completely
            return null;
        }
    }

    /**
     * Check if an exception is caused by a network error.
     */
    private boolean isNetworkError(Throwable e) {
        if (e == null) return false;

        if (e instanceof java.net.UnknownHostException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.NoRouteToHostException ||
                e instanceof javax.naming.CommunicationException ||
                e instanceof CommunicationException) {
            return true;
        }

        // Check the message for common network error patterns
        String message = e.getMessage();
        if (message != null && (
                message.contains("UnknownHostException") ||
                        message.contains("Connection refused") ||
                        message.contains("Connection timed out") ||
                        message.contains("No route to host"))) {
            return true;
        }

        // Check cause recursively
        return isNetworkError(e.getCause());
    }

    /**
     * Extracts user information from LDAP directory entry.
     */
    private LdapUserInfo extractUserInfo(@NotNull DirContextOperations ldapUser, @NotNull String username) {
        String email = ldapUser.getStringAttribute(ldapProperties.getEmailAttribute());
        if (email == null || email.isEmpty()) {
            if (username.contains("@")) {
                email = username;
            } else {
                email = username + "@domain.com";
                logger.warn("LDAP user {} has no email attribute, using generated email: {}", username, email);
            }
        }

        String firstname = ldapUser.getStringAttribute(ldapProperties.getFirstnameAttribute());
        if (firstname == null || firstname.isEmpty()) {
            firstname = username;
        }

        String lastname = ldapUser.getStringAttribute(ldapProperties.getLastnameAttribute());
        if (lastname == null || lastname.isEmpty()) {
            lastname = "";
        }

        return new LdapUserInfo(email.toLowerCase(), firstname, lastname, username);
    }

    /**
     * Synchronizes LDAP user with WiseMapping database.
     */
    private Account synchronizeUser(@NotNull LdapUserInfo userInfo) throws Exception {
        Account existingUser = userService.getUserBy(userInfo.email);

        if (existingUser == null) {
            logger.info("Creating new WiseMapping account for LDAP user: {}", userInfo.email);
            return createLdapUser(userInfo);
        } else {
            if (existingUser.getAuthenticationType() != AuthenticationType.LDAP) {
                logger.warn("User {} exists with authentication type {} but attempted LDAP login",
                        userInfo.email, existingUser.getAuthenticationType());
                throw new BadCredentialsException(
                        "Account exists with different authentication method. " +
                                "Please use " + existingUser.getAuthenticationType() + " to login.");
            }

            if (existingUser.isSuspended()) {
                throw new AccountSuspendedException("Account is suspended for user: " + userInfo.email);
            }

            return updateLdapUser(existingUser, userInfo);
        }
    }

    /**
     * Creates a new WiseMapping user from LDAP information.
     */
    private Account createLdapUser(@NotNull LdapUserInfo userInfo) throws Exception {
        Account newUser = new Account();
        newUser.setEmail(userInfo.email);
        newUser.setFirstname(userInfo.firstname);
        newUser.setLastname(userInfo.lastname);
        newUser.setPassword("");
        newUser.setAuthenticationType(AuthenticationType.LDAP);
        newUser.setActivationDate(Calendar.getInstance());

        userService.createUser(newUser, false, false);
        logger.info("Created new WiseMapping account for LDAP user: {}", userInfo.email);

        return newUser;
    }

    /**
     * Updates existing LDAP user's profile with latest LDAP information.
     */
    private Account updateLdapUser(@NotNull Account existingUser, @NotNull LdapUserInfo userInfo) {
        boolean updated = false;

        if (!userInfo.firstname.equals(existingUser.getFirstname())) {
            existingUser.setFirstname(userInfo.firstname);
            updated = true;
        }

        if (userInfo.lastname != null && !userInfo.lastname.equals(existingUser.getLastname())) {
            existingUser.setLastname(userInfo.lastname);
            updated = true;
        }

        if (updated) {
            userService.updateUser(existingUser);
            logger.debug("Updated profile for LDAP user: {}", userInfo.email);
        }

        userService.auditLogin(existingUser);

        return existingUser;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Internal class to hold extracted LDAP user information.
     */
    private static class LdapUserInfo {
        final String email;
        final String firstname;
        final String lastname;
        final String username;

        LdapUserInfo(String email, String firstname, String lastname, String username) {
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
            this.username = username;
        }
    }

    /**
     * Test LDAP connection.
     */
    public boolean testConnection() {
        try {
            contextSource.getReadOnlyContext().close();
            logger.debug("LDAP connection test successful");
            ldapAvailable = true;
            return true;
        } catch (Exception e) {
            logger.error("LDAP connection test failed: {}", e.getMessage());
            ldapAvailable = false;
            return false;
        }
    }

    /**
     * Check if LDAP server is currently available.
     */
    public boolean isLdapAvailable() {
        return ldapAvailable;
    }

    /**
     * Gets the LDAP properties used by this provider.
     */
    public LdapProperties getLdapProperties() {
        return ldapProperties;
    }
}