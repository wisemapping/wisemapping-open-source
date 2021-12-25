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

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.util.ZipUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Mindmap {
    private static final String UTF_8 = "UTF-8";

    //~ Instance fields ......................................................................................
    private int id;
    private Calendar creationTime;
    private String description;

    private boolean isPublic;
    private Calendar lastModificationTime;
    private User lastEditor;

    private Set<Collaboration> collaborations = new HashSet<Collaboration>();
    private Set<Label> labels = new LinkedHashSet<>();

    private User creator;
    private String tags;
    private String title;
    private byte[] zippedXml;

    //~ Constructors .........................................................................................

    public Mindmap() {
    }

    //~ Methods ..............................................................................................

    public void setUnzipXml(@NotNull byte[] value) {
        try {
            final byte[] zip = ZipUtils.bytesToZip(value);
            this.setZippedXml(zip);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setXmlStr(@NotNull String xml) {
        this.setUnzipXml(xml.getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    public byte[] getUnzipXml() {
        byte[] result = new byte[]{};
        if (zippedXml != null) {
            try {
                final byte[] zip = this.getZippedXml();
                result = ZipUtils.zipToBytes(zip);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    @NotNull
    public String getXmlStr() throws UnsupportedEncodingException {
        return new String(this.getUnzipXml(), StandardCharsets.UTF_8);
    }

    @NotNull
    public byte[] getZippedXml() {
        return zippedXml;
    }

    public void setZippedXml(@NotNull byte[] value) {
        this.zippedXml = value;
    }

    public Set<Collaboration> getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(Set<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public void addCollaboration(@NotNull Collaboration collaboration) {
        collaborations.add(collaboration);
    }

    public void removedCollaboration(@NotNull Collaboration collaboration) {
        collaborations.add(collaboration);
    }

    @NotNull public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(@NotNull final Set<Label> labels) {
        this.labels = labels;
    }

    public void addLabel(@NotNull final Label label) {
        this.labels.add(label);
    }

    @Nullable
    public Collaboration findCollaboration(@NotNull Collaborator collaborator) {
        Collaboration result = null;
        for (Collaboration collaboration : collaborations) {
            if (collaboration.getCollaborator().identityEquality(collaborator)) {
                result = collaboration;
                break;
            }
        }
        return result;
    }

    @Nullable
    public Collaboration findCollaboration(@NotNull String email) {
        Collaboration result = null;
        for (Collaboration collaboration : collaborations) {
            if (collaboration.getCollaborator().getEmail().equals(email)) {
                result = collaboration;
                break;
            }
        }
        return result;
    }

    public boolean isPublic() {
        return isPublic;
    }

    //@Todo: This is a hack to overcome some problem with JS EL. For some reason, ${mindmap.public} fails as not supported.
    // More research is needed...
    public boolean isAccessible() {
        return isPublic();
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Calendar getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    @Nullable
    public User getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(@Nullable User lastEditor) {
        this.lastEditor = lastEditor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NotNull
    public String getXmlAsJsLiteral()
            throws IOException {
        String xml = this.getXmlStr();
        if (xml != null) {
            xml = xml.replace("'", "\\'");
            xml = xml.replace("\n", "\\n");
            xml = xml.replace("\r", "");

            xml = xml.replace("\\b", "\\\\b");
            xml = xml.replace("\\t", "\\\\t");
            xml = xml.replace("\\r", "\\\\r");
            xml = xml.replace("\\f", "\\\\f");

            xml = xml.trim();
        }
        return xml;
    }


    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public void setCreator(@NotNull User creator) {
        this.creator = creator;
    }

    public User getCreator() {
        return creator;
    }

    private CollaborationProperties findUserProperty(@NotNull Collaborator collaborator) {
        final Collaboration collaboration = this.findCollaboration(collaborator);
        return collaboration != null ? collaboration.getCollaborationProperties() : null;
    }

    public void setStarred(@NotNull Collaborator collaborator, boolean value) throws WiseMappingException {
        final CollaborationProperties collaborationProperties = findCollaborationProperties(collaborator);
        collaborationProperties.setStarred(value);
    }

    @NotNull
    public CollaborationProperties findCollaborationProperties(@NotNull Collaborator collaborator) throws WiseMappingException {
        return this.findCollaborationProperties(collaborator, true);
    }

    @Nullable
    public CollaborationProperties findCollaborationProperties(@NotNull Collaborator collaborator, boolean forceCheck) throws WiseMappingException {
        if (collaborator == null) {
            throw new IllegalStateException("Collaborator can not be null");
        }

        final Collaboration collaboration = this.findCollaboration(collaborator);
        CollaborationProperties result = null;
        if (collaboration != null) {
            result = collaboration.getCollaborationProperties();
        } else {
            if (forceCheck)
                throw new AccessDeniedSecurityException("Collaborator " + collaborator.getEmail() + " could not access " + this.getId());
        }
        return result;
    }

    public boolean isStarred(@NotNull Collaborator collaborator) {
        final CollaborationProperties collaboratorProperty = this.findUserProperty(collaborator);
        return collaboratorProperty != null && collaboratorProperty.getStarred();
    }

    public static String getDefaultMindmapXml(@NotNull final String title) {

        final StringBuilder result = new StringBuilder();
        result.append("<map version=\"tango\">");
        result.append("<topic central=\"true\" text=\"");
        result.append(StringEscapeUtils.escapeXml(title));
        result.append("\"/></map>");
        return result.toString();
    }

    public Mindmap shallowClone() {
        final Mindmap result = new Mindmap();
        result.setDescription(this.getDescription());
        result.setTitle(this.getTitle());
        result.setUnzipXml(this.getUnzipXml());
        result.setTags(this.getTags());

        return result;
    }

    public boolean hasPermissions(@Nullable Collaborator collaborator, @NotNull CollaborationRole role) {
        boolean result = false;
        if (collaborator != null) {
            final Collaboration collaboration = this.findCollaboration(collaborator);
            if (collaboration != null) {
                result = collaboration.hasPermissions(role);
            }
        }
        return result;

    }
    //creo que no se usa mas
    public boolean hasLabel(@NotNull final String name) {
        for (Label label : this.labels) {
            if (label.getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Nullable public Label findLabel(int labelId) {
        Label result = null;
        for (Label label : this.labels) {
            if (label.getId() == labelId) {
                result = label;
                break;
            }
        }
        return result;
    }

    public void removeLabel(@NotNull final Label label) {
        this.labels.remove(label);
    }
}
