/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

package com.wisemapping.view;

import com.wisemapping.model.UserRole;
import com.wisemapping.model.Colaborator;
import com.wisemapping.model.User;

public class ColaboratorBean
{
    private UserRole userRole;
    private boolean isUser;
    private Colaborator colaborator;

    public ColaboratorBean(Colaborator colaborator, UserRole role)
    {
        this.colaborator = colaborator;
        this.userRole = role;
        this.isUser = false;
    }

    public ColaboratorBean(User user, UserRole role)
    {
        this.colaborator = user;
        this.userRole = role;
        this.isUser = true;
    }

    public boolean isUser()
    {
        return isUser;
    }

    public String getRole()
    {      
        return userRole.name();
    }

    public String getUsername()
    {
        return isUser ? ((User)colaborator).getUsername() : colaborator.getEmail();
    }

    public String getEmail()
    {
        return colaborator.getEmail();
    }

    public long getId()
    {
        return colaborator.getId();
    }
}
