/*
 *    Copyright [2022] [wisemapping]
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

import com.wisemapping.model.AccessAuditory;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;
import com.wisemapping.security.DefaultPasswordEncoderFactories;
import com.wisemapping.security.LegacyPasswordEncoder;
import org.hibernate.ObjectNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserManagerImpl
        extends HibernateDaoSupport
        implements UserManager {

    private PasswordEncoder passwordEncoder;

    public void setEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return currentSession().createQuery("from com.wisemapping.model.User user").list();
    }


    @Override
    @Nullable
    public User getUserBy(@NotNull final String email) {
        User user = null;

        var query = currentSession().createQuery("from com.wisemapping.model.User colaborator where email=:email");
        query.setParameter("email", email);

        final List<User> users = query.list();
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            user = users.get(0);
        }
        return user;

    }

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        final Collaborator cola;
        var query = currentSession().createQuery("from com.wisemapping.model.Collaborator colaborator where " +
                "email=:email");
        query.setParameter("email", email);

        final List<User> cols = query.list();
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            cola = cols.get(0);
        } else {
            cola = null;
        }
        return cola;
    }

    @Nullable
    @Override
    public User getUserBy(int id) {
        User user = null;
        try {
            user = getHibernateTemplate().get(User.class, id);
        } catch (ObjectNotFoundException e) {
            // Ignore ...
        }
        return user;
    }

    @Override
    public void createUser(User user) {
        assert user != null : "Trying to store a null user";
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        getHibernateTemplate().saveOrUpdate(user);
    }

    @Override
    public User createUser(@NotNull User user, @NotNull Collaborator collaborator) {
        assert user != null : "Trying to store a null user";

        // Migrate from previous temporal collab to new user ...
        List<Collaboration> newCollabs = new ArrayList<>();
        final Set<Collaboration> collaborations = collaborator.getCollaborations();
        for (Collaboration oldCollab : collaborations) {
            Collaboration newCollab = new Collaboration();
            newCollab.setRoleId(oldCollab.getRole().ordinal());
            newCollab.setMindMap(oldCollab.getMindMap());
            newCollab.setCollaborator(user);
            user.addCollaboration(newCollab);
            newCollabs.add(newCollab);
        }

        // Delete old collaboration
        final HibernateTemplate template = getHibernateTemplate();
        collaborations.forEach(c -> template.delete(c));
        template.delete(collaborator);
        template.flush();

        // Save all new...
        this.createUser(user);
        newCollabs.forEach(c -> template.saveOrUpdate(c));

        return user;
    }

    @Override
    public void removeUser(@NotNull final User user) {
        getHibernateTemplate().delete(user);
    }

    public void auditLogin(@NotNull AccessAuditory accessAuditory) {
        assert accessAuditory != null : "accessAuditory is null";
        getHibernateTemplate().save(accessAuditory);
    }

    public void updateUser(@NotNull User user) {
        assert user != null : "user is null";

        // Does the password need to be encrypted ?
        final String password = user.getPassword();
        if (password != null && (!password.startsWith(LegacyPasswordEncoder.ENC_PREFIX) && !password.startsWith("{" + DefaultPasswordEncoderFactories.ENCODING_ID))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        getHibernateTemplate().update(user);
    }

    public User getUserByActivationCode(long code) {
        final User user;

        var query = currentSession().createQuery("from com.wisemapping.model.User user where " +
                "activationCode=:activationCode");
        query.setParameter("activationCode", code);
        final List users = query.list();

        if (users != null && !users.isEmpty()) {

            assert users.size() == 1 : "More than one user with the same username!";
            user = (User) users.get(0);
        } else {
            user = null;
        }
        return user;
    }
}
