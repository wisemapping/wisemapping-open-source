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

import com.wisemapping.model.*;
import com.wisemapping.security.DefaultPasswordEncoderFactories;
import com.wisemapping.security.LegacyPasswordEncoder;
import jakarta.annotation.Resource;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.SelectionQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Repository
public class UserManagerImpl
        implements UserManager {
//    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void setEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return getSession().createSelectionQuery("from com.wisemapping.model.User user", User.class).getResultList();
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }


    @Override
    @Nullable
    public User getUserBy(@NotNull final String email) {
        User user = null;

        SelectionQuery<User> query = getSession().createSelectionQuery("from com.wisemapping.model.User colaborator where email=:email",User.class);
        query.setParameter("email", email);

        final List<User> users = query.getResultList();
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            user = users.get(0);
        }
        return user;

    }

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        final Collaborator result;
        Session session = getSession();
        final SelectionQuery<Collaborator> query = session.createSelectionQuery("from com.wisemapping.model.Collaborator colaborator where " +
                "email=:email", Collaborator.class);
        query.setParameter("email", email);

        final List<Collaborator> cols = query.getResultList();
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            result = cols.get(0);
        } else {
            result = null;
        }
        return result;
    }

    @Nullable
    @Override
    public User getUserBy(int id) {
        User user = null;
        try {
            user = getSession().get(User.class, id);
        } catch (ObjectNotFoundException e) {
            // Ignore ...
        }
        return user;
    }

    @Override
    public void createUser(User user) {
        assert user != null : "Trying to store a null user";
        if (!AuthenticationType.GOOGLE_OAUTH2.equals(user.getAuthenticationType())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword("");
        }
        getSession().persist(user);
    }

    @Override
    public User createUser(@NotNull User user, @NotNull Collaborator collaborator) {
        assert user != null : "Trying to store a null user";

        // Migrate from previous temporal collab to new user ...
        final Session session = getSession();
        collaborator.setEmail(collaborator.getEmail() + "_toRemove");
        session.merge(collaborator);
        session.flush();

        // Save all new...
        this.createUser(user);

        // Update mindmap ...
        final Set<Collaboration> collaborations = new CopyOnWriteArraySet<>(collaborator.getCollaborations());
        for (Collaboration collabs : collaborations) {
            collabs.setCollaborator(user);
        }

        // Delete old user ...
        session.remove(collaborator);
        return user;
    }

    @Override
    public void removeUser(@NotNull final User user) {
        getSession().remove(user);
    }

    public void auditLogin(@NotNull AccessAuditory accessAuditory) {
        assert accessAuditory != null : "accessAuditory is null";
        getSession().persist(accessAuditory);
    }

    public void updateUser(@NotNull User user) {
        assert user != null : "user is null";

        // Does the password need to be encrypted ?
        final String password = user.getPassword();
        if (password != null && (!password.startsWith(LegacyPasswordEncoder.ENC_PREFIX) && !password.startsWith("{" + DefaultPasswordEncoderFactories.ENCODING_ID))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        getSession().merge(user);
    }

    public User getUserByActivationCode(long code) {
        final User user;

        final SelectionQuery<User> query = getSession().createSelectionQuery("from com.wisemapping.model.User user where " +
                "activationCode=:activationCode", User.class);
        query.setParameter("activationCode", code);

        final List<User> users = query.getResultList();
        if (users != null && !users.isEmpty()) {

            assert users.size() == 1 : "More than one user with the same username!";
            user = users.get(0);
        } else {
            user = null;
        }
        return user;
    }
}
