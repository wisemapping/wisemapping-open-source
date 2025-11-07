package com.wisemapping.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.UUID;

/**
 * Custom OAuth2 Authorization Request Resolver for ChatGPT integration.
 * 
 * Detects when ChatGPT parameters are provided and stores them in the HTTP session
 * with a short reference ID in the state parameter. This prevents the state parameter
 * from exceeding Google OAuth's size limit (~2KB).
 */
public class ChatGptOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    
    private static final Logger logger = LogManager.getLogger();
    private static final String CHATGPT_PARAMS_SESSION_PREFIX = "CHATGPT_PARAMS_";
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
            logger.debug("ChatGPT params detected in OAuth request");
            
            // Generate a short reference ID for the session
            String referenceId = UUID.randomUUID().toString();
            
            // Store ChatGPT params in session with the reference ID
            HttpSession session = request.getSession(true);
            String sessionKey = CHATGPT_PARAMS_SESSION_PREFIX + referenceId;
            session.setAttribute(sessionKey, chatgptParams);
            logger.debug("Stored ChatGPT params in session with key: {}", sessionKey);
            
            // Use a short state format: CHATGPT:<reference-id>:<spring-original-state>
            String originalState = authorizationRequest.getState();
            String enhancedState = "CHATGPT:" + referenceId + ":" + originalState;
            
            authorizationRequest = OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(enhancedState)
                .build();
            
            logger.info("ChatGPT OAuth request - stored params in session, state size: {} chars", enhancedState.length());
        }
        
        return authorizationRequest;
    }
}

