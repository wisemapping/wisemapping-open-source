package com.wisemapping.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;

import java.util.Locale;

abstract public class ClientException extends WiseMappingException {
    public ClientException(@NotNull String message) {
        super(message);
    }

    protected abstract
    @NotNull
    String getMsgBundleKey();

    public String getMessage(@NotNull final MessageSource messageSource, final @NotNull Locale locale) {
        return messageSource.getMessage(this.getMsgBundleKey(), null, locale);
    }
}
