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

package com.wisemapping.security;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.User;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import java.util.Calendar;
import java.util.List;


public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService, org.springframework.security.core.userdetails.AuthenticationUserDetailsService<OpenIDAuthenticationToken> {
    private UserService userService;
    private String adminUser;

    @Override
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException, DataAccessException {
        final User user = userService.getUserBy(email);

        if (user != null) {
            return new UserDetails(user, isAdmin(email));
        } else {
            throw new UsernameNotFoundException(email);
        }
    }

    @Override
    @NotNull
    public UserDetails loadUserDetails(@NotNull OpenIDAuthenticationToken token) throws UsernameNotFoundException {

        final User tUser = buildUserFromToken(token);
        final User dbUser = userService.getUserBy(tUser.getEmail());

        final User result;
        if (dbUser != null) {
            result = dbUser;
        } else {
            try {
                result = userService.createUser(tUser, false, false);
            } catch (WiseMappingException e) {
                throw new IllegalStateException(e);
            }

        }
        return new UserDetails(result, isAdmin(result.getEmail()));
    }

    @NotNull
    private User buildUserFromToken(@NotNull OpenIDAuthenticationToken token) {
        final User result = new User();

        final List<OpenIDAttribute> attributes = token.getAttributes();
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("email")) {
                final String email = attribute.getValues().get(0);
                result.setEmail(email);
            }

            if (attribute.getName().equals("firstname")) {
                final String firstName = attribute.getValues().get(0);
                result.setFirstname(firstName);

            }

            if (attribute.getName().equals("lastname")) {
                final String lastName = attribute.getValues().get(0);
                result.setLastname(lastName);
            }
            result.setPassword("");
        }

        final Calendar now = Calendar.getInstance();
        result.setActivationDate(now);
        return result;
    }

    private boolean isAdmin(@Nullable String email) {
        return email != null && adminUser != null && email.trim().endsWith(adminUser);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userManager) {
        this.userService = userManager;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

}
