package com.wisemapping.service;

import org.springframework.validation.BindException;

public class RegistrationException extends BindException {
    public RegistrationException(Object target, String objectName) {
        super(target, objectName);
    }
}
