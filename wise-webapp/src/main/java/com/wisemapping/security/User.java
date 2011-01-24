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

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

public class User implements UserDetails {
    private com.wisemapping.model.User model;

    public User(com.wisemapping.model.User model) {
        this.model = model;
    }

    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_USER")};
    }

    public String getPassword() {        
        return model.getPassword();
    }

    public String getUsername() {
        return model.getEmail();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
       return this.model.isActive();
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return this.model.isActive();
    }

    public com.wisemapping.model.User getModel() {
        return model;
    }

    public String getDisplayName() {
        return model.getFirstname();
    }
}
