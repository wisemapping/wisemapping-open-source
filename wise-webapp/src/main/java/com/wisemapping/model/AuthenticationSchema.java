package com.wisemapping.model;

public enum AuthenticationSchema {
    DATABASE('D'),
    LDAP('L'),
    OPENID('O');
    private final char schemaCode;

    AuthenticationSchema(char schemaCode) {
        this.schemaCode = schemaCode;
    }

    public char getCode() {
        return schemaCode;
    }

    public static AuthenticationSchema valueOf(char code) {
        AuthenticationSchema result = null;
        AuthenticationSchema[] values = AuthenticationSchema.values();
        for (AuthenticationSchema value : values) {
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
