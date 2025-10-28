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
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User oauth2User;
    private final Account account;
    private final boolean isAdmin;
    
    public CustomOAuth2User(@NotNull OAuth2User oauth2User, @NotNull Account account, @NotNull String adminUser) {
        this.oauth2User = oauth2User;
        this.account = account;
        this.isAdmin = isAdmin(account.getEmail(), adminUser);
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return authorities based on the account, matching UserDetails behavior
        if (!account.isActive() || account.isSuspended()) {
            return Collections.emptyList();
        }
        
        final Collection<GrantedAuthority> result = new ArrayList<>();
        if (this.isAdmin) {
            result.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        result.add(new SimpleGrantedAuthority("ROLE_USER"));
        return result;
    }
    
    @Override
    public String getName() {
        return account.getEmail();
    }
    
    public Account getAccount() {
        return account;
    }
    
    public OAuth2User getOauth2User() {
        return oauth2User;
    }
    
    private boolean isAdmin(@NotNull String email, @NotNull String adminUser) {
        return email != null && adminUser != null && !adminUser.isEmpty() && email.trim().endsWith(adminUser);
    }
}
