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