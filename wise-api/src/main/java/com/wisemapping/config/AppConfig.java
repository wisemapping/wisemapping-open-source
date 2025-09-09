package com.wisemapping.config;

import com.wisemapping.dao.LabelManagerImpl;
import com.wisemapping.filter.JwtAuthenticationFilter;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.security.AuthenticationProvider;
import com.wisemapping.service.MindmapServiceImpl;
import com.wisemapping.util.VelocityEngineUtils;
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import com.wisemapping.model.Account;
import com.wisemapping.security.Utils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication(
    scanBasePackageClasses = {MindmapController.class, JwtAuthenticationFilter.class, AuthenticationProvider.class, MindmapServiceImpl.class, LabelManagerImpl.class, VelocityEngineUtils.class, com.wisemapping.service.SpamDetectionService.class, com.wisemapping.scheduler.SpamUserSuspensionScheduler.class, com.wisemapping.service.SpamDetectionBatchService.class, com.wisemapping.scheduler.SpamDetectionScheduler.class, com.wisemapping.service.spam.ContactInfoSpamStrategy.class, com.wisemapping.config.PropertyTestConfiguration.class, com.wisemapping.config.NewRelicAutoConfiguration.class}
)
@Import({com.wisemapping.config.common.JPAConfig.class, com.wisemapping.config.common.SecurityConfig.class})
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
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    SecurityFilterChain apiSecurityFilterChain(@NotNull final HttpSecurity http, @NotNull final MvcRequestMatcher.Builder mvc) throws Exception {
        http
                .cors(Customizer.withDefaults()) // enables WebMvcConfigurer CORS
                .securityMatcher("/**")
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(mvc.pattern("/error")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/authenticate")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/users/")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/app/config")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/maps/*/metadata")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/maps/*/document/xml-pub")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/users/resetPassword")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/oauth2/googlecallback")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/oauth2/confirmaccountsync")).permitAll()
                        .requestMatchers(mvc.pattern("/api/restful/admin/**")).hasAnyRole("ADMIN")
                        .requestMatchers(mvc.pattern("/**")).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Http basic is mainly used by automation tests.
        if (enableHttpBasic) {
            http.httpBasic(withDefaults());
        }

        return http.build();
    }


    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        if (!corsAllowedOrigins.isEmpty()) {
            registry.addMapping("/api/**")
                    .exposedHeaders("*")
                    .allowedHeaders("*")
                    .allowedMethods("*")
                    .allowedOrigins(corsAllowedOrigins)
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
                        builder.setVariant(locales[1]);
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
