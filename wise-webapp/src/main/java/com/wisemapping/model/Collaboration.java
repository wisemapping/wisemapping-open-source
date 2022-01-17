/*
*    Copyright [2015] [wisemapping]
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


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "COLLABORATION")
public class Collaboration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;;

    @Column(name = "role_id",unique = true,nullable = true)
    private CollaborationRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="mindmap_id",nullable = false)
    private Mindmap mindMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="colaborator_id",nullable = false)
    private Collaborator collaborator;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name="properties_id",nullable = false, unique = true)
    private CollaborationProperties collaborationProperties =  new CollaborationProperties();;

    public Collaboration() {
    }

    public Collaboration(@NotNull CollaborationRole role, @NotNull Collaborator collaborator, @NotNull Mindmap mindmap) {
        this.role = role;
        this.mindMap = mindmap;
        this.collaborator = collaborator;

        // Guarantee referential integrity
        mindmap.addCollaboration(this);
        collaborator.addCollaboration(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Mindmap getMindMap() {
        return mindMap;
    }

    public void setMindMap(Mindmap mindMap) {
        this.mindMap = mindMap;
    }

    @NotNull
    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(@NotNull Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    @NotNull
    public CollaborationProperties getCollaborationProperties() {
        return this.collaborationProperties;
    }

    public void setCollaborationProperties(@NotNull CollaborationProperties collaborationProperties) {
        this.collaborationProperties = collaborationProperties;
    }

    public boolean hasPermissions(@NotNull CollaborationRole role) {
        return this.getRole().ordinal() <= role.ordinal();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collaboration that = (Collaboration) o;

        if (id != that.id) return false;
        if (collaborator != null ? !collaborator.equals(that.collaborator) : that.collaborator != null) return false;
        if (mindMap != null ? !mindMap.equals(that.mindMap) : that.mindMap != null) return false;
        return role == that.role;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (mindMap != null ? mindMap.hashCode() : 0);
        result = 31 * result + (collaborator != null ? collaborator.hashCode() : 0);
        return result;
    }
}
