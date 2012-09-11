package com.wisemapping.rest;

class JsonHttpMessageNotReadableException extends org.springframework.http.converter.HttpMessageNotReadableException {

    public JsonHttpMessageNotReadableException(String msg, Exception cause) {
        super(msg, cause);
    }
}
