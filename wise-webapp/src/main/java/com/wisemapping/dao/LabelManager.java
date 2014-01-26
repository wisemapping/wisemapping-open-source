package com.wisemapping.dao;

import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LabelManager {

    void addLabel(@NotNull final Label label);

    void saveLabel(@NotNull final Label label);

    @NotNull
    List<Label> getAllLabels(@NotNull final User user);
}
