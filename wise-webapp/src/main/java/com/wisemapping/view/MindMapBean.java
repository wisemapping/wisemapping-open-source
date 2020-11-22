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

package com.wisemapping.view;

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MindMapBean {
    private Mindmap mindmap;
    private List<CollaboratorBean> viewers;
    private List<CollaboratorBean> collaborators;
    private Collaborator collaborator;

    public MindMapBean(@NotNull final Mindmap mindmap, @Nullable final Collaborator collaborator) {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
        this.collaborators = filterCollaboratorBy(mindmap.getCollaborations(), CollaborationRole.EDITOR);
        this.viewers = filterCollaboratorBy(mindmap.getCollaborations(), CollaborationRole.VIEWER);
    }

    public boolean getPublic() {
        return mindmap.isPublic();
    }

    //@Todo: This is a hack to overcome some problem with JS EL. For some reason, ${mindmap.public} fails as not supported.
    // More research is needed...
    public boolean isAccessible() {
        return getPublic();
    }

    public String getTitle() {
        return mindmap.getTitle();
    }

    public String getDescription() {
        return mindmap.getDescription();
    }

    public int getId() {
        return mindmap.getId();
    }

    public boolean isStarred() {
        return mindmap.isStarred(collaborator);
    }

    public List<CollaboratorBean> getViewers() {
        return viewers;
    }

    public List<CollaboratorBean> getCollaborators() {
        return collaborators;
    }

    public String getLastEditor() {
        final User lastEditor = mindmap.getLastEditor();
        return lastEditor != null ? lastEditor.getFullName() : "";
    }

    public String getLastEditTime() {
        return DateFormat.getInstance().format(mindmap.getLastModificationTime().getTime());
    }

    public String getCreationTime() {
        return DateFormat.getInstance().format(mindmap.getCreationTime().getTime());
    }

    public String getTags() {
        return mindmap.getTags();
    }

    private List<CollaboratorBean> filterCollaboratorBy(Set<Collaboration> source, CollaborationRole role) {
        List<CollaboratorBean> col = new ArrayList<CollaboratorBean>();
        if (source != null) {
            for (Collaboration mu : source) {
                if (mu.getRole() == role) {
                    col.add(new CollaboratorBean(mu.getCollaborator(), mu.getRole()));
                }
            }
        }
        return col;
    }

    public int getCountCollaborators() {
        return collaborators != null ? collaborators.size() : 0;
    }

    public int getCountViewers() {
        return viewers != null ? viewers.size() : 0;
    }

    public int getCountShared() {
        return getCountCollaborators() + getCountViewers();
    }

    public boolean isShared() {
        return getCountShared() > 0;
    }

    public void setTitle(String t) {
        mindmap.setTitle(t);
    }

    public void setDescription(String d) {
        mindmap.setDescription(d);
    }

    public String getXmlAsJsLiteral() throws IOException {
        return this.mindmap.getXmlAsJsLiteral();
    }

    public String getProperties() throws WiseMappingException {
        String result = null;

        if (collaborator != null) {
            try {
                final CollaborationProperties properties = this.mindmap.findCollaborationProperties(collaborator);
                result = properties.getMindmapProperties();
            } catch (AccessDeniedSecurityException e) {
                // Ignore exception. This is required for the admin could view maps ...
            }
        }

        if (result == null) {
            // It must be public view ...
            result = CollaborationProperties.DEFAULT_JSON_PROPERTIES;
        }

        return result;
    }

    public User getCreator() {
        return mindmap.getCreator();
    }

    public boolean isOwner() {
        return mindmap.hasPermissions(collaborator, CollaborationRole.OWNER);
    }

    public boolean isEditor() {
        return mindmap.hasPermissions(collaborator, CollaborationRole.EDITOR);
    }

    public String getRole() {
        final Collaboration collaboration = this.mindmap.findCollaboration(collaborator);
        return collaboration.getRole().getLabel();
    }


    public Mindmap getDelegated() {
        return mindmap;
    }

}
