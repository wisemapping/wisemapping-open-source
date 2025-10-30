/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.security;


import com.wisemapping.model.Account;
import com.wisemapping.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService {
    private static final Logger logger = LogManager.getLogger();
    
    @Autowired
    private UserService userService;

    @Value("${app.admin.user}")
    private String adminUser;

    @Override
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException, DataAccessException {
        final Account user = userService.getUserBy(email);

        if (user != null) {
            return new UserDetails(user, isAdmin(email), userService);
        } else {
            logger.warn("User not found: {}", email);
            throw new UsernameNotFoundException(email);
        }
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
