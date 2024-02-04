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
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestOath2CallbackResponse;
import com.wisemapping.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@RestController
@CrossOrigin
public class OAuth2Controller extends BaseController {
	@Qualifier("userService")
	@Autowired
	private UserService userService;

	@Qualifier("authenticationManager")
	@Autowired
	private AuthenticationManager authManager;

	@Value("${google.recaptcha2.enabled}")
	private Boolean recatchaEnabled;

	@Value("${accounts.exclusion.domain:''}")
	private String domainBanExclusion;

	private void doLogin(HttpServletRequest request, String email) {
		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(email,null);
		Authentication auth = authManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(auth);
		// update spring mvc session
		HttpSession session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/oauth2/googlecallback", produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	public RestOath2CallbackResponse processGoogleCallback(@NotNull @RequestParam String code, @NotNull HttpServletRequest request) throws WiseMappingException {
		User user = userService.createUserFromGoogle(code);
		if (user.getGoogleSync() != null && user.getGoogleSync()) {
			doLogin(request, user.getEmail());
		}
		RestOath2CallbackResponse response = new RestOath2CallbackResponse();
		response.setEmail(user.getEmail());
		response.setGoogleSync(user.getGoogleSync());
		response.setSyncCode(user.getSyncCode());
		return response;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/oauth2/confirmaccountsync", produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	public void confirmAccountSync(@NotNull @RequestParam String email, @NotNull @RequestParam String code, @NotNull HttpServletRequest request) throws WiseMappingException {
		userService.confirmAccountSync(email, code);
		doLogin(request, email);
	}

}
