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
package com.wisemapping.config;

import com.wisemapping.exceptions.*;
import com.wisemapping.rest.JsonHttpMessageNotReadableException;
import com.wisemapping.rest.model.RestErrors;
import com.wisemapping.service.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger();

    @Qualifier("messageSource")
    @Autowired(required = false)
    private ResourceBundleMessageSource messageSource;

    @Value("${app.site.ui-base-url:}")
    private String uiBaseUrl;

    private boolean isApiRequest(@NotNull HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }

    private void redirectToUi(HttpServletResponse response, String pathWithParams) {
        try {
            if (uiBaseUrl != null && !uiBaseUrl.isEmpty()) {
                response.sendRedirect(uiBaseUrl + pathWithParams);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception ignored) {
            // If redirect fails, fall back to default error handling
        }
    }

    @ExceptionHandler(AccessDeniedSecurityException.class)
    @ResponseBody
    public ResponseEntity<RestErrors> handleAccessDeniedSecurityException(AccessDeniedSecurityException ex) {
        // Log at DEBUG level to avoid ERROR logs
        logger.debug("Access denied for map access: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? ex.getMessage(messageSource, locale) : ex.getMessage();
        RestErrors error = new RestErrors(message, ex.getSeverity(), ex.getTechInfo());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidEmailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleInvalidEmailException(InvalidEmailException ex) {
        // Log at DEBUG level to avoid ERROR logs
        logger.debug("Invalid email exception: {}", ex.getMessage());
        return new RestErrors(ex.getMessage(), ex.getSeverity(), ex.getTechInfo());
    }

    @ExceptionHandler(SpamContentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public RestErrors handleSpamContentException(SpamContentException ex) {
        // Log at INFO level for spam detection tracking
        logger.info("Spam content detected and blocked: {}", ex.getMessage());
        return new RestErrors(ex.getMessage(), ex.getSeverity(), ex.getTechInfo());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleClientErrors(@NotNull IllegalArgumentException ex) {
        return new RestErrors(ex.getMessage(), Severity.WARNING);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public RestErrors handleAuthException(@NotNull final AuthenticationCredentialsNotFoundException ex) {
        logger.debug(ex.getMessage(), ex);
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("AUTHENTICATION_SESSION_EXPIRED", null, "Authentication exception. Session must be expired. Try logging again.", locale) :
            "Authentication exception. Session must be expired. Try logging again.";
        return new RestErrors(message, Severity.INFO);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public RestErrors handleAuthorizationDeniedException(@NotNull final AuthorizationDeniedException ex) {
        logger.debug("Authorization denied: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("AUTHORIZATION_ACCESS_DENIED", null, "Access denied. The map may have been deleted or you don't have permission to access it.", locale) :
            "Access denied. The map may have been deleted or you don't have permission to access it.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResponseEntity<RestErrors> handleValidationErrors(@NotNull final ValidationException ex) {
        logger.debug(ex.getMessage(), ex);
        RestErrors error = new RestErrors(ex.getErrors(), messageSource);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(JsonHttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleJSONErrors(@NotNull JsonHttpMessageNotReadableException ex) {
        logger.error(ex.getMessage(), ex);
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("COMMUNICATION_ERROR", null, "Communication error", locale) :
            "Communication error";
        return new RestErrors(message, Severity.SEVERE);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleHttpMessageNotReadableException(@NotNull org.springframework.http.converter.HttpMessageNotReadableException ex) {
        // Log at DEBUG level since this is usually a client error (missing request body)
        logger.debug("HTTP message not readable: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("INVALID_REQUEST", null, "Invalid request. Required request body is missing or malformed.", locale) :
            "Invalid request. Required request body is missing or malformed.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
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

    @ExceptionHandler(UserCouldNotBeAuthException.class)
    public ResponseEntity<RestErrors> handleUserCouldNotBeAuthException(@NotNull UserCouldNotBeAuthException ex) {
        logger.debug("Authentication failed: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        RestErrors error = new RestErrors(ex.getMessage(messageSource, locale), ex.getSeverity(), ex.getTechInfo());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @ExceptionHandler(AccountDisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public RestErrors handleAccountDisabledException(@NotNull AccountDisabledException ex) {
        logger.debug("Account disabled/not activated: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("ACCOUNT_DISABLED", null, "Your account has not been activated yet. Please check your email for activation instructions.", locale) :
            "Your account has not been activated yet. Please check your email for activation instructions.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public RestErrors handleAccountSuspendedException(@NotNull AccountSuspendedException ex) {
        logger.debug("Account suspended: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("ACCOUNT_SUSPENDED", null, "Your account has been suspended. Please contact support for assistance.", locale) :
            "Your account has been suspended. Please contact support for assistance.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public RestErrors handleLockedException(@NotNull LockedException ex) {
        logger.debug("Account locked/not activated: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("ACCOUNT_NOT_ACTIVATED_YET", null, "Your account has not been activated yet. Please check your email for the activation link.", locale) :
            "Your account has not been activated yet. Please check your email for the activation link.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public RestErrors handleBadCredentialsException(@NotNull BadCredentialsException ex) {
        logger.debug("Bad credentials: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource != null ? 
            messageSource.getMessage("INVALID_CREDENTIALS", null, "Invalid email or password. Please try again.", locale) :
            "Invalid email or password. Please try again.";
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(WrongAuthenticationTypeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public RestErrors handleWrongAuthenticationTypeException(@NotNull WrongAuthenticationTypeException ex) {
        logger.debug("Wrong authentication type: {}", ex.getMessage());
        final Locale locale = LocaleContextHolder.getLocale();
        
        // Provide specific message based on the authentication type
        String messageKey;
        String defaultMessage;
        switch (ex.getAuthenticationType()) {
            case GOOGLE_OAUTH2:
                messageKey = "ACCOUNT_USES_GOOGLE_OAUTH";
                defaultMessage = "This account is registered with Google. Please use the 'Sign in with Google' button to login.";
                break;
            case FACEBOOK_OAUTH2:
                messageKey = "ACCOUNT_USES_FACEBOOK_OAUTH";
                defaultMessage = "This account is registered with Facebook. Please use the 'Sign in with Facebook' button to login.";
                break;
            case LDAP:
                messageKey = "ACCOUNT_USES_EXTERNAL_AUTH";
                defaultMessage = "This account uses external authentication. Please use the appropriate sign-in method.";
                break;
            default:
                messageKey = "ACCOUNT_USES_EXTERNAL_AUTH";
                defaultMessage = "This account uses external authentication. Please use the appropriate sign-in method.";
                break;
        }
        
        String message = messageSource != null ? 
            messageSource.getMessage(messageKey, null, defaultMessage, locale) :
            defaultMessage;
        return new RestErrors(message, Severity.WARNING);
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleClientErrors(@NotNull ClientException ex) {
        final Locale locale = LocaleContextHolder.getLocale();
        return new RestErrors(ex.getMessage(messageSource, locale), ex.getSeverity(), ex.getTechInfo());
    }

    @ExceptionHandler(UserRegistrationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrors handleUserRegistrationException(@NotNull UserRegistrationException ex) {
        logger.error("User registration failed: {}", ex.getMessage(), ex);
        final Locale locale = LocaleContextHolder.getLocale();
        
        // Use a more specific error message if available, otherwise use generic one
        String message = ex.getMessage();
        if (message == null || message.isEmpty() || message.equals("User registration failed")) {
            message = messageSource != null ? 
                messageSource.getMessage("USER_REGISTRATION_ERROR", null, "An unexpected error occurred during registration. Please try again. If the problem persists, contact support.", locale) :
                "An unexpected error occurred during registration. Please try again. If the problem persists, contact support.";
        }
        return new RestErrors(message, Severity.SEVERE, ex.getCause() != null ? ex.getCause().getMessage() : null);
    }

    @ExceptionHandler(WiseMappingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleWiseMappingException(@NotNull WiseMappingException ex) {
        logger.debug(ex.getMessage(), ex);
        return new RestErrors(ex.getMessage(), Severity.WARNING);
    }

    @ExceptionHandler(OAuthAuthenticationException.class)
    public Object handleOAuthErrors(@NotNull OAuthAuthenticationException ex,
                                    @NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response) {
        logger.error("OAuth authentication error: {}", ex.getMessage(), ex);
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ex;
        }
        redirectToUi(response, "/c/login?error=oauth_failed");
        return null;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleServerErrors(@NotNull Exception ex,
                                     @NotNull HttpServletRequest request,
                                     @NotNull HttpServletResponse response) {
        // Log at DEBUG level for expected exceptions to avoid ERROR logs
        if (ex instanceof AccessDeniedSecurityException || ex instanceof InvalidEmailException || 
            ex instanceof ValidationException || ex instanceof ClientException) {
            logger.debug("Expected exception handled: {}", ex.getMessage());
        } else {
            logger.error(ex.getMessage(), ex);
        }
        if (isApiRequest(request)) {
            return new RestErrors(ex.getMessage(), Severity.SEVERE);
        }
        redirectToUi(response, "/c/login?error=server_error");
        return null;
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleRegistrationErrors(@NotNull RegistrationException ex) {
        return new RestErrors(ex, messageSource);
    }
}
