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

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.util.ZipUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MindMap {
    private static final String UTF_8 = "UTF-8";

    //~ Instance fields ......................................................................................
    private int id;
    private Calendar creationTime;
    private String description;

    private boolean isPublic;
    private Calendar lastModificationTime;
    private String lastModifierUser;

    private Set<Collaboration> collaborations = new HashSet<Collaboration>();

    private User creator;
    private String properties;
    private String tags;
    private String title;
    private byte[] xml;

    //~ Constructors .........................................................................................

    public MindMap() {
    }

    //~ Methods ..............................................................................................

    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    public void setXmlStr(@NotNull String xml)
            throws IOException {
        this.xml = xml.getBytes(UTF_8);
    }

    public byte[] getXml() {
        return xml;
    }

    public String getXmlStr() throws UnsupportedEncodingException {
        String result = null;
        if (this.xml != null) {
            result = new String(this.xml, UTF_8);
        }
        return result;
    }

    public byte[] getZippedXml()
            throws IOException {
        byte[] result = this.xml;
        if (result != null) {
            result = ZipUtils.stringToZip(new String(result, UTF_8));
        }
        return result;
    }

    public void setZippedXml(byte[] xml)
            throws IOException {
        this.xml = ZipUtils.zipToString(xml).getBytes(UTF_8);
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getProperties() {
        String ret;
        if (properties == null) {
            ret = "{zoom:0.85,saveOnLoad:true}";
        } else {
            ret = properties;
        }

        return ret;
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

    @Nullable
    public Collaboration findCollaboration(@NotNull Collaborator collaborator) {
        return this.findCollaboration(collaborator.getEmail());
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

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Calendar getLastModificationTime() {
        return lastModificationTime;
    }

    public Date getLastModificationDate() {
        return new Date();
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public String getLastModifierUser() {
        return lastModifierUser;
    }

    public void setLastModifierUser(String lastModifierUser) {
        this.lastModifierUser = lastModifierUser;
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

    public String getXmlAsJsLiteral()
            throws IOException {
        String xml = this.getXmlStr();
        if (xml != null) {
            xml = xml.replace("'", "\\'");
            xml = xml.replaceAll("\\r|\\n", "");
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
        if (creator == null) {
            throw new IllegalArgumentException("Owner can not be null");
        }
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
        if (collaborator == null) {
            throw new IllegalStateException("Collaborator can not be null");
        }

        final Collaboration collaboration = this.findCollaboration(collaborator);
        if (collaboration == null) {
            throw new WiseMappingException("User is not collaborator");
        }

        if (collaboration.getCollaborationProperties() == null) {
            collaboration.setCollaborationProperties(new CollaborationProperties());
        }
        collaboration.getCollaborationProperties().setStarred(value);
    }

    public boolean isStarred(@NotNull Collaborator collaborator) {
        final CollaborationProperties collaboratorProperty = this.findUserProperty(collaborator);
        return collaboratorProperty != null && collaboratorProperty.getStarred();
    }

    public static String getDefaultMindmapXml(@NotNull final String title) {

        final StringBuilder result = new StringBuilder();
        result.append("<map version=\"tango\">");
        result.append("<topic central=\"true\" text=\"");
        result.append(title);
        result.append("\"/></map>");
        return result.toString();
    }

    public MindMap shallowClone() {
        final MindMap result = new MindMap();
        result.setDescription(this.getDescription());
        result.setTitle(this.getTitle());
        result.setProperties(this.getProperties());
        result.setXml(this.getXml());
        result.setTags(this.getTags());

        return result;
    }

    public boolean hasPermissions(@NotNull Collaborator collaborator, @NotNull CollaborationRole role) {
        final Collaboration collaboration = this.findCollaboration(collaborator);
        boolean result = false;
        if (collaboration != null) {
            result = collaboration.hasPermissions(role);
        }
        return result;

    }
}
