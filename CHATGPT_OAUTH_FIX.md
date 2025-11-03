# ChatGPT OAuth Integration - Fix Summary

## Problem

When users clicked "Continue with Google/Facebook" on the ChatGPT OAuth login page (`ai.wisemapping.com`), they were redirected to the WiseMapping list page instead of back to ChatGPT.

### Root Cause

Spring Security OAuth2 was generating its own random state parameter (for CSRF protection) and ignoring our base64-encoded ChatGPT OAuth parameters. The error in logs:

```
>>> ✗ State is not ChatGPT OAuth format (decoding failed): Illegal base64 character 2d
```

The character `2d` (hyphen `-`) indicated Spring's UUID-style state, not our base64-encoded JSON.

## Solution

Implemented a **completely stateless** solution by encoding ChatGPT parameters directly into Spring's state parameter:

### 1. Custom Authorization Request Resolver

**File**: `wise-api/src/main/java/com/wisemapping/security/ChatGptOAuth2AuthorizationRequestResolver.java`

- Extends Spring's `DefaultOAuth2AuthorizationRequestResolver`
- Detects `chatgpt_params` query parameter
- **Encodes ChatGPT params INTO Spring's state parameter**
- Format: `CHATGPT:<chatgpt-params>:<spring-original-state>`
- **No session storage needed!** Completely stateless!
- Works in load-balanced, serverless, and containerized environments

### 2. Updated Success Handler

**File**: `wise-api/src/main/java/com/wisemapping/security/OAuth2AuthenticationSuccessHandler.java`

- **Extracts ChatGPT params from the enhanced state parameter** (no session needed!)
- Parses format: `CHATGPT:<params>:<original-state>`
- Detects ChatGPT flow by checking if state starts with `CHATGPT:`
- Redirects to `ai.wisemapping.com/oauth/social-callback` with JWT and ChatGPT params
- Preserves original Spring state for CSRF validation
- Added comprehensive debug logging

### 3. Updated Security Configuration

**File**: `wise-api/src/main/java/com/wisemapping/config/AppConfig.java`

- Configured OAuth2 login to use custom `ChatGptOAuth2AuthorizationRequestResolver`
- Uses Spring's built-in `authorizationEndpoint()` configuration

### 4. Updated Frontend

**File**: `wise.ai/src/main/resources/templates/oauth/login.html`

- Changed Google OAuth URL to include `chatgpt_params` query parameter:
  ```javascript
  https://api.wisemapping.com/oauth2/authorization/google?chatgpt_params=[base64-json]
  ```
- Same change for Facebook OAuth

## Flow Diagram

### Before (Broken):
```
1. User clicks "Continue with Google" on ai.wisemapping.com
2. Frontend redirects to: api.wisemapping.com/oauth2/authorization/google?state=[chatgpt-json]
3. Spring ignores our state, generates UUID state
4. Google OAuth callback with Spring's state
5. Success handler can't find ChatGPT params ❌
6. Redirects to app.wisemapping.com (normal flow)
```

### After (Fixed - Stateless):
```
1. User clicks "Continue with Google" on ai.wisemapping.com
2. Frontend redirects to: api.wisemapping.com/oauth2/authorization/google?chatgpt_params=[chatgpt-json]
3. Custom resolver detects chatgpt_params query parameter ✓
4. Resolver ENCODES params INTO state: CHATGPT:[params]:[spring-state] ✓
5. Spring redirects to Google with enhanced state (NO SESSION STORAGE!)
6. Google authenticates user
7. Google redirects back with state parameter
8. Success handler receives enhanced state: CHATGPT:[params]:[spring-state] ✓
9. Success handler EXTRACTS chatgpt_params from state ✓
10. Success handler validates original Spring state (CSRF protection preserved) ✓
11. Redirects to: ai.wisemapping.com/oauth/social-callback?jwtToken=...&state=[chatgpt-json] ✓
12. ChatGPT receives authorization code ✓
```

## Deployment

1. **Rebuild WiseMapping API**:
```bash
cd /Users/pveiga/repos/wiseapp/wisemapping-open-source/wise-api
mvn clean package -DskipTests
```

