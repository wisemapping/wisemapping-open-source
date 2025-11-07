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
package com.wisemapping.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LogManager.getLogger();
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MetricsService metricsService;
    
    @Value("${app.site.ui-base-url:}")
    private String uiBaseUrl;
    
    @Value("${app.admin.user:}")
    private String adminUser;
    
    @Value("${app.chatgpt.ai-base-url:https://ai.wisemapping.com}")
    private String chatgptAiBaseUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) throws IOException {
        
        logger.debug("OAuth2 authentication successful for user: {}", authentication.getName());
        
        // Get the OAuth2/OIDC user from Spring Security
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract user info from OAuth2 attributes
        String email = extractEmail(oauth2User);
        String firstName = extractFirstName(oauth2User);
        String lastName = extractLastName(oauth2User);
        String provider = extractProvider(request);
        
        try {
            // Find or create WiseMapping account
            Account account = findOrCreateOAuthUser(email, firstName, lastName, provider);
            
            // Generate JWT token using UserDetails
            String jwt = jwtTokenUtil.generateJwtToken(userDetailsService.loadUserByUsername(account.getEmail()));
            
            // Audit the login
            userService.auditLogin(account);
            
            // Track OAuth login telemetry
            String authType = mapProviderToAuthTypeString(provider);
            metricsService.trackUserLogin(account, authType);
            
            // Determine redirect URL based on OAuth flow type
            String state = request.getParameter("state");
            String redirectUrl;
            
            // Extract ChatGPT parameters from session using reference ID in state
            // Format: CHATGPT:<reference-id>:<spring-original-state>
            String chatgptParams = null;
            if (state != null && state.startsWith("CHATGPT:")) {
                logger.debug("Detected ChatGPT OAuth flow from state parameter");
                try {
                    String withoutPrefix = state.substring("CHATGPT:".length());
                    int lastColon = withoutPrefix.lastIndexOf(':');
                    
                    if (lastColon > 0) {
                        String referenceId = withoutPrefix.substring(0, lastColon);
                        logger.debug("Extracting ChatGPT params from session using reference ID: {}", referenceId);
                        
                        // Retrieve from session
                        String sessionKey = "CHATGPT_PARAMS_" + referenceId;
                        chatgptParams = (String) request.getSession().getAttribute(sessionKey);
                        
                        if (chatgptParams != null) {
                            logger.debug("Successfully retrieved ChatGPT params from session");
                            // Clean up session
                            request.getSession().removeAttribute(sessionKey);
                        } else {
                            logger.warn("ChatGPT params not found in session for reference ID: {}", referenceId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error extracting ChatGPT params from session: {}", e.getMessage());
                }
            }
            
            boolean isChatGptFlow = (chatgptParams != null && !chatgptParams.isEmpty() && isChatGptOAuthFlow(chatgptParams));
            
            if (isChatGptFlow) {
                // Track ChatGPT OAuth flow usage
                metricsService.trackUserLogin(account, "chatgpt_oauth_" + provider.toLowerCase());
                
                // ChatGPT OAuth flow - redirect to ai.wisemapping.com for conversion
                redirectUrl = chatgptAiBaseUrl + "/oauth/social-callback" +
                    "?jwtToken=" + URLEncoder.encode(jwt, "UTF-8") +
                    "&state=" + URLEncoder.encode(chatgptParams, "UTF-8");
                logger.info("OAuth2 ChatGPT flow detected for user: {}, provider: {}", email, provider);
                logger.debug("Redirecting to AI proxy: {}", redirectUrl);
            } else {
                // Normal OAuth flow - redirect to frontend oauth-callback page
                String baseUrl = (uiBaseUrl != null && !uiBaseUrl.isEmpty()) ? uiBaseUrl : "";
                String queryParams = "?jwtToken=" + URLEncoder.encode(jwt, "UTF-8") +
                                    "&email=" + URLEncoder.encode(account.getEmail(), "UTF-8") +
                                    "&oauthSync=" + account.getOauthSync();
                
                if (account.getSyncCode() != null) {
                    queryParams += "&syncCode=" + URLEncoder.encode(account.getSyncCode(), "UTF-8");
                }
                
                redirectUrl = baseUrl + "/c/oauth-callback" + queryParams;
                logger.debug("OAuth2 normal flow, redirecting to frontend: {}", redirectUrl);
            }
            
            // Redirect to determined URL
            response.sendRedirect(redirectUrl);
            
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding OAuth callback URL", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth callback error");
        } catch (Exception e) {
            logger.error("Error processing OAuth user: {}", email, e);
            response.sendRedirect(uiBaseUrl + "/c/login?error=oauth_failed");
        }
        
        logger.debug("OAuth2 authentication completed successfully for user: {}", email);
    }
    
    private String extractEmail(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser) {
            return ((OidcUser) oauth2User).getEmail();
        }
        return (String) oauth2User.getAttributes().get("email");
    }
    
    private String extractFirstName(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        // Try OIDC claim first
        String firstName = (String) attributes.get("given_name");
        if (firstName == null) {
            // Fall back to Facebook format
            firstName = (String) attributes.get("first_name");
        }
        return firstName;
    }
    
    private String extractLastName(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        // Try OIDC claim first
        String lastName = (String) attributes.get("family_name");
        if (lastName == null) {
            // Fall back to Facebook format
            lastName = (String) attributes.get("last_name");
        }
        return lastName;
    }
    
    private String extractProvider(HttpServletRequest request) {
        // Extract provider from the callback URL path
        String uri = request.getRequestURI();
        if (uri.contains("/oauth2/code/")) {
            String[] parts = uri.split("/");
            return parts[parts.length - 1];
        }
        return "unknown";
    }
    
    private Account findOrCreateOAuthUser(String email, String firstName, String lastName, String provider) throws Exception {
        AuthenticationType authType = mapProviderToAuthType(provider);
        
        // Check if user exists
        Account existingUser = userService.getUserBy(email);
        
        if (existingUser == null) {
            // Create new OAuth user
            return createNewOAuthUser(email, firstName, lastName, authType);
        } else {
            // Handle existing user
            return handleExistingUser(existingUser, authType);
        }
    }
    
    private Account createNewOAuthUser(String email, String firstName, String lastName, AuthenticationType authType) throws Exception {
        Account newUser = new Account();
        newUser.setEmail(email);
        newUser.setFirstname(firstName);
        newUser.setLastname(lastName);
        newUser.setAuthenticationType(authType);
        newUser.setOauthToken("");
        newUser.setPassword("");
        newUser.setOauthSync(true);
        
        return userService.createUser(newUser, false, true);
    }
    
    private Account handleExistingUser(Account existingUser, AuthenticationType authType) throws Exception {
        AuthenticationType existingAuthType = existingUser.getAuthenticationType();
        
        if (existingAuthType != AuthenticationType.DATABASE && existingAuthType != authType) {
            logger.warn("User {} attempted to login with {} but account uses {}", 
                       existingUser.getEmail(), authType, existingAuthType);
            throw new Exception("Account is registered with a different authentication provider");
        }
        
        // For existing OAuth users with same auth type, update if needed
        if (existingAuthType == authType) {
            if (existingUser.getOauthSync() == null || !existingUser.getOauthSync()) {
                existingUser.setOauthSync(true);
                existingUser.setSyncCode(null);
                userService.updateUser(existingUser);
            }
            return existingUser;
        }
        
        // Handle linking for DATABASE accounts
        if (existingUser.getAuthenticationType() == AuthenticationType.DATABASE) {
            if (existingUser.getOauthSync() == null || !existingUser.getOauthSync()) {
                existingUser.setOauthSync(false);
                existingUser.setSyncCode("oauth_pending");
                userService.updateUser(existingUser);
            }
        }
        
        return existingUser;
    }
    
    private AuthenticationType mapProviderToAuthType(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> AuthenticationType.GOOGLE_OAUTH2;
            case "facebook" -> AuthenticationType.FACEBOOK_OAUTH2;
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }
    
    private String mapProviderToAuthTypeString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> "google_oauth";
            case "facebook" -> "facebook_oauth";
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }
    
    /**
     * Check if the state parameter indicates this is a ChatGPT OAuth flow.
     * ChatGPT OAuth state is base64-encoded JSON containing "chatgptRedirectUri".
     */
    private boolean isChatGptOAuthFlow(String state) {
        if (state == null || state.isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode as base64 and parse as JSON
            String decoded = new String(Base64.getDecoder().decode(state));
            JsonNode stateData = objectMapper.readTree(decoded);
            
            // Check if it has chatgptRedirectUri field
            boolean hasChatGptField = stateData.has("chatgptRedirectUri");
            
            if (hasChatGptField) {
                logger.debug("ChatGPT OAuth flow detected");
            }
            
            return hasChatGptField;
            
        } catch (Exception e) {
            logger.debug("State is not ChatGPT OAuth format: {}", e.getMessage());
            return false;
        }
    }
}
