package com.wisemapping.service;

import com.wisemapping.dao.LabelManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

public class LabelServiceImpl implements LabelService {

    private LabelManager labelManager;

    public void setLabelManager(LabelManager labelManager) {
        this.labelManager = labelManager;
    }

    @Override
    public void addLabel(@NotNull final Label label, @NotNull final User user) throws WiseMappingException {

        label.setCreator(user);
        labelManager.addLabel(label);
    }

}
