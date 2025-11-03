package com.wisemapping.security;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom OAuth2 Authorization Request Resolver for ChatGPT integration.
 * 
 * Detects when ChatGPT parameters are provided and stores them in the
 * OAuth2AuthorizationRequest's additionalParameters. Spring Security
 * automatically persists this to session.
 */
public class ChatGptOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    
    private static final Logger logger = LogManager.getLogger();
    private final OAuth2AuthorizationRequestResolver defaultResolver;
    
    public ChatGptOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, 
            "/oauth2/authorization"
        );
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, 
            HttpServletRequest request) {
        
        if (authorizationRequest == null) {
            return null;
        }
        
        // Check if ChatGPT parameters are provided
        String chatgptParams = request.getParameter("chatgpt_params");
        
        if (chatgptParams != null && !chatgptParams.isEmpty()) {
            logger.info("=== ChatGPT OAuth Authorization Request ===");
            logger.info("ChatGPT params detected, length: {}", chatgptParams.length());
            logger.debug("ChatGPT params: {}", chatgptParams);
            
            // BETTER APPROACH: Encode ChatGPT params INTO Spring's state parameter
            // Format: CHATGPT:<chatgpt-params>:<spring-original-state>
            // This way we don't rely on session persistence!
            String originalState = authorizationRequest.getState();
            String enhancedState = "CHATGPT:" + chatgptParams + ":" + originalState;
            
            authorizationRequest = OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(enhancedState)  // Replace state with our enhanced version
                .build();
            
            logger.info("✓ Encoded ChatGPT params into state parameter (no session needed!)");
            logger.info("✓ Enhanced state length: {}", enhancedState.length());
            logger.debug("✓ Enhanced state format: CHATGPT:<params>:<original-state>");
        }
        
        return authorizationRequest;
    }
}

