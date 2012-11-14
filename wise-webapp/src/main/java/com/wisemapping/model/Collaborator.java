/*
*    Copyright [2012] [wisemapping]
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
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class Collaborator implements Serializable {
    private long id;
    private String email;
    private Calendar creationDate;
    private Set<Collaboration> collaborations = new HashSet<Collaboration>();

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

        if (id != that.id) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }


    public boolean equalCollab(@Nullable Collaborator that) {
        if (this == that) return true;
        if (that == null) return false;

        if (id != that.id) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;

        return true;

    }

}
