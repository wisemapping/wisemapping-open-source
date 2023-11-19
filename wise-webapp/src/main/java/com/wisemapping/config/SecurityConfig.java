package com.wisemapping.config;

import com.wisemapping.security.AuthenticationSuccessHandler;
import com.wisemapping.security.MapAccessPermissionEvaluation;
import com.wisemapping.security.UserDetailsService;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
    @Order(1)
    public SecurityFilterChain embeddedDisabledXOrigin(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder mvcMatcher = new MvcRequestMatcher.Builder(introspector).servletPath("/c");
        http
                .securityMatchers((matchers) ->
                        matchers.requestMatchers(mvcMatcher.pattern(("/maps/*/embed"))))
                .authorizeHttpRequests(
                        (auth) -> auth.requestMatchers(mvcMatcher.pattern("/maps/*/embed")).permitAll())
                .headers((header -> header.frameOptions()
                        .disable()
                ))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
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
                                .requestMatchers(serviceMapper.pattern("/users/")).permitAll()
                                .requestMatchers(serviceMapper.pattern("/users/resetPassword")).permitAll()
                                .requestMatchers(serviceMapper.pattern("/oauth2/googlecallback")).permitAll()
                                .requestMatchers(serviceMapper.pattern("/oauth2/confirmaccountsync")).permitAll()
                                .requestMatchers(serviceMapper.pattern("/admin/**")).hasAnyRole("ADMIN")
                                .requestMatchers(serviceMapper.pattern("/**")).hasAnyRole("USER", "ADMIN")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpBasic -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(3)
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
                                        .requestMatchers(mvcMatcher.pattern("/login")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/logout")).permitAll()

                                        .requestMatchers(mvcMatcher.pattern("/registration")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/registration-success")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/registration-google")).permitAll()

                                        .requestMatchers(mvcMatcher.pattern("/forgot-password")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/forgot-password-success")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/maps/*/try")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/maps/*/public")).permitAll()
                                        .requestMatchers(restfullMapper.pattern("/maps/*/document/xml-pub")).permitAll()
                                        .requestMatchers(mvcMatcher.pattern("/**")).hasAnyRole("USER", "ADMIN")
                                        .requestMatchers(restfullMapper.pattern("/**")).hasAnyRole("USER", "ADMIN")
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
                ).headers((header -> header.frameOptions()
                        .disable()
                ))
                .csrf((csrf) ->
                        csrf.ignoringRequestMatchers(mvcMatcher.pattern("/logout")));

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain shareResourcesFilterChain(@NotNull final HttpSecurity http, @NotNull final HandlerMappingIntrospector introspector) throws Exception {
        final MvcRequestMatcher.Builder restfullMapper = new MvcRequestMatcher.Builder(introspector);

        return http.authorizeHttpRequests(
                (auth) ->
                        auth.requestMatchers(restfullMapper.pattern("/static/**")).permitAll().
                                requestMatchers(restfullMapper.pattern("/css/**")).permitAll().
                                requestMatchers(restfullMapper.pattern("/js/**")).permitAll().
                                requestMatchers(restfullMapper.pattern("/images/**")).permitAll().
                                requestMatchers(restfullMapper.pattern("/*")).permitAll()
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
