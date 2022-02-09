package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LabelService {

    void addLabel(@NotNull final Label label, @NotNull final User user) throws WiseMappingException;

    @NotNull List<Label> getAll(@NotNull final User user);

    @Nullable
    Label findLabelById(int id, @NotNull final User user);

    Label getLabelByTitle(@NotNull String title, @NotNull final User user);

    void removeLabel(@NotNull final Label label, @NotNull final User user) throws WiseMappingException;
}
