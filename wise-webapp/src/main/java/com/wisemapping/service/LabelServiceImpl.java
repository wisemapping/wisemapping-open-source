package com.wisemapping.service;

import com.wisemapping.dao.LabelManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @NotNull
    @Override
    public List<Label> getAll(@NotNull final User user) {
        return labelManager.getAllLabels(user);
    }

    @Override @Nullable
    public Label findLabelById(int id, @NotNull final User user) {
        return labelManager.getLabelById(id, user);
    }

    @Nullable
    @Override
    public Label getLabelByTitle(@NotNull String title, @NotNull final User user) {
        return labelManager.getLabelByTitle(title, user);
    }

    @Override
    public void removeLabel(@NotNull Label label, @NotNull User user) throws WiseMappingException {
        if (label.getCreator().equals(user)) {
            labelManager.removeLabel(label);
        } else {
            throw new WiseMappingException("User: "+ user.getFullName()  + "has no ownership on label " + label.getTitle());

        }
    }
}
