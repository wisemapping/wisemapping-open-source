package com.wisemapping.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.newrelic.NewRelicConfig;
import io.micrometer.newrelic.NewRelicMeterRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Conditional New Relic configuration that respects the enabled property.
 * This configuration only activates when
 * management.metrics.export.newrelic.enabled=true
 * and provides the required API key and account ID.
 */
@Configuration
@ConditionalOnProperty(name = "management.metrics.export.newrelic.enabled", havingValue = "true", matchIfMissing = false)
public class NewRelicAutoConfiguration {
    private static final Logger logger = LogManager.getLogger();

    @Value("${management.metrics.export.newrelic.api-key:}")
    private String apiKey;

    @Value("${management.metrics.export.newrelic.account-id:}")
    private String accountId;

    @Value("${management.metrics.export.newrelic.uri:https://metric-api.newrelic.com/metric/v1}")
    private String uri;

    public NewRelicAutoConfiguration() {
        logger.info("New Relic metrics export is ENABLED");
    }

    /**
     * New Relic configuration that uses properties from application.yml
     */
    @Bean
    public NewRelicConfig newRelicConfig() {
        logger.info("Creating NewRelicConfig bean");
        
        // Validate required properties
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("NewRelic API key is required but not provided. Set management.metrics.export.newrelic.api-key");
        }
        if (!StringUtils.hasText(accountId)) {
            throw new IllegalArgumentException("NewRelic Account ID is required but not provided. Set management.metrics.export.newrelic.account-id");
        }
        
        logger.info("NewRelic configuration validated - API key and Account ID provided");
        
        return new NewRelicConfig() {
            @Override
            public String get(String key) {
                // Return the actual configuration values
                switch (key) {
                    case "newrelic.apiKey":
                        return apiKey;
                    case "newrelic.accountId":
                        return accountId;
                    case "newrelic.uri":
                        return uri;
                    default:
                        // Return null to use defaults for other properties
                        return null;
                }
            }

            @Override
            public boolean enabled() {
                return true;
            }
        };
    }

    /**
     * New Relic Meter Registry that only gets created when enabled.
     * This replaces the auto-configured registry and respects your settings.
     */
    @Bean
    public NewRelicMeterRegistry newRelicMeterRegistry(NewRelicConfig newRelicConfig) {
        // This will only be called when enabled=true and properties are loaded
        logger.info("New Relic Meter Registry created");

        return new NewRelicMeterRegistry(newRelicConfig, Clock.SYSTEM);
    }
}
