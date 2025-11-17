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

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestOath2CallbackResponse;
import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restful/oauth2")
@CrossOrigin
public class OAuth2Controller {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PutMapping(value = "/confirmaccountsync", produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    public RestOath2CallbackResponse confirmAccountSync(@NotNull @RequestParam String email,
                                                        @NotNull @RequestParam String code,
                                                        @Nullable @RequestParam(required = false) String provider,
                                                        @NotNull HttpServletResponse response) throws WiseMappingException {
        logger.info("Confirming OAuth account sync. Email: {}, Provider: {}", email, provider);

        final Account user = userService.confirmAccountSync(email, code, provider);
        final String jwtToken = jwtTokenUtil.doLogin(response, email);

        return new RestOath2CallbackResponse(user, jwtToken);
    }
}

