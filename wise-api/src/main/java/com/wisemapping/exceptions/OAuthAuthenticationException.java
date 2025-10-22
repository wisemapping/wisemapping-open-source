package com.wisemapping.exceptions;


import com.wisemapping.service.google.http.HttpInvokerException;

import jakarta.validation.constraints.NotNull;

public class OAuthAuthenticationException extends WiseMappingException {

    public OAuthAuthenticationException(@NotNull HttpInvokerException exception) {
        super(exception.getMessage());
    }

    public OAuthAuthenticationException(@NotNull String message) {
        super(message);
    }
}