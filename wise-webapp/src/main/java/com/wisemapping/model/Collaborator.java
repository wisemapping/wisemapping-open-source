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

package com.wisemapping.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "COLLABORATOR")
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Collaborator implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;

    @Column(name = "creation_date")
    private Calendar creationDate;

    @OneToMany(mappedBy = "collaborator")
    private Set<Collaboration> collaborations = new HashSet<>();

    public Collaborator() {
    }

    public Collaborator(Set<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public void setCollaborations(Set<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public void addCollaboration(@NotNull Collaboration collaboration) {
        collaborations.add(collaboration);
    }

    public Set<Collaboration> getCollaborations() {
        return collaborations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collaborator that = (Collaborator) o;

        if (id != that.getId()) return false;
        return email != null ? email.equals(that.getEmail()) : that.getEmail() == null;
    }

    @Override
    public int hashCode() {
        int id = this.getId();
        String email = this.getEmail();

        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }


    public boolean identityEquality(@Nullable Collaborator that) {
        if (this == that) {
            return true;
        }

        if (that == null) {
            return false;
        }

        if (id != that.getId()) {
            return false;
        }

        return email != null ? email.equals(that.getEmail()) : that.getEmail() == null;
    }

}
