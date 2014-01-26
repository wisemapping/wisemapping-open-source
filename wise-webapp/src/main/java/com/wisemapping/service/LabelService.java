package com.wisemapping.service;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

public interface LabelService {

    void addLabel(@NotNull final Label label, @NotNull final User user) throws WiseMappingException;

}
