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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;


public class Collaborator  implements Serializable {
    private long id;
    private String email;
    private Calendar creationDate;
    private Set<Collaboration> collaborations = new HashSet<Collaboration>();

    public Collaborator() {}

     public Collaborator(Set<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public void setCollaborations(Set<Collaboration> collaborations)
    {
        this.collaborations = collaborations;
    }

    public void addCollaboration(@NotNull Collaboration collaboration)
    {
       collaborations.add(collaboration);
    }

    public Set<Collaboration> getCollaborations()
    {
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
}
