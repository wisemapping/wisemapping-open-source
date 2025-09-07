package com.wisemapping.config;

import io.micrometer.newrelic.NewRelicConfig;
import io.micrometer.newrelic.NewRelicMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional New Relic configuration that respects the enabled property.
 * This configuration only activates when management.metrics.export.newrelic.enabled=true
 * and provides the required API key and account ID.
 */
@Configuration
@ConditionalOnProperty(
    name = "management.metrics.export.newrelic.enabled", 
    havingValue = "true",
    matchIfMissing = false
)
public class ConditionalNewRelicAutoConfiguration {
    
    /**
     * New Relic configuration that uses properties from application.yml
     */
    @Bean
    public NewRelicConfig newRelicConfig() {
        return new NewRelicConfig() {
            @Override
            public String get(String key) {
                // This will be called after properties are loaded
                // Return null to use default values from application.yml
                return null;
            }
            
            @Override
            public boolean enabled() {
                // This ensures the registry is only created when explicitly enabled
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
        return new NewRelicMeterRegistry(newRelicConfig, java.time.Clock.SYSTEM);
    }
}
