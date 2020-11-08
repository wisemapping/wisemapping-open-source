package com.wisemapping.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private RequestCache cache;

    public AuthenticationSuccessHandler() {
        cache = new HttpSessionRequestCache();
        this.setRequestCache(cache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest != null && savedRequest.getRedirectUrl().contains("c/restful")) {
            cache.removeRequest(request, response);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String url = super.determineTargetUrl(request, response);
        // Prevent redirecting to rest services on login ...
        if (url.contains("c/restful")) {
            url = this.getDefaultTargetUrl();
        }
        return url;
    }

}