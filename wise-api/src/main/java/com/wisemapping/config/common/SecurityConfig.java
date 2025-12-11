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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return DefaultPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider dbAuthenticationProvider() {
        final com.wisemapping.security.AuthenticationProvider provider =
                new com.wisemapping.security.AuthenticationProvider();
        provider.setEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        provider.setMetricsService(metricsService);

        // Add the logger information
        logger.info("DATABASE authentication provider initialized");
        return provider;
    }

    /**
     * Creates the LDAP authentication provider (conditionally).
     * This bean is only created when app.ldap.enabled=true.
     * <p>
     * The LDAP provider authenticates users against an external LDAP server
     * and automatically synchronizes user information with the WiseMapping database.
     */
    @Bean
    @ConditionalOnProperty(name = "app.ldap.enabled", havingValue = "true")
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        logger.info("Initializing LDAP authentication provider with URL: {}", ldapProperties.getUrl());

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(
                ldapProperties,
                userService,
                userDetailsService,
                metricsService
        );

        // test LDAP connection on startup
        if (provider.testConnection()) {
            logger.info("LDAP authentication provider initialized successfully");
        } else {
            logger.warn("LDAP authentication provider initialized but connection test failed. " +
                    "Please verify LDAP configuration.");
        }

        return provider;
    }

    /**
     * Provider order:
     * 1. LDAP Provider (if enabled) - Handles LDAP users
     * 2. Database Provider - Handles DATABASE users
     * <p>
     * This order ensures that LDAP authentication is attempted first when enabled,
     * allowing organizations to use LDAP as their primary authentication method
     * while still supporting local DATABASE accounts (e.g., for admin users).
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration,
            @Autowired(required = false) LdapAuthenticationProvider ldapProvider) throws Exception {

        List<AuthenticationProvider> providers = new ArrayList<>();

        /**
         * Add LDAP provider first if enabled
         * This makes LDAP the preferred authentication method
         */
        if (ldapProvider != null && ldapProperties.isEnabled()) {
            providers.add(ldapProvider);
            logger.info("LDAP authentication enabled - LDAP will be tried first");
        }

        /**
         * Always add the database provider
         * This handles DATABASE users and acts as fallback
         */
        providers.add(dbAuthenticationProvider());

        // Add the logger information
        logger.info("AuthenticationManager configured with {} provider(s)", providers.size());

        // Create ProviderManager with our custom provider list
        ProviderManager providerManager = new ProviderManager(providers);

        // Don't erase credentials after authentication (needed for some flows)
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }
}
