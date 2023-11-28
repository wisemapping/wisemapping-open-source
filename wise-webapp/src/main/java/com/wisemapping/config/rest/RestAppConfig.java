package com.wisemapping.config.rest;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


@SpringBootApplication
@EnableWebSecurity
@ComponentScan("com.wisemapping.rest")
public class RestAppConfig {
    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector).servletPath("/service");
        return http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(matcher.pattern(("/**"))))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(matcher.pattern("/users/")).permitAll()
                                .requestMatchers(matcher.pattern("/users/resetPassword")).permitAll()
                                .requestMatchers(matcher.pattern("/oauth2/googlecallback")).permitAll()
                                .requestMatchers(matcher.pattern("/oauth2/confirmaccountsync")).permitAll()
                                .requestMatchers(matcher.pattern("/admin/**")).hasAnyRole("ADMIN")
                                .requestMatchers(matcher.pattern("/**")).hasAnyRole("USER", "ADMIN")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpBasic -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

}
