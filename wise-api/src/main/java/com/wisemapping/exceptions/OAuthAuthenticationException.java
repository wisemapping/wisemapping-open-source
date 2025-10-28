package com.wisemapping.exceptions;

import jakarta.validation.constraints.NotNull;

public class OAuthAuthenticationException extends WiseMappingException {

    public OAuthAuthenticationException(@NotNull String message) {
        super(message);
    }

    public OAuthAuthenticationException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}