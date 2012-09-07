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

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.ClientException;
import com.wisemapping.filter.UserAgent;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestErrors;
import com.wisemapping.security.Utils;
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
        ex.printStackTrace();
        return new RestErrors(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleServerErrors(@NotNull Exception ex, @NotNull HttpServletRequest request) {
        final User user = Utils.getUser();
        notificationService.reportJavaException(ex, user, request);
        return ex.getMessage();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleValidationErrors(@NotNull ValidationException ex) {
        return new RestErrors(ex.getErrors(), messageSource);
    }

    @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleSecurityErrors(@NotNull UndeclaredThrowableException ex) {
        final Throwable cause = ex.getCause();
        RestErrors result;
        if (cause instanceof ClientException) {
            result = handleClientErrors((ClientException) cause);
        } else {
            result = new RestErrors(ex.getMessage());
        }
        return result;
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleClientErrors(@NotNull ClientException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        return new RestErrors(ex.getMessage(messageSource, locale));
    }
}
