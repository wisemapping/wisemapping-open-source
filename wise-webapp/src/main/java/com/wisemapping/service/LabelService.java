package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LabelService {

    void addLabel(@NotNull final Label label, @NotNull final User user) throws WiseMappingException;

    @NotNull List<Label> getAll(@NotNull final User user);
}
