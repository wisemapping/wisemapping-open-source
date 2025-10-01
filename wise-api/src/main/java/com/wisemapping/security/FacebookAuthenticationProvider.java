package com.wisemapping.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.wisemapping.exceptions.AccountDisabledException;
import com.wisemapping.model.Account;
import com.wisemapping.service.MetricsService;

public class FacebookAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private MetricsService metricsService;

    public FacebookAuthenticationProvider(@NotNull UserDetailsService userDetailsService, MetricsService metricsService) {
        this.userDetailsService = userDetailsService;
        this.metricsService = metricsService;
    }

    @Override
    public Authentication authenticate(Authentication inputToken) throws AuthenticationException {
        if (!supports(inputToken.getClass())) {
            return null;
        }
        if (inputToken.getPrincipal() == null) {
            throw new BadCredentialsException("No pre-authenticated principal found in request.");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(inputToken.getName());
        final Account user = userDetails.getUser();

        if (!user.isActive()) {
            throw new BadCredentialsException("User has been disabled for login " + inputToken.getName());
        }

        if (user.isSuspended()) {
            throw new AccountDisabledException("ACCOUNT_SUSPENDED");
        }

        PreAuthenticatedAuthenticationToken resultToken = new PreAuthenticatedAuthenticationToken(userDetails,
                inputToken.getCredentials(), userDetails.getAuthorities());
        resultToken.setDetails(userDetails);

        userDetailsService.getUserService().auditLogin(user);

        metricsService.trackUserLogin(user, "facebook_oauth");

        return resultToken;
    }

    @Override
    public final boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

}