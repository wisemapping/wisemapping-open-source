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

import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LogManager.getLogger();
    
    @Autowired
    private UserService userService;
    
    @org.springframework.beans.factory.annotation.Value("${app.admin.user:}")
    private String adminUser;
    
    @Override
    public OAuth2User loadUser(@NotNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Extract user information based on provider
        String email = extractEmail(attributes, provider);
        String firstName = extractFirstName(attributes, provider);
        String lastName = extractLastName(attributes, provider);
        String accessToken = userRequest.getAccessToken().getTokenValue();
        
        logger.debug("Processing OAuth2 user from {}: {}", provider, email);
        
        try {
            // Use existing user service logic to handle registration/linking
            Account account = findOrCreateOAuthUser(email, firstName, lastName, provider, accessToken);
            
            // Return custom OAuth2User that includes our Account with admin check
            return new CustomOAuth2User(oauth2User, account, adminUser != null ? adminUser : "");
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user from {}: {}", provider, email, e);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user: " + e.getMessage());
        }
    }
    
    private Account findOrCreateOAuthUser(String email, String firstName, String lastName, 
                                        String provider, String accessToken) throws Exception {
        
        // Convert provider string to AuthenticationType
        AuthenticationType authType = mapProviderToAuthType(provider);
        
        // Check if user exists
        Account existingUser = userService.getUserBy(email);
        
        if (existingUser == null) {
            // Create new OAuth user using existing logic
            return createNewOAuthUser(email, firstName, lastName, authType, accessToken);
        } else {
            // Handle existing user - use existing business logic
            return handleExistingUser(existingUser, authType, accessToken);
        }
    }
    
    private Account createNewOAuthUser(String email, String firstName, String lastName, 
                                     AuthenticationType authType, String accessToken) throws Exception {
        
        // Create new account with OAuth data
        Account newUser = new Account();
        newUser.setEmail(email);
        newUser.setFirstname(firstName);
        newUser.setLastname(lastName);
        newUser.setAuthenticationType(authType);
        newUser.setOauthToken(accessToken);
        newUser.setPassword(""); // OAuth users don't need passwords
        newUser.setOauthSync(true); // New OAuth users are already synced
        
        // Use existing user service to create user
        return userService.createUser(newUser, false, true);
    }
    
    private Account handleExistingUser(Account existingUser, AuthenticationType authType, String accessToken) throws Exception {
        
        AuthenticationType existingAuthType = existingUser.getAuthenticationType();
        
        if (existingAuthType != AuthenticationType.DATABASE && existingAuthType != authType) {
            // User is trying to login with different OAuth provider than registered
            logger.warn("User {} attempted to login with {} but account uses {}", 
                       existingUser.getEmail(), authType, existingAuthType);
            throw new Exception("Account is registered with a different authentication provider");
        }
        
        // For existing OAuth users with same auth type, just update the token
        if (existingAuthType == authType) {
            existingUser.setOauthToken(accessToken);
            // Ensure oauthSync is true for OAuth users (fix for legacy accounts)
            if (existingUser.getOauthSync() == null || !existingUser.getOauthSync()) {
                existingUser.setOauthSync(true);
                existingUser.setSyncCode(null);
            }
            userService.updateUser(existingUser);
            logger.debug("Updated OAuth token for existing {} user: {}", authType, existingUser.getEmail());
            return existingUser;
        }
        
        // Handle sync for DATABASE accounts only
        if (existingUser.getAuthenticationType() == AuthenticationType.DATABASE) {
            // Existing DATABASE account trying to link with OAuth - ask for confirmation
            if (existingUser.getOauthSync() == null || !existingUser.getOauthSync()) {
                existingUser.setOauthSync(false);
                existingUser.setSyncCode(accessToken); // Use access token as sync code
                existingUser.setOauthToken(accessToken);
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
    
    private String extractEmail(Map<String, Object> attributes, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> (String) attributes.get("email");
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }
    
    private String extractFirstName(Map<String, Object> attributes, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("given_name");
            case "facebook" -> (String) attributes.get("first_name");
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }
    
    private String extractLastName(Map<String, Object> attributes, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("family_name");
            case "facebook" -> (String) attributes.get("last_name");
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }
}
