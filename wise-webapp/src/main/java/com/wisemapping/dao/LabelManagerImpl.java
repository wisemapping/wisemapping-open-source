package com.wisemapping.dao;

import com.wisemapping.model.Label;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class LabelManagerImpl extends HibernateDaoSupport
        implements LabelManager {

    @Override
    public void addLabel(Label label) {
        saveLabel(label);
    }

    @Override
    public void saveLabel(@NotNull Label label) {
        getSession().save(label);
    }
}
