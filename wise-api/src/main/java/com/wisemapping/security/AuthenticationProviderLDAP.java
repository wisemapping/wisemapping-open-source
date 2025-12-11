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
import org.jetbrains.annotations.Nullable;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
 * Authentication Flow:
 * 1. User submits username/password
 * 2. This provider attempts LDAP bind authentication
 * 3. If successful, retrieves user attributes (email, firstname, lastname) from LDAP
 * 4. Creates or updates the user in WiseMapping database with AuthenticationType.LDAP
 * 5. Returns authenticated token with UserDetails
 */
public class AuthenticationProviderLDAP implements AuthenticationProvider {

    private static final Logger logger = LogManager.getLogger();

    private final LdapProperties ldapProperties;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final MetricsService metricsService;
    private final LdapAuthenticationProvider delegateProvider;
    private final LdapContextSource contextSource;

    /**
     * Creates a new LDAP Authentication Provider.
     *
     * @param ldapProperties     Configuration properties for LDAP connection
     * @param userService        Service for managing WiseMapping users
     * @param userDetailsService Service for loading user details
     * @param metricsService     Service for tracking authentication metrics
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
     * The context source manages connections to the LDAP server.
     */
    private LdapContextSource createContextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(ldapProperties.getUrl());
        source.setBase(ldapProperties.getBaseDn());
        source.setPooled(ldapProperties.isPooled());

        // Set manager credentials if provided (required for non-anonymous binds)
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

