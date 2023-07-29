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

import org.jetbrains.annotations.NotNull;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "COLLABORATION_PROPERTIES")
public class CollaborationProperties implements Serializable  {
    public static final String DEFAULT_JSON_PROPERTIES = "{zoom:0.8}";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean starred;
    @Column(name = "mindmap_properties")
    private String mindmapProperties;

    public CollaborationProperties() {

    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    public String getMindmapProperties() {
        return mindmapProperties == null ? DEFAULT_JSON_PROPERTIES : mindmapProperties;
    }

    public void setMindmapProperties(@NotNull String mindmapProperties) {
        this.mindmapProperties = mindmapProperties;
    }
}
