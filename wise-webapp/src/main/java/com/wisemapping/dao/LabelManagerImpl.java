package com.wisemapping.dao;

import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

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

    @NotNull
    @Override
    public List<Label> getAllLabels(@NotNull final User user) {
        return getHibernateTemplate().find("from com.wisemapping.model.Label wisemapping where creator_id=?", user.getId());
    }
}
