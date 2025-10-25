package com.wisemapping.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.wisemapping.exceptions.AccountDisabledException;
import com.wisemapping.exceptions.AccountSuspendedException;
import com.wisemapping.model.Account;
import com.wisemapping.service.MetricsService;

public class GoogleAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private MetricsService metricsService;

    public GoogleAuthenticationProvider(@NotNull UserDetailsService userDetailsService, MetricsService metricsService) {
        this.userDetailsService = userDetailsService;
        this.metricsService = metricsService;
    }

    /**
     * Authenticate the given PreAuthenticatedAuthenticationToken.
     * <p>
     * If the principal contained in the authentication object is null, the request will
     * be ignored to allow other providers to authenticate it.
     */
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

        // OAuth users should always be active (they don't need email confirmation)
        if (!user.isActive()) {
            throw new AccountDisabledException("Google OAuth account not active for " + inputToken.getName());
        }

        // Check if account is suspended - do NOT auto-unsuspend
        if (user.isSuspended()) {
            throw new AccountSuspendedException("Account suspended for " + inputToken.getName());
        }

        PreAuthenticatedAuthenticationToken resultToken = new PreAuthenticatedAuthenticationToken(userDetails,
                inputToken.getCredentials(), userDetails.getAuthorities());
        resultToken.setDetails(userDetails);

        userDetailsService.getUserService().auditLogin(user);

        // Track Google OAuth login (optional)
        metricsService.trackUserLogin(user, "google_oauth");

        return resultToken;
    }

    /**
     * Indicate that this provider only supports PreAuthenticatedAuthenticationToken
     * (sub)classes.
     */
    @Override
    public final boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }


}
