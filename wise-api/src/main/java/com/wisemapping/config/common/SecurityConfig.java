package com.wisemapping.config.common;

import com.wisemapping.config.LdapProperties;
import com.wisemapping.security.*;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Security Configuration for WiseMapping.
 *
 * This configuration sets up authentication providers for the application.
 * It supports multiple authentication methods:
 *
 * 1. DATABASE Authentication (always enabled)
 *    - Traditional username/password authentication against WiseMapping's database
 *    - Users with AuthenticationType.DATABASE
 *
 * 2. LDAP Authentication (configurable via app.ldap.enabled)
 *    - Enterprise LDAP/Active Directory authentication
 *    - Users with AuthenticationType.LDAP
 *    - Automatically creates/syncs users from LDAP on first login
 *
 * Authentication Flow:
 * When a user attempts to authenticate, the AuthenticationManager tries each
 * configured AuthenticationProvider in order until one succeeds or all fail.
 *
 * - The DbAuthenticationProvider handles DATABASE users
 * - The AuthenticationProviderLDAP handles LDAP users (if enabled)
 * - Each provider checks the user's AuthenticationType to ensure the correct
 *   authentication method is used
 *
 * Configuration:
 * LDAP authentication is enabled by setting app.ldap.enabled=true in application.yaml
 * See LdapProperties for all available LDAP configuration options.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    @Lazy
    private ReadSecurityAdvise readAdvice;

    @Autowired
    @Lazy
    private UpdateSecurityAdvise updateAdvice;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private LdapProperties ldapProperties;

    @Autowired
    @Lazy
    private UserService userService;

    /**
     * Creates the method security expression handler with custom permission evaluator.
     * This enables method-level security annotations like @PreAuthorize.
     */
    @Bean
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();

        final MapAccessPermissionEvaluation permissionEvaluator =
                new MapAccessPermissionEvaluation(readAdvice, updateAdvice);
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /**
     * Creates the password encoder for DATABASE authentication.
     * Uses the delegating encoder which supports multiple encoding algorithms
     * and automatically upgrades passwords to stronger algorithms.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return DefaultPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Creates the DATABASE authentication provider.
     * This provider handles authentication for users stored in WiseMapping's database.
     * It validates passwords using the configured password encoder.
     */
    @Bean
    public AuthenticationProvider dbAuthenticationProvider() {
        final com.wisemapping.security.AuthenticationProvider provider =
                new com.wisemapping.security.AuthenticationProvider();
        provider.setEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        provider.setMetricsService(metricsService);

        logger.info("DATABASE authentication provider initialized");
        return provider;
    }

    /**
     * Creates the LDAP authentication provider (conditionally).
     * This bean is only created when app.ldap.enabled=true.
     *
     * The LDAP provider authenticates users against an external LDAP server
     * and automatically synchronizes user information with the WiseMapping database.
     */
    @Bean
    @ConditionalOnProperty(name = "app.ldap.enabled", havingValue = "true")
    public AuthenticationProviderLDAP ldapAuthenticationProvider() {
        logger.info("Initializing LDAP authentication provider with URL: {}", ldapProperties.getUrl());

        AuthenticationProviderLDAP provider = new AuthenticationProviderLDAP(
                ldapProperties,
                userService,
                userDetailsService,
                metricsService
        );

        // Test LDAP connection on startup
        if (provider.testConnection()) {
            logger.info("LDAP authentication provider initialized successfully");
        } else {
            logger.warn("LDAP authentication provider initialized but connection test failed. " +
                    "Please verify LDAP configuration.");
        }

        return provider;
    }

    /**
     * Creates the AuthenticationManager with all configured providers.
     *
     * The AuthenticationManager delegates authentication requests to the
     * configured AuthenticationProviders in order. The first provider that
     * can authenticate the request (returns a non-null Authentication) wins.
     *
     * Provider order:
     * 1. LDAP Provider (if enabled) - Handles LDAP users
     * 2. Database Provider - Handles DATABASE users
     *
     * This order ensures that LDAP authentication is attempted first when enabled,
     * allowing organizations to use LDAP as their primary authentication method
     * while still supporting local DATABASE accounts (e.g., for admin users).
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration,
            @Autowired(required = false) AuthenticationProviderLDAP ldapProvider) throws Exception {

        List<AuthenticationProvider> providers = new ArrayList<>();

        // Add LDAP provider first if enabled
        // This makes LDAP the preferred authentication method
        if (ldapProvider != null && ldapProperties.isEnabled()) {
            providers.add(ldapProvider);
            logger.info("LDAP authentication enabled - LDAP will be tried first");
        }

        // Always add the database provider
        // This handles DATABASE users and acts as fallback
        providers.add(dbAuthenticationProvider());

        logger.info("AuthenticationManager configured with {} provider(s)", providers.size());

        // Create ProviderManager with our custom provider list
        ProviderManager providerManager = new ProviderManager(providers);

        // Don't erase credentials after authentication (needed for some flows)
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }
}