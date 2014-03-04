package com.wisemapping.exceptions;

import org.jetbrains.annotations.NotNull;

public class LabelCouldNotFoundException extends ClientException {

    private static final String MSG_KEY = "LABEL_CAN_NOT_BE_FOUND";

    public LabelCouldNotFoundException(@NotNull String msg)
    {
        super(msg,Severity.FATAL);
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return MSG_KEY;
    }
}
