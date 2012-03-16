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

public class MindmapUser {

    private int id;
    private int roleId;
    private MindMap mindMap;   
    private Collaborator collaborator;

    public MindmapUser(){ }

    public MindmapUser(int role, Collaborator collaborator, MindMap mindmap)
    {
        this.roleId = role;
        this.mindMap =mindmap;
        this.collaborator = collaborator;

        // Guarantee referential integrity
		mindmap.addMindmapUser(this);
		collaborator.addMindmapUser(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public UserRole getRole() {
        return UserRole.values()[roleId];
    }

    public boolean isOwner() {
        return getRole() == UserRole.OWNER;
    }

    public boolean isColaborator() {
        return getRole() == UserRole.COLLABORATOR;
    }

    public boolean isViewer() {
        return getRole() == UserRole.VIEWER;
    }

    public MindMap getMindMap() {
        return mindMap;
    }

    public void setMindMap(MindMap mindMap) {
        this.mindMap = mindMap;
    }

    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }    
}
