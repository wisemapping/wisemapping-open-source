package com.wisemapping.config.common;

import io.github.resilience4j.spring6.fallback.configure.FallbackConfiguration;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfiguration;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfigurationProperties;
import io.github.resilience4j.spring6.spelresolver.configure.SpelResolverConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SpelResolverConfiguration.class,
        FallbackConfiguration.class,
        RateLimiterConfiguration.class
})
public class Resilience4jConfig {
    @Bean
    @ConfigurationProperties(prefix = "resilience4j.ratelimiter")
    public RateLimiterConfigurationProperties rateLimiterConfigurationProperties() {
        return new RateLimiterConfigurationProperties();
    }
}
