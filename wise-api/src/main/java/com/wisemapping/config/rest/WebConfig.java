package com.wisemapping.config.rest;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.security.corsAllowedOrigins:}")
    private String corsAllowedOrigins;

    @Override
    public void addCorsMappings(@NotNull CorsRegistry registry) {
        if (!corsAllowedOrigins.isEmpty()) {
            registry.addMapping("/api/**")
                    .exposedHeaders("*")
                    .allowedHeaders("*")
                    .allowedOrigins(corsAllowedOrigins)
                    .maxAge(3600);
        }
    }
}