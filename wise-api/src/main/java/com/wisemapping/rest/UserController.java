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

import com.wisemapping.exceptions.EmailNotExistsException;
import com.wisemapping.exceptions.PasswordTooLongException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestResetPasswordResponse;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.*;
import com.wisemapping.validator.Messages;
import com.wisemapping.validator.UserValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/restful/users")
public class UserController {

    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Autowired
    private RecaptchaService captchaService;

    @Autowired
    private DisposableEmailService disposableEmailService;

    @Autowired
    private MetricsService metricsService;

    @Qualifier("authenticationManager")
    @Autowired
    private AuthenticationManager authManager;

    @Value("${app.registration.enabled:true}")
    private Boolean registrationEnabled;

    @Value("${app.registration.email-confirmation-enabled:true}")
    private Boolean emailConfirmationEnabled;

    @Value("${app.registration.captcha.enabled:true}")
    private Boolean registrationCaptchaEnabled;

    @Value("${app.accounts.exclusion.domain:''}")
    private String domainBanExclusion;

    private static final Logger logger = LogManager.getLogger();
    private static final String REAL_IP_ADDRESS_HEADER = "X-Real-IP";

    @RequestMapping(method = RequestMethod.POST, value = "/", produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void registerUser(@RequestBody RestUserRegistration registration, @NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response) throws WiseMappingException, BindException {
        logger.debug("Register new user:" + registration.getEmail());
        if (!registrationEnabled) {
            throw new WiseMappingException("Registration is disabled. You can enable it using app.registration.enabled");
        }

        if (registration.getPassword().length() > Account.MAX_PASSWORD_LENGTH_SIZE) {
            throw new PasswordTooLongException();
        }

        // If tomcat is behind a reverse proxy, ip needs to be found in other header.
        String remoteIp = request.getHeader(REAL_IP_ADDRESS_HEADER);
        if (remoteIp == null || remoteIp.isEmpty()) {
            remoteIp = request.getRemoteAddr();
        }
        logger.debug("Remote address" + remoteIp);
        verify(registration, remoteIp);

        final Account user = new Account();
        user.setEmail(registration.getEmail().trim());
        user.setFirstname(registration.getFirstname());
        user.setLastname(registration.getLastname());
        user.setPassword(registration.getPassword());

        user.setAuthenticationType(AuthenticationType.DATABASE);
        // For DATABASE users, email confirmation can be configured via app.registration.email-confirmation-enabled
        // For OAuth users, this is handled separately and they don't need email confirmation
        userService.createUser(user, emailConfirmationEnabled, !emailConfirmationEnabled);
        
        // Track user registration
        String emailProvider = metricsService.extractEmailProvider(user.getEmail());
        metricsService.trackUserRegistration(user, emailProvider);
        
        response.setHeader("Location", "/api/restful/users/" + user.getId());
        response.setHeader("ResourceId", Integer.toString(user.getId()));

    }

    @RequestMapping(method = RequestMethod.PUT, value = "/resetPassword", produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.OK)
    public RestResetPasswordResponse resetPassword(@RequestParam String email) throws InvalidAuthSchemaException, EmailNotExistsException {
        try {
            return userService.resetPassword(email);
        } catch (InvalidUserEmailException e) {
            throw new EmailNotExistsException(e);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/activation", produces = {"application/json"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void activateAccount(@RequestParam long code) throws WiseMappingException {
        logger.debug("Activating account with code: " + code);
        // Both exceptions extend ClientException, which will be automatically handled
        // by GlobalExceptionHandler with proper i18n message lookup
        userService.activateAccount(code);
    }

    private void verify(@NotNull final RestUserRegistration registration, @NotNull String remoteAddress)
            throws BindException {

        final BindException errors = new RegistrationException(registration, "registration");
        final UserValidator validator = new UserValidator();
        validator.setUserService(userService);
        validator.setDisposableEmailService(disposableEmailService);
        validator.validate(registration, errors);

        // If captcha is enabled, generate it ...
        if (registrationCaptchaEnabled) {
            final String recaptcha = registration.getRecaptcha();
            if (recaptcha != null) {
                final String reCaptchaResponse = captchaService.verifyRecaptcha(remoteAddress, recaptcha);
                if (reCaptchaResponse != null && !reCaptchaResponse.isEmpty()) {
                    errors.rejectValue("recaptcha", reCaptchaResponse);
                }
            } else {
                errors.rejectValue("recaptcha", Messages.CAPTCHA_LOADING_ERROR);
            }
        } else {
            logger.warn("captchaEnabled is enabled.Recommend to enable it for production environments.");
        }

        if (errors.hasErrors()) {
            throw errors;
        }

        // Is excluded ?.
        final List<String> excludedDomains = Arrays.asList(domainBanExclusion.split(","));
        final String emailDomain = registration.getEmail().split("@")[1];
        if (excludedDomains.contains(emailDomain)) {
            throw new IllegalArgumentException(
                    "Email is part of ban exclusion list due to abuse. Please, contact site admin if you think this is an error."
                            + emailDomain);
        }
    }
}
