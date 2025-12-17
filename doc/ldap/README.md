# LDAP Authentication Documentation

## Overview

This documentation describes the LDAP/Active Directory authentication integration in WiseMapping API, allowing users to log in with their enterprise credentials.

---

## Modified/Created Files

### 1. Maven Dependencies

**File:** `wise-api/pom.xml`

Added Spring LDAP dependencies:

```xml
<!-- Spring LDAP Core -->
<dependency>
    <groupId>org.springframework.ldap</groupId>
    <artifactId>spring-ldap-core</artifactId>
</dependency>

<!-- Spring Security LDAP -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-ldap</artifactId>
</dependency>

<!-- UnboundID LDAP SDK (testing only) -->
<dependency>
    <groupId>com.unboundid</groupId>
    <artifactId>unboundid-ldapsdk</artifactId>
    <scope>test</scope>
</dependency>
```

---

### 2. LDAP Properties Configuration

**Files:** 
- `wise-api/src/main/java/com/wisemapping/config/LdapProperties.java`
- `wise-api/src/main/resources/application.yml`

Spring Boot configuration class that maps LDAP properties from the YAML file.

**Property Structure:**

WiseMapping uses a two-tier property structure:
1. **Spring Boot Standard Properties** (`spring.ldap.*`) - Basic LDAP connection settings
2. **WiseMapping Custom Extensions** (`app.ldap.*`) - Authentication and attribute mapping

#### Spring Boot Standard Properties (`spring.ldap.*`)

| Property | Description | Default Value |
|----------|-------------|---------------|
| `urls` | LDAP server URL(s) | `ldap://localhost:389` |
| `base` | Base DN for searches | `dc=example,dc=com` |
| `username` | Service account DN (bind account) | `""` (anonymous) |
| `password` | Service account password | `""` |

#### WiseMapping Custom Extensions (`app.ldap.*`)

| Property | Description | Default Value |
|----------|-------------|---------------|
| `enabled` | Enable/disable LDAP authentication | `false` |
| `user-dn-patterns` | Pattern for constructing user DN | `uid={0},ou=users` |
| `user-search-base` | User search base (relative to base) | `ou=users` |
| `user-search-filter` | LDAP filter for user search | `(uid={0})` |
| `group-search-base` | Group search base | `ou=groups` |
| `group-search-filter` | LDAP filter for group search | `(member={0})` |
| `email-attribute` | LDAP attribute for email | `mail` |
| `firstname-attribute` | LDAP attribute for first name | `givenName` |
| `lastname-attribute` | LDAP attribute for last name | `sn` |
| `password-compare` | Use password comparison | `false` |
| `pooled` | Enable connection pooling | `true` |
| `connect-timeout` | Connection timeout (ms) | `5000` |
| `read-timeout` | Read timeout (ms) | `10000` |

**Attribute Mapping Details:**

The attribute mappings (`email-attribute`, `firstname-attribute`, `lastname-attribute`) are critical for user synchronization:

- **email-attribute**: Used as the primary user identifier in WiseMapping
  - Common values: `mail` (OpenLDAP), `userPrincipalName` (Active Directory)
  - If not found, WiseMapping constructs email from username@domain
  
- **firstname-attribute**: User's given name
  - Common values: `givenName` (standard), `firstName`, `gn`
  
- **lastname-attribute**: User's surname/family name
  - Common values: `sn` (standard - "surname"), `surname`, `lastName`, `familyName`

---

### 3. LDAP Authentication Provider

**File:** `wise-api/src/main/java/com/wisemapping/security/AuthenticationProviderLDAP.java`

Spring Security provider that handles LDAP authentication.

**Features:**

- User authentication against an LDAP/AD server
- Automatic WiseMapping account creation on first login
- User attribute synchronization (first name, last name, email) on each login
- Fallback to DATABASE authentication if LDAP server is unavailable
- Network error and invalid credentials handling

**Authentication Flow:**

```
1. User submits username + password
2. AuthenticationProviderLDAP checks if user exists with another auth type
3. LDAP bind attempt with provided credentials
4. If successful: extract attributes (email, first name, last name)
5. Synchronize with WiseMapping database (create or update)
6. Return authentication token with WiseMapping email
```

---

### 4. Security Configuration

