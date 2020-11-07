/*
 *    Copyright [2015] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.dao;

import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.List;

public class LabelManagerImpl extends HibernateDaoSupport
        implements LabelManager {

    @Override
    public void addLabel(@NotNull final Label label) {
        saveLabel(label);
    }

    @Override
    public void saveLabel(@NotNull final Label label) {
        currentSession().save(label);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<Label> getAllLabels(@NotNull final User user) {
        var query = currentSession().createQuery("from com.wisemapping.model.Label wisemapping where creator_id=:creatorId");
        query.setParameter("creatorId", user.getId());
        return query.list();
    }

    @Nullable
    @Override
    public Label getLabelById(int id, @NotNull final User user) {
        var query = currentSession().createQuery("from com.wisemapping.model.Label wisemapping where id=:id and creator=:creator");
        query.setParameter("id", id);
        query.setParameter("creator", user);
        return getFirst(query.list());
    }

    @Nullable
    @Override
    public Label getLabelByTitle(@NotNull String title, @NotNull final User user) {
        var query = currentSession().createQuery("from com.wisemapping.model.Label wisemapping where title=:title and creator=:creator");
        query.setParameter("title", title);
        query.setParameter("creator", user);
        return getFirst(query.list());
    }

    @Override
    public void removeLabel(@NotNull Label label) {
        getHibernateTemplate().delete(label);
    }

    @Nullable private Label getFirst(List<Label> labels) {
        Label result = null;
        if (labels != null && !labels.isEmpty()) {
            result = labels.get(0);
        }
        return result;
    }

}
