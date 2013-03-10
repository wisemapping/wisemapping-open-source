package com.wisemapping.model;

public enum  AuthenticationSchema
{
    DATABASE(0),
    LDAP(1),
    OPENID(2);
    private final int schemaCode;

    AuthenticationSchema(int schemaCode) {
        this.schemaCode = schemaCode;
    }

    public int getSchemaCode() {
        return schemaCode;
    }
}
