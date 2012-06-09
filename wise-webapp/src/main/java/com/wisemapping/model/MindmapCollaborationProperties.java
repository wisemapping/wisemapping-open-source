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

public class MindmapCollaborationProperties {
    private long id;
    private boolean starred;
    private Collaborator collaborator;
    private MindMap mindmap;


    public MindmapCollaborationProperties(@NotNull Collaborator collaborator, @NotNull MindMap mindmap) {
        this.collaborator = collaborator;
        this.mindmap = mindmap;
    }

    public MindmapCollaborationProperties(){

    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }
    public Collaborator getCollaborator() {
        return collaborator;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MindMap getMindmap() {
        return mindmap;
    }

    public void setMindmap(@NotNull MindMap mindmap) {
        this.mindmap = mindmap;
    }
}