2. **Deploy to production** (`api.wisemapping.com`)

3. **Rebuild AI Service** (optional - frontend changes already deployed):
```bash
cd /Users/pveiga/repos/wise.ai
mvn clean package -DskipTests
```

4. **Restart services**

## Testing

1. Navigate to: `https://ai.wisemapping.com/oauth/authorize?response_type=code&client_id=openai-chats-client&redirect_uri=https://chat.openai.com/...&state=...&scope=mindmap:read+mindmap:list+mindmap:create+mindmap:update+mindmap:delete`

2. Click "Continue with Google"

3. Check logs on `api.wisemapping.com`:
```
=== ChatGPT OAuth Authorization Request ===
ChatGPT params detected, length: 152
✓ Encoded ChatGPT params into state parameter (no session needed!)
✓ Enhanced state format: CHATGPT:<params>:<original-state>

=== OAuth2 Callback Debug Info ===
✓ Detected enhanced state with ChatGPT params
✓ Extracted ChatGPT params from state (length: 152)
✓ Original Spring state preserved: [uuid]
>>> ✓ CONFIRMED: This is a ChatGPT OAuth flow!
✓ Redirecting to AI proxy: https://ai.wisemapping.com/oauth/social-callback?...
```

4. Should be redirected back to ChatGPT, not to WiseMapping list page

## Debug Logging

Extensive debug logging added to trace the flow:

### Initialization:
- ChatGPT params stored in session
- Provider being used

### Success Handler:
- State parameter from Spring
- ChatGPT params from session
- Flow detection result
- Base64 decoding details
- JSON parsing results
- Final redirect URL

## Files Changed

### WiseMapping API (`wisemapping-open-source`):
1. `wise-api/src/main/java/com/wisemapping/security/ChatGptOAuth2AuthorizationRequestResolver.java` (NEW)
2. `wise-api/src/main/java/com/wisemapping/security/OAuth2AuthenticationSuccessHandler.java` (MODIFIED)
3. `wise-api/src/main/java/com/wisemapping/config/AppConfig.java` (MODIFIED)
4. `wise-api/src/main/resources/application.yml` (MODIFIED - ChatGPT config)

### AI Service (`wise.ai`):
1. `src/main/resources/templates/oauth/login.html` (MODIFIED - frontend URLs)

## Configuration

The following configuration is already in `application.yml`:

```yaml
app:
  chatgpt:
    ai-base-url: https://ai.wisemapping.com
```

Default value in code:
```java
@Value("${app.chatgpt.ai-base-url:https://ai.wisemapping.com}")
private String chatgptAiBaseUrl;
```

## Benefits

1. ✅ **Completely stateless** - No session storage required!
2. ✅ **Works in ANY environment** - Load-balanced, serverless, containerized
3. ✅ **No sticky sessions needed** - Each request is independent
4. ✅ **No persistence layer** - No Redis, database, or session store required
5. ✅ **Simple and elegant** - ChatGPT params encoded in state parameter
6. ✅ **Secure** - State still provides CSRF protection (original state preserved)
7. ✅ **Scalable** - No shared state between servers
8. ✅ **Clean code** - No manual session manipulation
9. ✅ **Standard Spring Security pattern** - Uses OAuth2AuthorizationRequestResolver
10. ✅ **Comprehensive logging** - Easy to debug issues

## Why This Approach is Better

**Session-based approaches:**
- ❌ Requires session persistence (Redis/database/sticky sessions)
- ❌ Doesn't work in stateless environments
- ❌ Complex clustering configuration
- ❌ Memory/storage overhead
- ❌ Session expiration issues

**Current approach (State parameter encoding):**
- ✅ **100% stateless** - No server-side storage
- ✅ **Works everywhere** - Stateless containers, serverless, Kubernetes
- ✅ **No configuration needed** - No Redis, no sticky sessions
- ✅ **Self-contained** - All data travels with the request
- ✅ **Simple** - Just encode/decode state parameter
- ✅ **Scales infinitely** - No shared state between servers
- ✅ **CSRF protection preserved** - Original Spring state still validated

