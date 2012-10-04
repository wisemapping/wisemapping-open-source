package com.wisemapping.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;

import java.util.Locale;

abstract public class ClientException extends WiseMappingException {
    private Severity severity;

    public ClientException(@NotNull String message, @NotNull Severity severity) {
        super(message);
        this.severity = severity;
    }

    protected abstract
    @NotNull
    String getMsgBundleKey();

    public String getMessage(@NotNull final MessageSource messageSource, final @NotNull Locale locale) {
        return messageSource.getMessage(this.getMsgBundleKey(), null, locale);
    }

    public Severity getSeverity() {
        return this.severity;
    }
}
