package com.wisemapping.dao;

import com.wisemapping.model.MindmapLabel;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LabelManager {

    void addLabel(@NotNull final MindmapLabel label);

    void saveLabel(@NotNull final MindmapLabel label);

    @NotNull
    List<MindmapLabel> getAllLabels(@NotNull final Account user);

    @Nullable
    MindmapLabel getLabelById(int id, @NotNull final Account user);

    @Nullable
    MindmapLabel getLabelByTitle(@NotNull final String title, @NotNull final Account user);

    void removeLabel(@NotNull final MindmapLabel label);
}
