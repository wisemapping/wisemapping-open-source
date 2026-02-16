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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom OIDC configuration properties for WiseMapping.
 * 
 * This class configures a custom OIDC provider that can be used alongside
 * or instead of the built-in Google and Facebook OAuth2 providers.
 * 
 * Configuration properties (from app.oidc.custom.*):
 * - enabled: Enable/disable custom OIDC authentication
 * - name: Display name for the provider
 * - clientId: OAuth2 client ID
 * - clientSecret: OAuth2 client secret
 * - issuerUri: OIDC issuer URI (discovery endpoint)
 * - authorizationUri: Authorization endpoint URL (optional, auto-discovered if not set)
 * - tokenUri: Token endpoint URL (optional, auto-discovered if not set)
 * - userInfoUri: UserInfo endpoint URL (optional, auto-discovered if not set)
 * - jwkSetUri: JWK Set endpoint URL (optional, auto-discovered if not set)
 * - userNameAttribute: Attribute to use as username (default: "sub")
 * - scope: OAuth2 scopes to request (default: "openid,profile,email")
 * 
 * Example configuration in application.yml:
 * 
 * app:
 *   oidc:
 *     custom:
 *       enabled: true
 *       name: "My Company SSO"
 *       client-id: "your-client-id"
 *       client-secret: "your-client-secret"
 *       issuer-uri: "https://auth.company.com/realms/wisemapping"
 *       scope: "openid,profile,email"
 *       user-name-attribute: "preferred_username"
 */
@Configuration
@ConfigurationProperties(prefix = "app.oidc.custom")
public class CustomOidcProperties {

    /**
     * Enable/disable custom OIDC authentication.
     * When enabled, a custom OIDC provider will be configured alongside
     * Google and Facebook OAuth2 providers.
     */
    private boolean enabled = false;

    /**
     * Display name for the custom OIDC provider.
     * This will be shown in the UI and logs.
     */
    private String name = "Custom OIDC";

    /**
     * OAuth2 client ID for the custom OIDC provider.
     * This is obtained from your OIDC provider's configuration.
     */
    private String clientId;

    /**
     * OAuth2 client secret for the custom OIDC provider.
     * This is obtained from your OIDC provider's configuration.
     */
    private String clientSecret;

    /**
     * OIDC issuer URI (discovery endpoint).
     * This is the base URL of your OIDC provider where the
     * .well-known/openid_configuration endpoint can be found.
     * Example: https://auth.company.com/realms/wisemapping
     */
    private String issuerUri;

    /**
     * Authorization endpoint URL.
     * If not specified, it will be auto-discovered from the issuer URI.
     */
    private String authorizationUri;

    /**
     * Token endpoint URL.
     * If not specified, it will be auto-discovered from the issuer URI.
     */
    private String tokenUri;

    /**
     * UserInfo endpoint URL.
     * If not specified, it will be auto-discovered from the issuer URI.
     */
    private String userInfoUri;

    /**
     * JWK Set endpoint URL.
     * If not specified, it will be auto-discovered from the issuer URI.
     */
    private String jwkSetUri;

    /**
     * Attribute to use as username from the OIDC user info.
     * Common values: "sub", "preferred_username", "email"
     * Default: "sub"
     */
    private String userNameAttribute = "sub";

    /**
     * OAuth2 scopes to request.
     * Default: "openid,profile,email"
     */
    private String scope = "openid,profile,email";

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Check if all required properties are configured.
     * @return true if the configuration is valid and complete
     */
    public boolean isConfigurationValid() {
        return enabled && 
               clientId != null && !clientId.trim().isEmpty() &&
               clientSecret != null && !clientSecret.trim().isEmpty() &&
               issuerUri != null && !issuerUri.trim().isEmpty();
    }

    /**
     * Get the registration ID for Spring Security OAuth2.
     * @return the registration ID to use with Spring Security
     */
    public String getRegistrationId() {
        return "custom-oidc";
    }
}