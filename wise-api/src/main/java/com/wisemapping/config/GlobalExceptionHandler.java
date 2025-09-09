package com.wisemapping.config;

import com.wisemapping.exceptions.*;
import com.wisemapping.model.Account;
import com.wisemapping.rest.JsonHttpMessageNotReadableException;
import com.wisemapping.rest.model.RestErrors;
import com.wisemapping.security.Utils;
import com.wisemapping.service.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @Qualifier("messageSource")
    @Autowired(required = false)
    private ResourceBundleMessageSource messageSource;

    @ExceptionHandler(AccessDeniedSecurityException.class)
    @ResponseBody
    public ResponseEntity<RestErrors> handleAccessDeniedSecurityException(AccessDeniedSecurityException ex) {
        // Log at DEBUG level to avoid ERROR logs
        logger.debug("Access denied for map access: {}", ex.getMessage());
        RestErrors error = new RestErrors(ex.getMessage(), ex.getSeverity(), ex.getTechInfo());
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

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleClientErrors(@NotNull IllegalArgumentException ex) {
        return new RestErrors(ex.getMessage(), Severity.WARNING);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RestErrors handleAuthException(@NotNull final AuthenticationCredentialsNotFoundException ex) {
        logger.debug(ex.getMessage(), ex);
        return new RestErrors("Authentication exception. Session must be expired. Try logging again.", Severity.INFO);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestErrors handleValidationErrors(@NotNull final ValidationException ex) {
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

    @ExceptionHandler(WiseMappingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleWiseMappingException(@NotNull WiseMappingException ex) {
        logger.debug(ex.getMessage(), ex);
        return new RestErrors(ex.getMessage(), Severity.WARNING);
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
        // Log at DEBUG level for expected exceptions to avoid ERROR logs
        if (ex instanceof AccessDeniedSecurityException || ex instanceof InvalidEmailException) {
            logger.debug("Expected exception handled: {}", ex.getMessage());
        } else {
            logger.error(ex.getMessage(), ex);
        }
        final Account user = Utils.getUser(false);
        return new RestErrors(ex.getMessage(), Severity.SEVERE);
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrors handleRegistrationErrors(@NotNull RegistrationException ex) {
        return new RestErrors(ex, messageSource);
    }
}
