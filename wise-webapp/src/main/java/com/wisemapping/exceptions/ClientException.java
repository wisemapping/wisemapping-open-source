package com.wisemapping.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;

import java.util.Locale;

abstract public class ClientException extends WiseMappingException {
    private final Severity severity;

    public ClientException(@NotNull String message, @NotNull Severity severity) {
        super(message);
        this.severity = severity;
    }

    protected abstract
    @NotNull
    String getMsgBundleKey();

    public String getMessage(@NotNull final MessageSource messageSource, final @NotNull Locale locale) {
        String message = messageSource.getMessage(this.getMsgBundleKey(), this.getMsgBundleArgs(), locale);
        if(message==null){
            message = this.getMessage();
        }
        return message;
    }

    protected  Object[] getMsgBundleArgs(){
         return null;
    }

    public Severity getSeverity() {
        return this.severity;
    }

    public String getTechInfo() {
        return getMessage();
    }
}
