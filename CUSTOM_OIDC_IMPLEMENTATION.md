# Custom OIDC Provider Implementation for WiseMapping

This document describes the implementation of custom OIDC (OpenID Connect) provider support in WiseMapping, allowing integration with any OIDC-compliant identity provider.

## Overview

WiseMapping now supports custom OIDC providers in addition to the existing Google and Facebook OAuth2 integrations. This allows organizations to integrate with their own identity providers such as Keycloak, Auth0, Okta, Azure AD, or any other OIDC-compliant system.

## Implementation Details

### Files Modified/Created

1. **AuthenticationType.java** - Added `CUSTOM_OIDC` authentication type
2. **CustomOidcProperties.java** - Configuration properties for custom OIDC provider
3. **CustomOidcClientRegistrationConfig.java** - Dynamic client registration configuration
4. **OAuth2AuthenticationSuccessHandler.java** - Updated to handle custom OIDC providers
5. **AppConfig.java** - Updated OAuth2 configuration to use custom client registration repository
6. **RestAppConfig.java** - Added custom OIDC properties to REST API configuration
7. **AppController.java** - Updated to expose custom OIDC settings in API
8. **application-custom-oidc-example.yml** - Example configuration file

### Key Components

#### 1. CustomOidcProperties
Configuration class that handles all custom OIDC provider settings:
- Provider name and display settings
- OAuth2 client credentials
- OIDC discovery endpoint
- Manual endpoint configuration (optional)
- User attribute mapping

#### 2. CustomOidcClientRegistrationConfig
Dynamically creates Spring Security OAuth2 client registrations:
- Supports OIDC discovery
- Falls back to manual endpoint configuration
- Integrates with existing OAuth2 providers

#### 3. Enhanced OAuth2AuthenticationSuccessHandler
Updated to handle custom OIDC providers:
- Maps custom OIDC to `CUSTOM_OIDC` authentication type
- Supports standard OIDC user attributes
- Maintains compatibility with existing providers

## Configuration

### Basic Configuration

Add the following to your `application.yml`:

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "My Company SSO"
      client-id: "your-client-id"
      client-secret: "your-client-secret"
      issuer-uri: "https://auth.company.com/realms/wisemapping"
```

### Advanced Configuration

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "My Company SSO"
      client-id: "your-client-id"
      client-secret: "your-client-secret"
      issuer-uri: "https://auth.company.com/realms/wisemapping"
      scope: "openid,profile,email,groups"
      user-name-attribute: "preferred_username"
      # Manual endpoint configuration (optional)
      authorization-uri: "https://auth.company.com/auth"
      token-uri: "https://auth.company.com/token"
      user-info-uri: "https://auth.company.com/userinfo"
      jwk-set-uri: "https://auth.company.com/certs"
```

### Configuration Properties

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `enabled` | Yes | `false` | Enable/disable custom OIDC provider |
| `name` | No | `"Custom OIDC"` | Display name for the provider |
| `client-id` | Yes | - | OAuth2 client ID |
| `client-secret` | Yes | - | OAuth2 client secret |
| `issuer-uri` | Yes | - | OIDC issuer URI (discovery endpoint) |
| `scope` | No | `"openid,profile,email"` | OAuth2 scopes to request |
| `user-name-attribute` | No | `"sub"` | User attribute to use as username |
| `authorization-uri` | No | Auto-discovered | Authorization endpoint URL |
| `token-uri` | No | Auto-discovered | Token endpoint URL |
| `user-info-uri` | No | Auto-discovered | UserInfo endpoint URL |
| `jwk-set-uri` | No | Auto-discovered | JWK Set endpoint URL |

## Provider Examples

### Keycloak

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "Keycloak SSO"
      client-id: "wisemapping"
      client-secret: "your-secret"
      issuer-uri: "https://keycloak.company.com/realms/wisemapping"
      user-name-attribute: "preferred_username"
