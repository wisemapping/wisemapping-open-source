/*
*    Copyright [2011] [wisemapping]
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

import com.wisemapping.dao.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService {
    private UserManager userManager;
    private String adminUser;

    @Override
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException, DataAccessException {
        final com.wisemapping.model.User model = userManager.getUserBy(email);

        if (model != null) {
            return new UserDetails(model, isAdmin(email));
        } else {
            throw new UsernameNotFoundException(email);
        }
    }

    private boolean isAdmin(@Nullable String email) {
        return email != null && adminUser != null && email.trim().endsWith(adminUser);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }
}