**File:** `wise-api/src/main/java/com/wisemapping/config/common/SecurityConfig.java`

**Changes:**

- Added conditional `ldapAuthenticationProvider()` bean (`@ConditionalOnProperty`)
- Configured `AuthenticationManager` with both providers (LDAP + DATABASE)
- Provider order: LDAP first (if enabled), DATABASE as fallback

---

### 5. JWT Authentication Controller

**File:** `wise-api/src/main/java/com/wisemapping/rest/JwtAuthController.java`

**Changes:**

- The `authenticate()` method now returns the authenticated email
- Added `InternalAuthenticationServiceException` handling for LDAP errors
- Uses the email returned by authentication (may differ from input for LDAP)

This handles the case where the user logs in with `jsmith` but their WiseMapping email is `jsmith@company.com`.

---

### 6. YAML Configuration

**File:** `wise-api/src/main/resources/application.yml`

Added LDAP configuration section:

```yaml
spring:
  ldap:
    urls: ldap://localhost:389
    base: dc=example,dc=com
    username: ""  # Manager DN for binding
    password: ""  # Manager password

app:
  ldap:
    enabled: false
    user-dn-patterns: uid={0},ou=users
    user-search-base: ou=users
    user-search-filter: (uid={0})
    email-attribute: mail
    firstname-attribute: givenName
    lastname-attribute: sn
    password-compare: false
    pooled: true
    connect-timeout: 5000
    read-timeout: 10000
```

---

## Configuration Guide for Deployment

### Step 1: Enable LDAP

In `application.yml` or via environment variables:

```yaml
app:
  ldap:
    enabled: true
```

### Step 2: Configure Server Connection

```yaml
spring:
  ldap:
    urls: ldaps://dc-01.example.com:636    # LDAPS for SSL
    base: DC=example,DC=com
```

### Step 3: Configure Service Account (Bind Account)

The service account must have read permissions on the directory.

**Find the exact account DN:**

```powershell
# PowerShell on AD server
Get-ADUser -Identity "wisemapping-bind" | Select DistinguishedName
```

**Configuration:**

```yaml
spring:
  ldap:
    username: CN=wisemapping bind,OU=Service Accounts,DC=example,DC=com
    password: ${LDAP_BIND_PASSWORD}
```

**Important:** If the password contains special characters (`!`, `$`, etc.), use single quotes when exporting:

```bash
export LDAP_BIND_PASSWORD='Complex!Password'
```

### Step 4: Configure User Search

**For Active Directory:**

```yaml
app:
  ldap:
    user-search-base: OU=Users
    user-search-filter: (sAMAccountName={0})
    user-dn-patterns: ""   # Leave empty for AD
```

**For OpenLDAP:**

```yaml
app:
  ldap:
    user-search-base: ou=people
    user-search-filter: (uid={0})
```

### Step 5: Configure Attribute Mapping

```yaml
app:
  ldap:
    email-attribute: mail           # or userPrincipalName for AD
    firstname-attribute: givenName
    lastname-attribute: sn
```

---

## Complete Configuration Examples

### Example 1: OpenLDAP with Simple Authentication

```yaml
spring:
  ldap:
    urls: ldap://ldap.example.com:389
    base: dc=example,dc=com
    username: cn=admin,dc=example,dc=com
    password: admin_password

app:
  ldap:
    enabled: true
    user-dn-patterns: uid={0},ou=people
    user-search-base: ou=people
    user-search-filter: (uid={0})
    email-attribute: mail
    firstname-attribute: givenName
    lastname-attribute: sn
```

### Example 2: Active Directory with Service Account

```yaml
spring:
  ldap:
    urls: ldaps://ad.company.com:636
    base: dc=company,dc=com
    username: CN=LDAP Service,OU=Service Accounts,DC=company,DC=com
    password: ${LDAP_BIND_PASSWORD}

app:
  ldap:
    enabled: true
    user-search-base: OU=Employees
    user-search-filter: (sAMAccountName={0})
    email-attribute: userPrincipalName
    firstname-attribute: givenName
    lastname-attribute: sn
```

### Example 3: Anonymous Bind with Email Login