        // Initialize the context source
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
     * This handles the actual LDAP authentication logic.
     */
    private LdapAuthenticationProvider createDelegateProvider() {
        // Create bind authenticator for LDAP authentication
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

        // Initialize the authenticator
        try {
            authenticator.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("Failed to initialize LDAP authenticator: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize LDAP authenticator", e);
        }

        // Create a simple authorities populator that assigns ROLE_USER to all LDAP users
        LdapAuthoritiesPopulator authoritiesPopulator = (userData, username) ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // Create the Spring Security LDAP provider with our authenticator and authorities populator
        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        logger.debug("Attempting LDAP authentication for user: {}", username);

        if (password == null || password.isEmpty()) {
            throw new BadCredentialsException("Password cannot be empty");
        }

        try {
            // Step 1: Authenticate against LDAP server using the delegate provider
            Authentication ldapAuth = delegateProvider.authenticate(authentication);

            if (!ldapAuth.isAuthenticated()) {
                throw new BadCredentialsException("LDAP authentication failed for user: " + username);
            }

            logger.debug("LDAP authentication successful for user: {}", username);

            // Step 2: Extract user attributes from LDAP
            DirContextOperations ldapUser = (DirContextOperations) ldapAuth.getPrincipal();
            LdapUserInfo userInfo = extractUserInfo(ldapUser, username);

            logger.debug("Extracted LDAP user info - email: {}, firstname: {}, lastname: {}",
                    userInfo.email, userInfo.firstname, userInfo.lastname);

            // Step 3: Synchronize user with WiseMapping database
            Account account = synchronizeUser(userInfo);

            // Step 4: Create and return authenticated token with WiseMapping UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(account.getEmail());

            // Track LDAP login metrics
            metricsService.trackUserLogin(account, "ldap");

            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    password,
                    userDetails.getAuthorities()
            );

        } catch (org.springframework.ldap.CommunicationException e) {
            logger.error("LDAP server communication error: {}", e.getMessage());
            throw new BadCredentialsException("Unable to connect to LDAP server", e);

        } catch (org.springframework.ldap.AuthenticationException e) {
            logger.debug("LDAP authentication failed for user {}: {}", username, e.getMessage());
            throw new BadCredentialsException("Invalid LDAP credentials", e);

        } catch (AccountSuspendedException e) {
            logger.warn("Suspended account attempted LDAP login: {}", username);
            throw new DisabledException("Account is suspended", e);

        } catch (AuthenticationException e) {
            // Re-throw Spring Security exceptions
            throw e;

        } catch (Exception e) {
            logger.error("Unexpected error during LDAP authentication for user {}: {}", username, e.getMessage(), e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    /**
     * Extracts user information from LDAP directory entry.
     * Uses configured attribute names to retrieve email, firstname, and lastname.
     */
    private LdapUserInfo extractUserInfo(@NotNull DirContextOperations ldapUser, @NotNull String username) {
        // Get email from LDAP - this is required and is used as the user identifier
        String email = ldapUser.getStringAttribute(ldapProperties.getEmailAttribute());
        if (email == null || email.isEmpty()) {
            // Fallback: use username as email if mail attribute is not set
            if (username.contains("@")) {
                email = username;
            } else {
                // Generate email from username
                email = username + "@ldap.local";
                logger.warn("LDAP user {} has no email attribute, using generated email: {}", username, email);
            }
        }

        // Get firstname from LDAP
        String firstname = ldapUser.getStringAttribute(ldapProperties.getFirstnameAttribute());
        if (firstname == null || firstname.isEmpty()) {
            firstname = username; // Fallback to username
        }

        // Get lastname from LDAP
        String lastname = ldapUser.getStringAttribute(ldapProperties.getLastnameAttribute());
        if (lastname == null || lastname.isEmpty()) {
            lastname = ""; // Empty is acceptable for lastname
        }

        return new LdapUserInfo(email.toLowerCase(), firstname, lastname, username);
    }

    /**
     * Synchronizes LDAP user with WiseMapping database.
     * Creates a new account if the user doesn't exist, or updates existing LDAP account.
     */
    private Account synchronizeUser(@NotNull LdapUserInfo userInfo) throws Exception {
        Account existingUser = userService.getUserBy(userInfo.email);

        if (existingUser == null) {
            // Create new user in WiseMapping database
            logger.info("Creating new WiseMapping account for LDAP user: {}", userInfo.email);
            return createLdapUser(userInfo);

        } else {
            // Verify existing user is an LDAP user
            if (existingUser.getAuthenticationType() != AuthenticationType.LDAP) {
                logger.warn("User {} exists with authentication type {} but attempted LDAP login",
                        userInfo.email, existingUser.getAuthenticationType());
                throw new BadCredentialsException(
                        "Account exists with different authentication method. " +
                                "Please use " + existingUser.getAuthenticationType() + " to login.");
            }

            // Check if account is suspended
            if (existingUser.isSuspended()) {
                throw new AccountSuspendedException("Account is suspended for user: " + userInfo.email);
            }

            // Update user profile from LDAP (in case attributes changed)
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
        newUser.setPassword(""); // LDAP users don't need local password
        newUser.setAuthenticationType(AuthenticationType.LDAP);
        newUser.setActivationDate(Calendar.getInstance()); // Auto-activate LDAP users

        userService.createUser(newUser, false, false);
        logger.info("Created new WiseMapping account for LDAP user: {}", userInfo.email);

        return newUser;
    }

    /**
     * Updates existing LDAP user's profile with latest LDAP information.
     */
    private Account updateLdapUser(@NotNull Account existingUser, @NotNull LdapUserInfo userInfo) {
        boolean updated = false;

        // Update firstname if changed
        if (!userInfo.firstname.equals(existingUser.getFirstname())) {
            existingUser.setFirstname(userInfo.firstname);
            updated = true;
        }

        // Update lastname if changed
        if (userInfo.lastname != null && !userInfo.lastname.equals(existingUser.getLastname())) {
            existingUser.setLastname(userInfo.lastname);
            updated = true;
        }

        if (updated) {
            userService.updateUser(existingUser);
            logger.debug("Updated profile for LDAP user: {}", userInfo.email);
        }

        // Update last login
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
     * Useful for health checks and configuration validation.
     *
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try {
            contextSource.getReadOnlyContext().close();
            logger.debug("LDAP connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("LDAP connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the LDAP properties used by this provider.
     */
    public LdapProperties getLdapProperties() {
        return ldapProperties;
    }
}