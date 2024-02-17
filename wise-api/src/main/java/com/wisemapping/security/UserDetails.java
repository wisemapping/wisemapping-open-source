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

package com.wisemapping.security;

import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {
	private final Account user;
    private final boolean isAdmin;

    public  UserDetails(@NotNull final Account user, boolean isAdmin) {
        this.user = user;
        this.isAdmin = isAdmin;
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
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.user.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.user.isActive();

    }

    public Account getUser() {
        return user;
    }
}
