/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.rest;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestOath2CallbackResponse;
import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/oauth2/")
@CrossOrigin
public class OAuth2Controller extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("authenticationManager")
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @RequestMapping(method = RequestMethod.POST, value = "googlecallback", produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.OK)
    public RestOath2CallbackResponse processGoogleCallback(@NotNull @RequestParam String code, @NotNull HttpServletResponse response, @NotNull HttpServletRequest request) throws WiseMappingException {
        Account user = userService.createAndAuthUserFromGoogle(code);
        if (user.getGoogleSync()) {
            jwtTokenUtil.doLogin(response, user.getEmail());
        }

        // Response ...
        final RestOath2CallbackResponse result = new RestOath2CallbackResponse();
        result.setEmail(user.getEmail());
        result.setGoogleSync(user.getGoogleSync());
        result.setSyncCode(user.getSyncCode());
        return result;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "confirmaccountsync", produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void confirmAccountSync(@NotNull @RequestParam String email, @NotNull @RequestParam String code, @NotNull HttpServletResponse response) throws WiseMappingException {
        // Authenticate ...
        userService.createAndAuthUserFromGoogle(code);

        // Update login
        userService.confirmAccountSync(email, code);

        // Add header ...
        jwtTokenUtil.doLogin(response, email);
    }
}
