# Generic OpenID Connect (OIDC) login

WiseMapping can delegate sign-in to any OpenID Connect provider — Keycloak, Authentik,
Authelia, Okta, Microsoft Entra ID, Zitadel, … — next to (or instead of) Google and Facebook.
The provider is configured with standard Spring Security properties under the registration id
**`oidc`**; nothing is enabled unless that registration exists.

## How it works

1. The login page shows a **"Sign in with {label}"** button when `/api/restful/app/config`
   returns `oidcOauth2Enabled: true`. The label comes from `app.oauth.oidc.label`.
2. The button opens `{api-base-url}/oauth2/authorization/oidc`; Spring Security runs the
   authorization-code flow against the provider.
3. On the callback (`{api-base-url}/login/oauth2/code/oidc`) the user is matched **by e-mail**:
   - **no account** → an account of type `OIDC` is created (no local password);
   - **an `OIDC` account** → signed in;
   - **a local (`DATABASE`) account** → the user is asked to confirm the link once; the account
     then becomes `OIDC` and its local password is cleared;
   - **an account bound to another provider** (Google, Facebook, LDAP) → refused.

Provisioning on first login does **not** depend on `app.registration.enabled`: an instance that
lets only its identity provider in can close the public sign-up form
(`app.registration.enabled: false`) and still create accounts on first OIDC login.

## Configuration

Endpoints are discovered from the issuer (`<issuer-uri>/.well-known/openid-configuration`).

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          oidc:
            client-id: wisemapping
            client-secret: <client secret>
            authorization-grant-type: authorization_code   # required for a custom registration id
            scope: openid,profile,email
            redirect-uri: "${app.site.api-base-url}/login/oauth2/code/{registrationId}"
        provider:
          oidc:
            issuer-uri: https://sso.example.com/realms/example
            user-name-attribute: sub

app:
  oauth:
    oidc:
      label: Example SSO          # button text: "Sign in with Example SSO"
```

The same settings as environment variables (Docker):

```sh
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_CLIENT_ID=wisemapping
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_CLIENT_SECRET=<client secret>
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_AUTHORIZATION_GRANT_TYPE=authorization_code
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_SCOPE=openid,profile,email
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}
SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_OIDC_ISSUER_URI=https://sso.example.com/realms/example
SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_OIDC_USER_NAME_ATTRIBUTE=sub
APP_OAUTH_OIDC_LABEL="Example SSO"
```

The provider must return the `email` claim (scope `email`). `given_name` and `family_name`
fill the account's first and last name.

### Keycloak

- Client: *OpenID Connect*, *Client authentication* on, *Standard flow* only.
- Valid redirect URI: `https://wm.example.com/login/oauth2/code/oidc`.
- Issuer: `https://keycloak.example.com/realms/<realm>`.

### Authentik

- Provider: *OAuth2/OpenID Provider*, client type *Confidential*, scopes `openid`, `email`,
  `profile`.
- Redirect URI (strict): `https://wm.example.com/login/oauth2/code/oidc`.
- Issuer: `https://authentik.example.com/application/o/<application-slug>/`
  (keep the trailing slash: it is part of the issuer Authentik announces).

## Troubleshooting

- **`authorizationGrantType cannot be null` at startup** — add
  `authorization-grant-type: authorization_code`; Spring only presets it for the providers it
  knows (Google, Facebook, GitHub, Okta).
- **`redirect_uri` mismatch** — the URI registered in the provider must be exactly
  `{api-base-url}/login/oauth2/code/oidc`.
- **Issuer mismatch** — `issuer-uri` must equal the `issuer` field of the discovery document,
  character for character.
