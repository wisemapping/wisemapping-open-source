package com.wisemapping.security;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class CSFRRequestMatcher implements RequestMatcher {

    private String prefix;
    static String[] supportedMethods = {"POST", "PUT", "GET", "DELETE", "PATCH"};

    @Override
    public boolean matches(HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        return Arrays.stream(supportedMethods).anyMatch(p -> request.getMethod().toUpperCase().equals(p))
                && requestURI.startsWith(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
