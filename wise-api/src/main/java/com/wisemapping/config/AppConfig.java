package com.wisemapping.config;

import com.wisemapping.dao.LabelManagerImpl;
import com.wisemapping.filter.JwtAuthenticationFilter;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.scheduler.HistoryCleanupScheduler;
import com.wisemapping.scheduler.InactiveUserSuspensionScheduler;
import com.wisemapping.scheduler.SpamDetectionScheduler;
import com.wisemapping.scheduler.SpamUserSuspensionScheduler;
import com.wisemapping.security.AuthenticationProvider;
import com.wisemapping.service.HistoryPurgeService;
import com.wisemapping.service.InactiveUserService;
import com.wisemapping.service.MindmapServiceImpl;
import com.wisemapping.service.SpamDetectionService;
import com.wisemapping.service.spam.ContactInfoSpamStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import com.wisemapping.model.Account;
import com.wisemapping.security.Utils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication(scanBasePackageClasses = { MindmapController.class, JwtAuthenticationFilter.class,
        AuthenticationProvider.class, MindmapServiceImpl.class, LabelManagerImpl.class,
        com.wisemapping.util.VelocityEngineWrapper.class, SpamDetectionService.class, SpamUserSuspensionScheduler.class,
        com.wisemapping.service.SpamDetectionBatchService.class, SpamDetectionScheduler.class,
        ContactInfoSpamStrategy.class, GlobalExceptionHandler.class,
        com.wisemapping.validator.HtmlContentValidator.class, InactiveUserService.class,
        InactiveUserSuspensionScheduler.class, HistoryCleanupScheduler.class, HistoryPurgeService.class })
@Import({ com.wisemapping.config.common.JPAConfig.class, com.wisemapping.config.common.SecurityConfig.class })
@EnableScheduling
@EnableAsync
@EnableWebSecurity
@Configuration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    @Value("${app.api.http-basic-enabled:false}")
    private boolean enableHttpBasic;

    @Value("${app.security.corsAllowedOrigins:}")
    private String corsAllowedOrigins;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain apiSecurityFilterChain(@NotNull final HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // enables WebMvcConfigurer CORS
                .securityMatcher("/**")
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/restful/authenticate").permitAll()
                        .requestMatchers("/api/restful/users/").permitAll()
                        .requestMatchers("/api/restful/app/config").permitAll()
                        .requestMatchers("/api/restful/maps/*/metadata").permitAll()
                        .requestMatchers("/api/restful/maps/*/document/xml-pub").permitAll()
                        .requestMatchers("/api/restful/users/resetPassword").permitAll()
                        .requestMatchers("/api/restful/users/activation").permitAll()
                        .requestMatchers("/api/restful/oauth2/googlecallback").permitAll()
                        .requestMatchers("/api/restful/oauth2/facebookcallback").permitAll()
                        .requestMatchers("/api/restful/oauth2/confirmaccountsync").permitAll()
                        .requestMatchers("/api/restful/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .logout(logout -> logout.permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        // Content Security Policy for HTML content
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'; frame-ancestors 'none';"))
                        // Prevent MIME type sniffing
                        .contentTypeOptions(Customizer.withDefaults())
                        // Prevent clickjacking
                        .frameOptions(frameOptions -> frameOptions.deny())
                        // HSTS (HTTP Strict Transport Security)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000))
                        // Referrer Policy
                        .referrerPolicy(referrerPolicy -> referrerPolicy
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // Custom headers
                        .addHeaderWriter((request, response) -> response.setHeader("Server", "WiseMapping")));

        // Http basic is mainly used by automation tests.
        if (enableHttpBasic) {
            http.httpBasic(withDefaults());
        }

        return http.build();
    }

    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        if (!corsAllowedOrigins.isEmpty()) {
            // Split comma-separated origins and trim whitespace
            String[] origins = corsAllowedOrigins.split(",");
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }

            registry.addMapping("/api/**")
                    .exposedHeaders("*")
                    .allowedHeaders("*")
                    .allowedMethods("*")
                    .allowedOrigins(origins)
                    .maxAge(3600);
        }
    }

    @Bean
    @Primary
    public LocaleResolver customLocaleResolver() {
        return new AcceptHeaderLocaleResolver() {
            @Override
            @NotNull
            public Locale resolveLocale(@NotNull HttpServletRequest request) {
                final Account user = Utils.getUser();
                Locale result;
                if (user != null && user.getLocale() != null) {
                    String locale = user.getLocale();
                    final String locales[] = locale.split("_");

                    Locale.Builder builder = new Locale.Builder().setLanguage(locales[0]);
                    if (locales.length > 1) {
                        builder.setRegion(locales[1]);
                    }
                    result = builder.build();
                } else {
                    result = super.resolveLocale(request);
                }
                return result;
            }
        };
    }
}
