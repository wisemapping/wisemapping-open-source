package com.wisemapping.dao;

import com.wisemapping.model.LabelMindmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LabelMindmapManager {

    @Nullable
    LabelMindmap getLabelMindmap(final int labelId, final int mindmapId);

    void removeLabelMindmap(@NotNull LabelMindmap labelMindmap);

}
