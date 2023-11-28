package com.wisemapping.config.mvc;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


@Configuration
@EnableWebSecurity
public class MvcSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain embeddedDisabledXOrigin(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector);

        http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(matcher.pattern("c/maps/*/embed")))
                .authorizeHttpRequests(
                        (auth) -> auth.requestMatchers(matcher.pattern(("c/maps/*/embed"))).permitAll())
                .headers((header -> header.frameOptions()
                        .disable()
                ))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain mvcFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector);
        http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(matcher.pattern("/c/**")))
                .authorizeHttpRequests(
                        (auth) ->
                                auth
                                        .requestMatchers(matcher.pattern("/c/login")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/logout")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/registration")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/registration-success")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/registration-google")).permitAll()

                                        .requestMatchers(matcher.pattern("/c/forgot-password")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/forgot-password-success")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/maps/*/try")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/maps/*/public")).permitAll()
                                        .requestMatchers(matcher.pattern("/c/**")).hasAnyRole("USER", "ADMIN")
                                        .anyRequest().authenticated())
                .formLogin((loginForm) ->
                        loginForm.loginPage("/c/login")
                                .loginProcessingUrl("/c/perform-login")
                                .defaultSuccessUrl("/c/maps/")
                                .failureUrl("/c/login?login_error=2"))
                .logout((logout) ->
                        logout
                                .logoutUrl("/c/logout")
                                .logoutSuccessUrl("/c/login")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                ).rememberMe(remember ->
                        remember
                                .tokenValiditySeconds(2419200)
                                .rememberMeParameter("remember-me"
                                )
                ).headers((header -> header.frameOptions()
                        .disable()
                ))
                .csrf((csrf) ->
                        csrf.ignoringRequestMatchers(matcher.pattern("/c/logout")));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain shareResourcesFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder matcher = new MvcRequestMatcher.Builder(introspector);

        return http.authorizeHttpRequests(
                (auth) ->
                        auth.requestMatchers(matcher.pattern("/static/**")).permitAll().
                                requestMatchers(matcher.pattern("/css/**")).permitAll().
                                requestMatchers(matcher.pattern("/js/**")).permitAll().
                                // @todo: Wht this is required ...
                                requestMatchers(matcher.pattern("/WEB-INF/jsp/*.jsp")).permitAll().
                                requestMatchers(matcher.pattern("/images/**")).permitAll().
                                requestMatchers(matcher.pattern("/*")).permitAll()
        ).build();
    }
}
