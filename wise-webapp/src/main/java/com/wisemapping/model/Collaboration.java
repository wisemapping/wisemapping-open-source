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

package com.wisemapping.model;

import org.jetbrains.annotations.NotNull;

public class Collaboration {

    private long id;
    private CollaborationRole role;
    private MindMap mindMap;
    private Collaborator collaborator;
    private CollaborationProperties collaborationProperties;

    public Collaboration() {
    }

    public Collaboration(@NotNull CollaborationRole role, @NotNull Collaborator collaborator, @NotNull MindMap mindmap) {
        this.role = role;
        this.mindMap = mindmap;
        this.collaborator = collaborator;

        // Guarantee referential integrity
        mindmap.addCollaboration(this);
        collaborator.addCollaboration(this);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRoleId() {
        return role.ordinal();
    }

    public void setRoleId(int roleId) {
        this.role = CollaborationRole.values()[roleId];
    }

    public CollaborationRole getRole() {
        return role;
    }

    public void setRole(@NotNull CollaborationRole role) {
        this.role = role;
    }

    public MindMap getMindMap() {
        return mindMap;
    }

    public void setMindMap(MindMap mindMap) {
        this.mindMap = mindMap;
    }

    @NotNull
    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(@NotNull Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public CollaborationProperties getCollaborationProperties() {
        return collaborationProperties;
    }

    public void setCollaborationProperties(@NotNull CollaborationProperties collaborationProperties) {
        this.collaborationProperties = collaborationProperties;
    }

    public boolean hasPermissions(@NotNull CollaborationRole role) {
        return this.getRole().ordinal() <= role.ordinal();

    }
}
