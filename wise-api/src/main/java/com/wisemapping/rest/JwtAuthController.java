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

package com.wisemapping.rest;

import com.wisemapping.exceptions.UserCouldNotBeAuthException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestJwtUser;
import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restful")
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private MetricsService metricsService;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<String> createAuthenticationToken(@RequestBody RestJwtUser user,
            @NotNull HttpServletResponse response) throws WiseMappingException {
        // Is a valid user ?
        authenticate(user.getEmail(), user.getPassword());
        final String result = jwtTokenUtil.doLogin(response, user.getEmail());

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<Void> logout(@NotNull HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(JwtTokenUtil.BEARER_TOKEN_PREFIX)) {
            String token = authHeader.substring(JwtTokenUtil.BEARER_TOKEN_PREFIX.length());
            String email = jwtTokenUtil.extractFromJwtToken(token);
            
            if (email != null) {
                try {
                    Account user = userService.getUserBy(email);
                    if (user != null) {
                        metricsService.trackUserLogout(user, "manual");
                    }
                } catch (Exception e) {
                    // Log error but don't fail logout
                }
            }
        }
        
        return ResponseEntity.ok().build();
    }

    private void authenticate(@NotNull String username, @NotNull String password) throws WiseMappingException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException | BadCredentialsException e) {
            throw new UserCouldNotBeAuthException("Authentication failed:" + e.getLocalizedMessage(), e);
        }
    }
}