/*
 *    Copyright [2011] [wisemapping]
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

import com.wisemapping.exceptions.ClientException;
import com.wisemapping.exceptions.Severity;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestErrors;
import com.wisemapping.security.Utils;
import com.wisemapping.service.RegistrationException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

public class BaseController {

    final private Logger logger = Logger.getLogger(BaseController.class);

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
        logger.error(ex.getMessage(), ex);
        return new RestErrors(ex.getMessage(), Severity.WARNING);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleValidationErrors(@NotNull ValidationException ex) {
        return new RestErrors(ex.getErrors(), messageSource);
    }

    @ExceptionHandler(JsonHttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleJSONErrors(@NotNull JsonHttpMessageNotReadableException ex) {
        return new RestErrors("Communication error", Severity.SEVERE);
    }

    @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleSecurityErrors(@NotNull UndeclaredThrowableException ex) {
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrors handleServerErrors(@NotNull Exception ex, @NotNull HttpServletRequest request) {
        final User user = Utils.getUser(false);
        notificationService.reportJavaException(ex, user, request);
        logger.error(ex);

        return new RestErrors(ex.getMessage(), Severity.SEVERE);
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleRegistrationErrors(@NotNull RegistrationException exception) {
        return new RestErrors(exception, messageSource);
    }
}
