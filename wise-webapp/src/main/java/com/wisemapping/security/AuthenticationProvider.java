package com.wisemapping.security;


import com.wisemapping.dao.UserManager;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;


public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    private UserManager userManager;

    private PasswordEncoder encoder;

    @Override()
    public Authentication authenticate(@NotNull final Authentication auth) throws AuthenticationException {

        // All your user authentication needs
        final String email = auth.getName();

        final User user = userManager.getUserBy(email);
        final String credentials = (String) auth.getCredentials();
        if (user == null || credentials == null || !encoder.isPasswordValid(user.getPassword(), credentials, null)) {
            throw new BadCredentialsException("Username/Password does not match for " + auth.getPrincipal());
        }

        final UserDetails userDetails = new UserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, credentials, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(final Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setEncoder(@NotNull PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

}
