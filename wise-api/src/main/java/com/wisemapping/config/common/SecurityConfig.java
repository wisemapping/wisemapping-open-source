package com.wisemapping.config.common;

import com.wisemapping.security.*;
import com.wisemapping.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

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

    @Bean
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();

        final MapAccessPermissionEvaluation permissionEvaluator = new MapAccessPermissionEvaluation(readAdvice, updateAdvice);
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return  DefaultPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider dbAuthenticationProvider() {
        final com.wisemapping.security.AuthenticationProvider provider =
                new com.wisemapping.security.AuthenticationProvider();
        provider.setEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        provider.setMetricsService(metricsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Expose the framework-built AuthenticationManager so tests and controllers can autowire it
        return configuration.getAuthenticationManager();
    }
}
