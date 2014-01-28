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
    @Nullable
    @Override
    public Label getLabelByTitle(@NotNull String title, @NotNull final User user) {
        Label result = null;
        final List<Label> labels = getHibernateTemplate().find("from com.wisemapping.model.Label wisemapping where title=? and creator=?", new Object[]{title, user});
        if (labels != null && !labels.isEmpty()) {
            result = labels.get(0);
        }
        return result;
    }
}
