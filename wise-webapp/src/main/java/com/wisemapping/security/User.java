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
