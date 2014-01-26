package com.wisemapping.dao;

import com.wisemapping.model.Label;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class LabelManagerImpl extends HibernateDaoSupport
        implements LabelManager {

    @Override
    public void addLabel(@NotNull final Label label) {
        saveLabel(label);
    }

    @Override
    public void saveLabel(@NotNull final Label label) {
        getSession().save(label);
    }
}
