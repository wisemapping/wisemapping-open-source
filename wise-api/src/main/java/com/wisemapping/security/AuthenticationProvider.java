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


import com.wisemapping.exceptions.AccountDisabledException;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;


public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    private UserDetailsService userDetailsService;
    private PasswordEncoder encoder;

    @Override()
    public Authentication authenticate(@NotNull final Authentication auth) throws AuthenticationException {

        // All your user authentication needs
        final String email = auth.getName();

        final UserDetails userDetails = getUserDetailsService().loadUserByUsername(email);
        final Account user = userDetails.getUser();
        final String credentials = (String) auth.getCredentials();

        if (user == null || credentials == null || !encoder.matches(user.getPassword(), credentials)) {
            throw new BadCredentialsException("Username/Password does not match for " + auth.getPrincipal());
        }

        // User has been disabled ...
        if (!user.isActive()) {
            throw new BadCredentialsException("User has been disabled for login " + auth.getPrincipal());
        }

        // Check if account is suspended
        if (user.isSuspended()) {
            throw new AccountDisabledException("ACCOUNT_SUSPENDED");
        }

        userDetailsService.getUserService().auditLogin(user);
        return new UsernamePasswordAuthenticationToken(userDetails, credentials, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setEncoder(@NotNull PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
