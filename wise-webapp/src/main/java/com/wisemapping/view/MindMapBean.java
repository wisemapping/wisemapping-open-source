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

package com.wisemapping.view;

import com.wisemapping.model.Collaboration;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;

import java.text.DateFormat;
import java.util.*;

public class MindMapBean {
    private MindMap mindMap;
    private List<ColaboratorBean> viewers;
    private List<ColaboratorBean> colaborators;

    public MindMapBean(final MindMap mindmap) {
        this.mindMap = mindmap;
        this.colaborators = getColaboratorBy(mindmap.getCollaborations(), CollaborationRole.EDITOR);
        this.viewers = getColaboratorBy(mindmap.getCollaborations(), CollaborationRole.VIEWER);
    }

    public MindMap getMindMap() {
        return mindMap;
    }

    public boolean getPublic() {
        return mindMap.isPublic();
    }

    public String getTitle() {
        return mindMap.getTitle();
    }

    public String getDescription() {
        return mindMap.getDescription();
    }

    public int getId() {
        return mindMap.getId();
    }

    public boolean isStarred() {
        return mindMap.isStarred(Utils.getUser());
    }

    public List<ColaboratorBean> getViewers() {
        return viewers;
    }

    public List<ColaboratorBean> getCollaborators() {
        return colaborators;
    }

    public String getLastEditor() {
        return mindMap.getLastModifierUser();
    }

    public String getLastEditTime() {
        return DateFormat.getInstance().format(mindMap.getLastModificationTime().getTime());
    }

    public String getCreationTime() {
        return DateFormat.getInstance().format(mindMap.getCreationTime().getTime());
    }

    public String getTags() {
        return mindMap.getTags();
    }

    private List<ColaboratorBean> getColaboratorBy(Set<Collaboration> source, CollaborationRole role) {
        List<ColaboratorBean> col = new ArrayList<ColaboratorBean>();
        if (source != null) {
            for (Collaboration mu : source) {
                if (mu.getRole() == role) {
                    col.add(new ColaboratorBean(mu.getCollaborator(), mu.getRole()));
                }
            }
        }
        return col;
    }

    public int getCountColaborators() {
        return colaborators != null ? colaborators.size() : 0;
    }

    public int getCountViewers() {
        return viewers != null ? viewers.size() : 0;
    }

    public int getCountShared() {
        return getCountColaborators() + getCountViewers();
    }

    public boolean isShared() {
        return getCountShared() > 0;
    }

    public void setTitle(String t) {
        mindMap.setTitle(t);
    }

    public void setDescription(String d) {
        mindMap.setDescription(d);
    }

    public User getCreator() {
        return mindMap.getCreator();
    }
}
