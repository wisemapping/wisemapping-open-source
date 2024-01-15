package com.wisemapping.config.common;

import com.wisemapping.security.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private ReadSecurityAdvise readAdvice;

    @Autowired
    private UpdateSecurityAdvise updateAdvice;

    @Autowired
    private UserDetailsService userDetailsService;

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
        return createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider googleAuthenticationProvider() {
        return new GoogleAuthenticationProvider(userDetailsService);

    }

    @Bean
    public AuthenticationProvider dbAuthenticationProvider() {
        com.wisemapping.security.AuthenticationProvider provider =
                new com.wisemapping.security.AuthenticationProvider();
        provider.setEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(@NotNull HttpSecurity http)
            throws Exception {
        final AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        builder.authenticationProvider(dbAuthenticationProvider());
        builder.authenticationProvider(googleAuthenticationProvider());

        return builder.build();
    }
}
