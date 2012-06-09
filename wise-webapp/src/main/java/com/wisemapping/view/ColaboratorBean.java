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

package com.wisemapping.view;

import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;

public class ColaboratorBean
{
    private CollaborationRole collaborationRole;
    private boolean isUser;
    private Collaborator collaborator;

    public ColaboratorBean(Collaborator collaborator, CollaborationRole role)
    {
        this.collaborator = collaborator;
        this.collaborationRole = role;
        this.isUser = false;
    }

    public ColaboratorBean(User user, CollaborationRole role)
    {
        this.collaborator = user;
        this.collaborationRole = role;
        this.isUser = true;
    }

    public boolean isUser()
    {
        return isUser;
    }

    public String getRole()
    {      
        return collaborationRole.name();
    }

    public String getUsername()
    {
        return isUser ? ((User) collaborator).getUsername() : collaborator.getEmail();
    }

    public String getEmail()
    {
        return collaborator.getEmail();
    }

    public long getId()
    {
        return collaborator.getId();
    }
}
