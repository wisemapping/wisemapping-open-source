package com.wisemapping.config.rest;

import com.wisemapping.rest.MindmapController;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.springframework.security.config.Customizer.withDefaults;


@SpringBootApplication(scanBasePackageClasses = MindmapController.class)
@Import({ServletConfig.class, InterceptorsConfig.class})
@EnableWebSecurity
public class RestAppConfig {
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    SecurityFilterChain apiSecurityFilterChain(@NotNull final HttpSecurity http, @NotNull final MvcRequestMatcher.Builder mvc) throws Exception {
        return http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(mvc.pattern("/api/restfull/users/")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restfull/users/resetPassword")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restfull/oauth2/googlecallback")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restfull/oauth2/confirmaccountsync")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restfull/admin/**")).hasAnyRole("ADMIN")
                        .requestMatchers(mvc.pattern("/**")).hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .build();
    }
}