```yaml
spring:
  ldap:
    urls: ldap://ldap.example.com:389
    base: dc=example,dc=com
    username: ""  # Anonymous bind
    password: ""

app:
  ldap:
    enabled: true
    user-search-base: ou=users
    user-search-filter: (mail={0})
    email-attribute: mail
    firstname-attribute: givenName
    lastname-attribute: sn
```

### Example 4: Multiple LDAP Servers (Failover)

```yaml
spring:
  ldap:
    urls: ldap://dc1.company.com:389,ldap://dc2.company.com:389
    base: dc=company,dc=com
    username: CN=wisemapping,OU=Service Accounts,DC=company,DC=com
    password: ${LDAP_BIND_PASSWORD}

app:
  ldap:
    enabled: true
    user-search-base: OU=Users
    user-search-filter: (sAMAccountName={0})
    email-attribute: userPrincipalName
    firstname-attribute: givenName
    lastname-attribute: sn
    connect-timeout: 10000  # Increased for failover scenarios
```



# Docker Deployment with LDAP Support

> For versions prior to 6.0.2. Future releases of [wisemapping/wisemapping](https://hub.docker.com/r/wisemapping/wisemapping) may include this natively.

---

## 1. Build the Image

**Using buildx:**
```bash
docker buildx build --no-cache --build-arg FRONTEND_BRANCH=main -t wisemapping:app-ldap -f distribution/app/Dockerfile .
```

**Using build:**
```bash
docker build --no-cache --build-arg FRONTEND_BRANCH=main -t wisemapping:app-ldap -f distribution/app/Dockerfile .
```

## 2. Run the Container

```bash
docker run -d --name wisemapping -p 80:80 --dns ip_dns \
  --add-host dc-01.domain.com:ip_ldap \
  -v $(pwd)/app.yml:/app/config/application.yml:ro \
  -e LDAP_BIND_PASSWORD='password' \
  wisemapping:app-ldap
```

## 3. SSL Certificate Configuration (LDAPS)

**Export the certificate:**
```bash
echo | openssl s_client -connect dc-01.domain.com:636 -showcerts 2>/dev/null | openssl x509 > example-key.crt
```

**Copy it into the container:**
```bash
docker cp example-key.crt wisemapping:/tmp/
```

**Import into Java truststore:**
```bash
docker exec wisemapping keytool -import -trustcacerts -alias example-key \
  -file /tmp/example-key.crt \
  -keystore /opt/java/openjdk/lib/security/cacerts \
  -storepass changeit -noprompt
```

## 4. Restart the Container

```bash
docker restart wisemapping
```

---

**Deployment complete.**

---

## Troubleshooting

### Error: `LDAP error code 49 - data 52e`

**Cause:** Invalid credentials (service account or user).

**Solutions:**
1. Verify that `LDAP_BIND_PASSWORD` environment variable is correctly set
2. Use the full DN for `manager-dn` (not UPN format)
3. Check that the account is not locked in AD

### Error: `LDAP error code 32 - NO_OBJECT`

**Cause:** The searched DN does not exist in the context.

**Solution:** Verify that `user-search-base` is correct and relative to `base-dn`.

### LDAP Connection Test

```bash
ldapwhoami -x -H ldaps://dc-01.example.com:636 \
  -D "CN=wisemapping bind,OU=Service,DC=example,DC=com" \
  -w "Password"
```

### Useful Logs

Enable DEBUG logs for diagnosis:

```yaml
logging:
  level:
    com.wisemapping.security: DEBUG
    org.springframework.ldap: DEBUG
    org.springframework.security.ldap: DEBUG
```

---

## System Behavior

### Automatic Account Creation

On first successful LDAP login, a WiseMapping account is automatically created with:
- Email: extracted from LDAP attribute or generated (`username@domain.com`)
- Authentication type: `LDAP`
- Account activated immediately

### Attribute Synchronization

On each login, the following attributes are updated if changed in AD:
- First name (`firstname`)
- Last name (`lastname`)

### DATABASE Fallback

If the LDAP server is unavailable, users with `AuthenticationType.DATABASE` can still log in. LDAP users will not be able to log in until the LDAP server is accessible.

### Authentication Methods Coexistence

A user can only use one authentication method. If an account exists with `AuthenticationType.DATABASE`, LDAP login will be rejected and vice versa.