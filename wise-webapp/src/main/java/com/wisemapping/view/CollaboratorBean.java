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

package com.wisemapping.view;

import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;

public class CollaboratorBean {
    private final CollaborationRole collaborationRole;
    private final boolean isUser;
    private final Collaborator collaborator;

    public CollaboratorBean(Collaborator collaborator, CollaborationRole role) {
        this.collaborator = collaborator;
        this.collaborationRole = role;
        this.isUser = false;
    }

    public CollaboratorBean(User user, CollaborationRole role) {
        this.collaborator = user;
        this.collaborationRole = role;
        this.isUser = true;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getRole() {
        return collaborationRole.name();
    }

    public String getUsername() {
        return isUser ? ((User) collaborator).getFullName() : collaborator.getEmail();
    }

    public String getEmail() {
        return collaborator.getEmail();
    }

    public int getId() {
        return collaborator.getId();
    }
}
