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

import com.wisemapping.exceptions.*;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestErrors;
import com.wisemapping.security.Utils;
import com.wisemapping.service.NotificationService;
import com.wisemapping.service.RegistrationException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

public class BaseController {

    final private Logger logger = LogManager.getLogger();

    @Qualifier("messageSource")
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    ServletContext context;

    @Autowired
    private NotificationService notificationService;

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleClientErrors(@NotNull IllegalArgumentException ex) {
        return new RestErrors(ex.getMessage(), Severity.WARNING);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleValidationErrors(@NotNull ValidationException ex) {
        logger.debug(ex.getMessage(), ex);
        return new RestErrors(ex.getErrors(), messageSource);
    }

    @ExceptionHandler(JsonHttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleJSONErrors(@NotNull JsonHttpMessageNotReadableException ex) {
        logger.error(ex.getMessage(), ex);
        return new RestErrors("Communication error", Severity.SEVERE);
    }

    @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleSecurityErrors(@NotNull UndeclaredThrowableException ex) {
        logger.error(ex.getMessage(), ex);
        final Throwable cause = ex.getCause();
        RestErrors result;
        if (cause instanceof ClientException) {
            result = handleClientErrors((ClientException) cause);
        } else {
            result = new RestErrors(ex.getMessage(), Severity.INFO);
        }
        return result;
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleClientErrors(@NotNull ClientException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        return new RestErrors(ex.getMessage(messageSource, locale), ex.getSeverity(), ex.getTechInfo());
    }


    @ExceptionHandler(AccessDeniedSecurityException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RestErrors handleAccessDeniedSecurityException(@NotNull AccessDeniedSecurityException ex) {
        return new RestErrors(ex.getMessage(), ex.getSeverity(), ex.getTechInfo());
    }

    @ExceptionHandler(OAuthAuthenticationException.class)
    @ResponseBody
    public OAuthAuthenticationException handleOAuthErrors(@NotNull OAuthAuthenticationException ex, HttpServletResponse response) {
        // @todo: Further research needed for this error. No clear why this happens.
        // Caused by: com.wisemapping.service.http.HttpInvokerException: error invoking https://oauth2.googleapis.com/token, response: {
        //  "error": "invalid_grant",
        //  "error_description": "Bad Request"
        //}, status: 400
        //
        logger.error(ex.getMessage(), ex);
        response.setStatus(response.getStatus());
        return ex;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrors handleServerErrors(@NotNull Exception ex, @NotNull HttpServletRequest request) {
        logger.error(ex.getMessage(), ex);
        final Account user = Utils.getUser(false);
        notificationService.reportJavaException(ex, user, request);
        return new RestErrors(ex.getMessage(), Severity.SEVERE);
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleRegistrationErrors(@NotNull RegistrationException ex) {
        return new RestErrors(ex, messageSource);
    }
}
