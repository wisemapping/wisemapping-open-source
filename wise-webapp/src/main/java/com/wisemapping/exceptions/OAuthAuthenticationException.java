package com.wisemapping.exceptions;


import com.wisemapping.service.google.http.HttpInvokerException;

import javax.validation.constraints.NotNull;

public class OAuthAuthenticationException extends WiseMappingException {

    public OAuthAuthenticationException(@NotNull HttpInvokerException exception) {
        super(exception.getMessage());
    }
}