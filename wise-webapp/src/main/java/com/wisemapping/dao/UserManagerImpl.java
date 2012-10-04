/*
*    Copyright [2012] [wisemapping]
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

import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;
import com.wisemapping.model.AccessAuditory;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.security.authentication.encoding.PasswordEncoder;
//import org.acegisecurity.providers.encoding.PasswordEncoder;

import java.util.List;
import java.util.Set;

public class UserManagerImpl
        extends HibernateDaoSupport
        implements UserManager {

    private PasswordEncoder passwordEncoder;

    public void setEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return getHibernateTemplate().find("from com.wisemapping.model.User user");
    }


    @Override
    public User getUserBy(@NotNull final String email) {
        User user = null;
        final List<User> users = getHibernateTemplate().find("from com.wisemapping.model.User colaborator where email=?", email);
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            user = users.get(0);
        }
        return user;
    }

    @Override
    public Collaborator getCollaboratorBy(final String email) {
        final Collaborator cola;
        final List cols = getHibernateTemplate().find("from com.wisemapping.model.Collaborator colaborator where email=?", email);
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            cola = (Collaborator) cols.get(0);
        } else {
            cola = null;
        }
        return cola;
    }

    public User getUserBy(long id) {
        return getHibernateTemplate().get(User.class, id);
    }

    @Override
    public void createUser(User user) {
        assert user != null : "Trying to store a null user";
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
        getHibernateTemplate().saveOrUpdate(user);
    }

    @Override
    public User createUser(@NotNull User user, @NotNull Collaborator col) {
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
        assert user != null : "Trying to store a null user";

        final Set<Collaboration> set = col.getCollaborations();
        for (Collaboration collaboration : set) {
            Collaboration newMapUser = new Collaboration();
            newMapUser.setRoleId(collaboration.getRole().ordinal());
            newMapUser.setMindMap(collaboration.getMindMap());
            newMapUser.setCollaborator(user);
            user.addCollaboration(newMapUser);
        }

        getHibernateTemplate().delete(col);
        getHibernateTemplate().flush();
        getHibernateTemplate().saveOrUpdate(user);
        return user;
    }

    @Override
    public void deleteUser(@NotNull User user) {
        final Collaborator collaborator = this.getCollaboratorBy(user.getEmail());
        getHibernateTemplate().delete(collaborator);
        getHibernateTemplate().delete(user);
        getHibernateTemplate().flush();
    }

    public void auditLogin(@NotNull AccessAuditory accessAuditory) {
        assert accessAuditory != null : "accessAuditory is null";
        getHibernateTemplate().save(accessAuditory);
    }

    public void updateUser(@NotNull User user) {
        assert user != null : "user is null";
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
        getHibernateTemplate().update(user);
    }

    public User getUserByActivationCode(long code) {
        final User user;
        final List users = getHibernateTemplate().find("from com.wisemapping.model.User user where activationCode=?", code);
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same username!";
            user = (User) users.get(0);
        } else {
            user = null;
        }
        return user;
    }
}
