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
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {
	private final int userId;
    private final String email;
    private final String password;
    private final boolean isAdmin;
    private transient UserService userService;

    public  UserDetails(@NotNull final Account user, boolean isAdmin, UserService userService) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.isAdmin = isAdmin;
        this.userService = userService;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        final Collection<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
        if (this.isAdmin) {
            final SimpleGrantedAuthority role_admin = new SimpleGrantedAuthority("ROLE_ADMIN");
            result.add(role_admin);
        }
        final SimpleGrantedAuthority role_user = new SimpleGrantedAuthority("ROLE_USER");
        result.add(role_user);
        return result;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Account is locked if it's suspended - fetch fresh data from DB
        final Account freshUser = getFreshAccount();
        return freshUser != null && !freshUser.isSuspended();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Account is enabled only if it's active AND not suspended - fetch fresh data from DB
        final Account freshUser = getFreshAccount();
        return freshUser != null && freshUser.isActive() && !freshUser.isSuspended();
    }

    /**
     * Get fresh account data from database to avoid stale session data
     */
    private Account getFreshAccount() {
        if (userService != null) {
            return userService.getUserBy(userId);
        }
        return null;
    }

    /**
     * Get fresh user account from database.
     * This ensures suspension status and other mutable fields are up-to-date.
     */
    public Account getUser() {
        Account freshUser = getFreshAccount();
        if (freshUser == null) {
            throw new IllegalStateException("User could not be retrieved from database");
        }
        return freshUser;
    }

    public int getUserId() {
        return userId;
    }
}
