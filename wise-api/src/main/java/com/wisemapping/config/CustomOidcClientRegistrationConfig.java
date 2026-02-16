/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration for custom OIDC client registration.
 * 
 * This configuration creates a ClientRegistrationRepository that includes
 * the custom OIDC provider alongside any existing OAuth2 providers
 * (Google, Facebook) that are configured via Spring Boot's standard
 * spring.security.oauth2.client.registration.* properties.
 */
@Configuration
public class CustomOidcClientRegistrationConfig {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private CustomOidcProperties customOidcProperties;

    /**
     * Creates a ClientRegistrationRepository that includes the custom OIDC provider.
     * This bean is only created when the custom OIDC provider is enabled and properly configured.
     * 
     * @param existingRepository The existing repository from Spring Boot auto-configuration (may be null)
     * @return A repository containing all configured OAuth2 providers
     */
    @Bean
    @ConditionalOnProperty(name = "app.oidc.custom.enabled", havingValue = "true")
    public ClientRegistrationRepository customOidcClientRegistrationRepository(
            @Autowired(required = false) ClientRegistrationRepository existingRepository) {
        
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // Add existing registrations (Google, Facebook, etc.) if they exist
        if (existingRepository != null) {
            try {
                // Try to get common provider registrations
                String[] commonProviders = {"google", "facebook", "github", "okta"};
                for (String providerId : commonProviders) {
                    try {
                        ClientRegistration registration = existingRepository.findByRegistrationId(providerId);
                        if (registration != null) {
                            registrations.add(registration);
                            logger.debug("Added existing OAuth2 provider: {}", providerId);
                        }
                    } catch (Exception e) {
                        // Provider not configured, skip
                        logger.debug("Provider {} not configured: {}", providerId, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error accessing existing client registrations: {}", e.getMessage());
            }
        }
        
        // Add custom OIDC provider if properly configured
        if (customOidcProperties.isConfigurationValid()) {
            try {
                ClientRegistration customRegistration = createCustomOidcRegistration();
                registrations.add(customRegistration);
                logger.info("Custom OIDC provider '{}' registered successfully", customOidcProperties.getName());
            } catch (Exception e) {
                logger.error("Failed to register custom OIDC provider: {}", e.getMessage(), e);
                throw new IllegalStateException("Failed to configure custom OIDC provider", e);
            }
        } else {
            logger.warn("Custom OIDC provider is enabled but not properly configured. " +
                       "Please check app.oidc.custom.* properties.");
        }
        
        if (registrations.isEmpty()) {
            logger.warn("No OAuth2 client registrations found. OAuth2 login will not be available.");
            return null;
        }
        
        logger.info("Configured {} OAuth2 provider(s)", registrations.size());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    /**
     * Creates a ClientRegistration for the custom OIDC provider.
     * 
     * @return ClientRegistration configured for the custom OIDC provider
     */
    private ClientRegistration createCustomOidcRegistration() {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(customOidcProperties.getRegistrationId())
                .clientId(customOidcProperties.getClientId())
                .clientSecret(customOidcProperties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(Arrays.asList(customOidcProperties.getScope().split(",")))
                .clientName(customOidcProperties.getName())
                .userNameAttributeName(customOidcProperties.getUserNameAttribute());

        // Set issuer URI for OIDC discovery
        if (customOidcProperties.getIssuerUri() != null && !customOidcProperties.getIssuerUri().trim().isEmpty()) {
            builder.issuerUri(customOidcProperties.getIssuerUri());
        }

        // Set specific endpoints if provided (overrides discovery)
        if (customOidcProperties.getAuthorizationUri() != null && !customOidcProperties.getAuthorizationUri().trim().isEmpty()) {
            builder.authorizationUri(customOidcProperties.getAuthorizationUri());
        }
        
        if (customOidcProperties.getTokenUri() != null && !customOidcProperties.getTokenUri().trim().isEmpty()) {
            builder.tokenUri(customOidcProperties.getTokenUri());
        }
        
        if (customOidcProperties.getUserInfoUri() != null && !customOidcProperties.getUserInfoUri().trim().isEmpty()) {
            builder.userInfoUri(customOidcProperties.getUserInfoUri());
        }
        
        if (customOidcProperties.getJwkSetUri() != null && !customOidcProperties.getJwkSetUri().trim().isEmpty()) {
            builder.jwkSetUri(customOidcProperties.getJwkSetUri());
        }

        return builder.build();
    }
}