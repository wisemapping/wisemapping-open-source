/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

package com.wisemapping.dao;

import com.wisemapping.model.Colaborator;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.model.UserLogin;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.acegisecurity.providers.encoding.PasswordEncoder;

import java.util.List;
import java.util.Set;

public class UserManagerImpl
        extends HibernateDaoSupport
        implements UserManager {

    private PasswordEncoder passwordEncoder;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return getHibernateTemplate().find("from com.wisemapping.model.User user");
    }

    public User getUserBy(final String email) {
        final User user;
        final List users = getHibernateTemplate().find("from com.wisemapping.model.User colaborator where email=?", email);
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same email!";
            user = (User) users.get(0);
        } else {
            user = null;
        }
        return user;
    }

    public Colaborator getColaboratorBy(final String email) {
        final Colaborator cola;
        final List cols = getHibernateTemplate().find("from com.wisemapping.model.Colaborator colaborator where email=?", email);
        if (cols != null && !cols.isEmpty()) {
            assert cols.size() == 1 : "More than one colaborator with the same email!";
            cola = (Colaborator) cols.get(0);
        } else {
            cola = null;
        }
        return cola;
    }

    public User getUserBy(long id)
    {
        return (User)getHibernateTemplate().get(User.class,id);
    }

    public User getUserByUsername(String username) {
        final User user;
        final List users = getHibernateTemplate().find("from com.wisemapping.model.User colaborator where username=?", username);
        if (users != null && !users.isEmpty()) {
            assert users.size() == 1 : "More than one user with the same username!";
            user = (User) users.get(0);
        } else {
            user = null;
        }
        return user;
    }

    public boolean authenticate(final String email, final String password) {
        final boolean result;
        final User user = getUserBy(email);
        result = user != null && user.getPassword().equals(password);
        return result;
    }

    public void createUser(User user) {
        assert user != null : "Trying to store a null user";
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(),null));
        getHibernateTemplate().saveOrUpdate(user);
    }

    public User createUser(User user, Colaborator col)
    {
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(),null));
        assert user != null : "Trying to store a null user";

        final Set<MindmapUser> set = col.getMindmapUsers();
        for (MindmapUser mindmapUser : set) {
            MindmapUser newMapUser = new MindmapUser();            
            newMapUser.setRoleId(mindmapUser.getRole().ordinal());
            newMapUser.setMindMap(mindmapUser.getMindMap());
            newMapUser.setColaborator(user);
            user.addMindmapUser(newMapUser);
        }

        getHibernateTemplate().delete(col);
        getHibernateTemplate().flush();
        getHibernateTemplate().saveOrUpdate(user);
        return user;
    }

    public void auditLogin(UserLogin userLogin) {
        assert userLogin != null : "userLogin is null";
        getHibernateTemplate().save(userLogin);
    }

    public void updateUser(User user) {
        assert user != null : "user is null";
        user.setPassword(passwordEncoder.encodePassword(user.getPassword(),null));
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
