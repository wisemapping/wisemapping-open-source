package com.wisemapping.exceptions;

import org.jetbrains.annotations.NotNull;

public class LabelMindmapRelationshipNotFoundException extends ClientException {

    private static final String MSG_KEY = "LABEL_MINDMAP_RELATION_NOT_BE_FOUND";

    public LabelMindmapRelationshipNotFoundException(@NotNull String msg)
    {
        super(msg,Severity.WARNING);
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return MSG_KEY;
    }

}
