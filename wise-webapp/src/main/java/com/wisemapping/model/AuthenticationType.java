package com.wisemapping.model;


import org.jetbrains.annotations.NotNull;

public enum AuthenticationType {
    DATABASE('D'),
    LDAP('L'),
    OPENID('O');
    private final char schemaCode;

    AuthenticationType(char schemaCode) {
        this.schemaCode = schemaCode;
    }

    public char getCode() {
        return schemaCode;
    }

    @NotNull
    public static AuthenticationType valueOf(char code) {
        AuthenticationType result = null;
        AuthenticationType[] values = AuthenticationType.values();
        for (AuthenticationType value : values) {
            if (value.getCode() == code) {
                result = value;
                break;
            }
        }

        if (result == null) {
            throw new IllegalStateException("Could not find auth with code:" + code);
        }

        return result;
    }
}
