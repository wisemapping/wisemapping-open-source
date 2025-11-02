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

package com.wisemapping.model;


import org.jetbrains.annotations.Nullable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "COLLABORATION")
@NamedQueries({
    @NamedQuery(
        name = "Collaboration.findByCollaboratorId",
        query = "SELECT c FROM Collaboration c WHERE c.collaborator.id = :collaboratorId"
    )
})
public class Collaboration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_id", unique = true)
    private CollaborationRole role;

    @ManyToOne
    @JoinColumn(name = "mindmap_id", nullable = false)
    private Mindmap mindMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "properties_id", nullable = true, unique = true)
    private CollaborationProperties collaborationProperties = new CollaborationProperties();

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


    @Nullable
    public CollaborationProperties getCollaborationProperties() {
        return this.collaborationProperties;
    }

    public void setCollaborationProperties(@Nullable CollaborationProperties collaborationProperties) {
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
        // Use collaborator ID instead of full object comparison to avoid lazy initialization issues
        if (collaborator != null && that.collaborator != null) {
            // Use getId() method which is safe and doesn't trigger lazy loading
            if (collaborator.getId() != that.collaborator.getId()) return false;
        } else if (collaborator != that.collaborator) {
            return false;
        }
        // Use mindMap ID instead of full object comparison to avoid lazy initialization issues
        if (mindMap != null && that.mindMap != null) {
            // Use getId() method which is safe and doesn't trigger lazy loading
            if (mindMap.getId() != that.mindMap.getId()) return false;
        } else if (mindMap != that.mindMap) {
            return false;
        }
        return role == that.role;
    }

    @Override
    public int hashCode() {
        //https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate/
        return 13;
    }
}