```

### Auth0

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "Auth0 SSO"
      client-id: "your-auth0-client-id"
      client-secret: "your-auth0-client-secret"
      issuer-uri: "https://your-domain.auth0.com/"
      user-name-attribute: "email"
```

### Okta

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "Okta SSO"
      client-id: "your-okta-client-id"
      client-secret: "your-okta-client-secret"
      issuer-uri: "https://your-domain.okta.com/oauth2/default"
      user-name-attribute: "email"
```

### Azure AD

```yaml
app:
  oidc:
    custom:
      enabled: true
      name: "Azure AD SSO"
      client-id: "your-azure-client-id"
      client-secret: "your-azure-client-secret"
      issuer-uri: "https://login.microsoftonline.com/your-tenant-id/v2.0"
      user-name-attribute: "email"
```

## Authentication Flow

1. User clicks on custom OIDC login button in WiseMapping UI
2. User is redirected to `/oauth2/authorization/custom-oidc`
3. Spring Security redirects to the configured OIDC provider
4. User authenticates with the OIDC provider
5. Provider redirects back to WiseMapping with authorization code
6. WiseMapping exchanges code for tokens and retrieves user info
7. `OAuth2AuthenticationSuccessHandler` processes the authentication:
   - Creates or updates user account with `CUSTOM_OIDC` authentication type
   - Generates JWT token for WiseMapping session
   - Redirects to WiseMapping frontend

## User Account Management

- New users are automatically created with `AuthenticationType.CUSTOM_OIDC`
- Existing users with `AuthenticationType.DATABASE` can be linked to OIDC accounts
- User information is synchronized from OIDC provider attributes:
  - Email from `email` claim
  - First name from `given_name` or `first_name` claim
  - Last name from `family_name` or `last_name` claim

## API Integration

The custom OIDC configuration is exposed through the `/api/restful/app/config` endpoint:

```json
{
  "customOidcEnabled": true,
  "customOidcName": "My Company SSO",
  "customOidcUrl": "https://api.wisemapping.com/oauth2/authorization/custom-oidc"
}
```

## Security Considerations

1. **Client Secret Protection**: Store client secrets securely using environment variables or encrypted configuration
2. **HTTPS Required**: All OIDC communication must use HTTPS in production
3. **Token Validation**: JWT tokens from OIDC provider are validated using JWK Set
4. **Scope Limitation**: Request only necessary scopes from OIDC provider
5. **User Attribute Validation**: Validate user attributes received from OIDC provider

## Troubleshooting

### Common Issues

1. **Discovery Endpoint Not Found**
   - Verify `issuer-uri` is correct and accessible
   - Check that `/.well-known/openid_configuration` endpoint exists

2. **Invalid Client Credentials**
   - Verify `client-id` and `client-secret` are correct
   - Check client configuration in OIDC provider

3. **Scope Issues**
   - Ensure requested scopes are allowed by OIDC provider
   - Verify `openid` scope is included

4. **User Attribute Mapping**
   - Check `user-name-attribute` matches available claims
   - Verify user info endpoint returns expected attributes

### Logging

Enable debug logging for OAuth2 components:

```yaml
logging:
  level:
    org.springframework.security.oauth2: DEBUG
    com.wisemapping.security: DEBUG
    com.wisemapping.config: DEBUG
```

## Testing

To test the implementation:

1. Configure a test OIDC provider (e.g., Keycloak)
2. Update `application.yml` with test configuration
3. Start WiseMapping application
4. Navigate to login page and verify custom OIDC option appears
5. Test authentication flow end-to-end
6. Verify user account creation and JWT token generation

## Migration from Existing OAuth2 Providers

Users with existing Google or Facebook accounts can continue using those providers. The custom OIDC provider works alongside existing OAuth2 integrations without conflicts.

## Future Enhancements

Potential future improvements:
- Support for multiple custom OIDC providers
- Advanced user attribute mapping
- Group/role synchronization from OIDC provider
- SAML 2.0 support alongside OIDC
- Dynamic provider registration via admin interface