package com.wisemapping.config;

import com.wisemapping.security.AuthenticationSuccessHandler;
import com.wisemapping.security.UserDetailsService;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    UserService userService;

    @Value("${admin.user}")
    String adminUser;

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder serviceMapper = new MvcRequestMatcher.Builder(introspector).servletPath("/service");
        return http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(serviceMapper.pattern(("/**"))))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/users/").permitAll()
                                .requestMatchers("/users/resetPassword").permitAll()
                                .requestMatchers("/oauth2/googlecallback").permitAll()
                                .requestMatchers("/oauth2/confirmaccountsync").permitAll()
                                .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                                .requestMatchers("/**").hasAnyRole("USER", "ADMIN")

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpBasic -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain mvcFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final AuthenticationSuccessHandler authenticationSuccessHandler = new AuthenticationSuccessHandler();
        authenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(false);
        authenticationSuccessHandler.setDefaultTargetUrl("/c/maps/");

        final MvcRequestMatcher.Builder restfullMapper = new MvcRequestMatcher.Builder(introspector).servletPath("/c/restful");
        final MvcRequestMatcher.Builder mvcMatcher = new MvcRequestMatcher.Builder(introspector).servletPath("/c");

        http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(restfullMapper.pattern(("/**"))).
                                requestMatchers(mvcMatcher.pattern(("/**"))))
                .authorizeHttpRequests(
                        (auth) ->
                                auth
                                        .requestMatchers("/login", "logout").permitAll()
                                        .requestMatchers("/registration", "registration-success", "/registration-google").permitAll()
                                        .requestMatchers("/forgot-password", "/forgot-password-success").permitAll()
                                        .requestMatchers("/maps/*/embed", "/maps/*/try", "/maps/*/public").permitAll()
                                        .requestMatchers("/restful/maps/*/document/xml-pub").permitAll()
                                        .requestMatchers("/**").hasAnyRole("USER", "ADMIN")
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
                                ).authenticationSuccessHandler(authenticationSuccessHandler)
                )
                .csrf((csrf) ->
                        csrf.ignoringRequestMatchers("/logout"));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain shareResourcesFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        return http.authorizeHttpRequests(
                (auth) ->
                        auth.requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
        ).build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        final UserDetailsService result = new UserDetailsService();
        result.setUserService(userService);
        result.setAdminUser(adminUser);
        return result;
    }
}
