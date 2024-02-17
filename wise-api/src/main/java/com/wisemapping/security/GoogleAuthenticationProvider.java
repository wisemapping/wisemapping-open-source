package com.wisemapping.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.wisemapping.model.Account;

public class GoogleAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private UserDetailsService userDetailsService;

    public GoogleAuthenticationProvider(@NotNull UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
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

        if (!user.isActive()) {
            throw new BadCredentialsException("User has been disabled for login " + inputToken.getName());
        }

        PreAuthenticatedAuthenticationToken resultToken = new PreAuthenticatedAuthenticationToken(userDetails,
                inputToken.getCredentials(), userDetails.getAuthorities());
        resultToken.setDetails(userDetails);

        userDetailsService.getUserService().auditLogin(user);

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
