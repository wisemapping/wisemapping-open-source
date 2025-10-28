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
import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.security.UserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LogManager.getLogger();
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Value("${app.site.ui-base-url:}")
    private String uiBaseUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) throws IOException {
        
        logger.debug("OAuth2 authentication successful for user: {}", authentication.getName());
        
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Account account = customOAuth2User.getAccount();
        
        // Generate JWT token using UserDetails
        String jwt = jwtTokenUtil.generateJwtToken(userDetailsService.loadUserByUsername(account.getEmail()));
        
        // Audit the login
        userDetailsService.getUserService().auditLogin(account);
        
        try {
            // Check if there's a redirect state parameter from the OAuth flow
            String state = request.getParameter("state");
            
            // Build redirect URL to frontend
            String redirectPath = "/c/oauth-callback";
            
            // If state contains a redirect path, use it
            if (state != null && !state.isEmpty() && !state.equals("wisemapping")) {
                // State might be a full URL or just a path
                if (state.startsWith("http")) {
                    redirectPath = state;
                } else if (state.startsWith("/")) {
                    redirectPath = state;
                } else {
                    redirectPath = "/" + state;
                }
            }
            
            // Build query parameters
            String queryParams = "?jwtToken=" + URLEncoder.encode(jwt, "UTF-8") +
                                "&email=" + URLEncoder.encode(account.getEmail(), "UTF-8") +
                                "&oauthSync=" + account.getOauthSync();
            
            if (account.getSyncCode() != null) {
                queryParams += "&syncCode=" + URLEncoder.encode(account.getSyncCode(), "UTF-8");
            }
            
            // Build full redirect URL
            String redirectUrl;
            if (redirectPath.startsWith("http")) {
                redirectUrl = redirectPath + queryParams;
            } else {
                // Prepend UI base URL if it's set
                String baseUrl = (uiBaseUrl != null && !uiBaseUrl.isEmpty()) ? uiBaseUrl : "";
                redirectUrl = baseUrl + redirectPath + queryParams;
            }
            
            response.sendRedirect(redirectUrl);
            logger.debug("OAuth2 redirecting to: {}", redirectUrl);
            
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding OAuth callback URL", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth callback error");
        }
        
        logger.debug("OAuth2 authentication completed successfully for user: {}", account.getEmail());
    }
}
