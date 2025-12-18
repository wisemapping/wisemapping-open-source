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
import org.springframework.ldap.core.DirContextAdapter;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LDAP Authentication Provider for WiseMapping.
 *
 * This provider handles authentication against an external LDAP server and
 * automatically synchronizes user information with the WiseMapping database.
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

    public AuthenticationProviderLDAP(
            @NotNull LdapProperties ldapProperties,
            @NotNull UserService userService,
            @NotNull UserDetailsService userDetailsService,
            @NotNull MetricsService metricsService) {

        this.ldapProperties = ldapProperties;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.metricsService = metricsService;

        this.contextSource = createContextSource();
        this.delegateProvider = createDelegateProvider();

        logger.info("LDAP Authentication Provider initialized with URL: {}", ldapProperties.getUrl());
    }

    private LdapContextSource createContextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(ldapProperties.getUrl());
        source.setBase(ldapProperties.getBaseDn());
        source.setPooled(ldapProperties.isPooled());

        if (ldapProperties.hasManagerCredentials()) {
            source.setUserDn(ldapProperties.getManagerDn());
            source.setPassword(ldapProperties.getManagerPassword());
            logger.debug("LDAP configured with manager DN: {}", ldapProperties.getManagerDn());
        }

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

    private LdapAuthenticationProvider createDelegateProvider() {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);

        if (ldapProperties.getUserDnPatterns() != null && !ldapProperties.getUserDnPatterns().isEmpty()) {
            authenticator.setUserDnPatterns(new String[] { ldapProperties.getUserDnPatterns() });
        }

        if (ldapProperties.getUserSearchFilter() != null && !ldapProperties.getUserSearchFilter().isEmpty()) {
            FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                    ldapProperties.getUserSearchBase() != null ? ldapProperties.getUserSearchBase() : "",
                    ldapProperties.getUserSearchFilter(),
                    contextSource);
            authenticator.setUserSearch(userSearch);
        }

        try {
            authenticator.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("Failed to initialize LDAP authenticator: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize LDAP authenticator", e);
        }

        // Use a simple authorities populator that does NOT search LDAP for groups
        // WiseMapping doesn't use LDAP groups - all authenticated users get ROLE_USER
        LdapAuthoritiesPopulator simpleAuthoritiesPopulator = new LdapAuthoritiesPopulator() {
            @Override
            public Collection<? extends GrantedAuthority> getGrantedAuthorities(
                    DirContextOperations userData, String username) {
                // Simply return ROLE_USER for all authenticated LDAP users
                // No LDAP group search is performed
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            }
        };

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, simpleAuthoritiesPopulator);

        // Configure user details mapper
        LdapUserDetailsMapper userDetailsMapper = new LdapUserDetailsMapper();
        provider.setUserDetailsContextMapper(userDetailsMapper);

        return provider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        logger.debug("Attempting LDAP authentication for user: {}", username);

        if (password == null || password.isEmpty()) {
            logger.debug("Empty password, skipping LDAP authentication for user: {}", username);
            return null;
        }

        // Check if user exists with non-LDAP auth type
        Account existingUser = userService.getUserBy(username);
        if (existingUser != null && existingUser.getAuthenticationType() != AuthenticationType.LDAP) {
            logger.debug("User {} exists with auth type {}, skipping LDAP",
                    username, existingUser.getAuthenticationType());
            return null;
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

            // Extract user info - handle different principal types
            LdapUserInfo userInfo = extractUserInfoFromPrincipal(ldapAuth.getPrincipal(), username);

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
                    userDetails.getAuthorities());

        } catch (CommunicationException e) {
            logger.warn("LDAP server unreachable ({}), allowing fallback to next provider", e.getMessage());
            ldapAvailable = false;
            return null;

        } catch (InternalAuthenticationServiceException e) {
            if (isNetworkError(e)) {
                logger.warn("LDAP communication error ({}), allowing fallback to next provider", e.getMessage());
                ldapAvailable = false;
                return null;
            }
            throw e;

        } catch (AuthenticationServiceException e) {
            logger.warn("LDAP service error ({}), allowing fallback to next provider", e.getMessage());
            ldapAvailable = false;
            return null;

        } catch (org.springframework.ldap.AuthenticationException e) {
            logger.debug("LDAP authentication failed for user {}: invalid credentials", username);
            if (existingUser != null && existingUser.getAuthenticationType() == AuthenticationType.LDAP) {
                throw new BadCredentialsException("Invalid LDAP credentials", e);
            }
            return null;

        } catch (BadCredentialsException e) {
            logger.debug("LDAP bad credentials for user {}", username);
            if (existingUser != null && existingUser.getAuthenticationType() == AuthenticationType.LDAP) {
                throw e;
            }
            return null;

        } catch (AccountSuspendedException e) {
            logger.warn("Suspended account attempted LDAP login: {}", username);
            throw new DisabledException("Account is suspended", e);

        } catch (Exception e) {
            if (isNetworkError(e)) {
                logger.warn("LDAP network error ({}), allowing fallback to next provider", e.getMessage());
                ldapAvailable = false;
                return null;
            }

            logger.error("Unexpected error during LDAP authentication for user {}: {}", username, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract user info from the authentication principal.
     * Handles both DirContextOperations and LdapUserDetails types.
     */
    private LdapUserInfo extractUserInfoFromPrincipal(Object principal, String username) {
        String email = null;
        String firstname = null;
        String lastname = null;
        String dn = null;

        if (principal instanceof DirContextOperations) {
            DirContextOperations ctx = (DirContextOperations) principal;
            email = ctx.getStringAttribute(ldapProperties.getEmailAttribute());
            firstname = ctx.getStringAttribute(ldapProperties.getFirstnameAttribute());
            lastname = ctx.getStringAttribute(ldapProperties.getLastnameAttribute());
            dn = ctx.getDn() != null ? ctx.getDn().toString() : null;
            logger.debug("Extracted attributes from DirContextOperations - DN: {}", dn);

        } else if (principal instanceof LdapUserDetails) {
            LdapUserDetails ldapUserDetails = (LdapUserDetails) principal;
            dn = ldapUserDetails.getDn();
            logger.debug("Principal is LdapUserDetails - DN: {}", dn);

            // Need to fetch attributes from LDAP since LdapUserDetails doesn't have them
            try {
                DirContextOperations userContext = fetchUserAttributes(dn);
                if (userContext != null) {
                    email = userContext.getStringAttribute(ldapProperties.getEmailAttribute());
                    firstname = userContext.getStringAttribute(ldapProperties.getFirstnameAttribute());
                    lastname = userContext.getStringAttribute(ldapProperties.getLastnameAttribute());
                    logger.debug("Fetched attributes from LDAP - email: {}, firstname: {}, lastname: {}",
                            email, firstname, lastname);
                }
            } catch (Exception e) {
                logger.warn("Could not fetch LDAP attributes for DN {}: {}", dn, e.getMessage());
            }
        } else {
            logger.warn("Unknown principal type: {}", principal.getClass().getName());
        }

        // Apply defaults if attributes are missing
        if (email == null || email.isEmpty()) {
            if (username.contains("@")) {
                email = username;
            } else {
                // Construct email from LDAP base DN
                // Handle both uppercase (DC=) and lowercase (dc=) DN components
                email = username + "@" + ldapProperties.getBaseDn()
                        .replaceAll("(?i)dc=", "") // Case-insensitive replacement
                        .replace(",", ".")
                        .toLowerCase();
                logger.info("Generated email for LDAP user {}: {}", username, email);
            }
        }

        if (firstname == null || firstname.isEmpty()) {
            firstname = username;
        }

        if (lastname == null || lastname.isEmpty()) {
            lastname = "";
        }

        return new LdapUserInfo(email.toLowerCase(), firstname, lastname);
    }

    /**
     * Fetch user attributes from LDAP by DN.
     */
    private DirContextOperations fetchUserAttributes(String dn) {
        try {
            LdapContext ctx = (LdapContext) contextSource.getReadOnlyContext();
            try {
                // Context already has DN, give relative Dn
                String baseDn = ldapProperties.getBaseDn();
                String relativeDn = dn;

                // Break Base Dn
                String dnLower = dn.toLowerCase();
                String baseDnLower = "," + baseDn.toLowerCase();
                int idx = dnLower.lastIndexOf(baseDnLower);
                if (idx > 0) {
                    relativeDn = dn.substring(0, idx);
                }

                logger.debug("Fetching attributes - original DN: {}, relative DN: {}", dn, relativeDn);

                Attributes attrs = ctx.getAttributes(relativeDn, new String[] {
                        ldapProperties.getEmailAttribute(),
                        ldapProperties.getFirstnameAttribute(),
                        ldapProperties.getLastnameAttribute()
                });

                DirContextAdapter adapter = new DirContextAdapter(attrs, null, null);
                return adapter;
            } finally {
                ctx.close();
            }
        } catch (NamingException e) {
            logger.warn("Failed to fetch attributes for DN {}: {}", dn, e.getMessage());
            return null;
        }
    }

    private boolean isNetworkError(Throwable e) {
        if (e == null)
            return false;

        if (e instanceof java.net.UnknownHostException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.NoRouteToHostException ||
                e instanceof javax.naming.CommunicationException ||
                e instanceof CommunicationException) {
            return true;
        }

        String message = e.getMessage();
        if (message != null && (message.contains("UnknownHostException") ||
                message.contains("Connection refused") ||
                message.contains("Connection timed out") ||
                message.contains("No route to host"))) {
            return true;
        }

        return isNetworkError(e.getCause());
    }

    private Account synchronizeUser(@NotNull LdapUserInfo userInfo) throws Exception {
        // First try to find by email
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

    private static class LdapUserInfo {
        final String email;
        final String firstname;
        final String lastname;

        LdapUserInfo(String email, String firstname, String lastname) {
            this.email = email;
            this.firstname = firstname;
            this.lastname = lastname;
        }
    }

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

    public boolean isLdapAvailable() {
        return ldapAvailable;
    }

    public LdapProperties getLdapProperties() {
        return ldapProperties;
    }
}