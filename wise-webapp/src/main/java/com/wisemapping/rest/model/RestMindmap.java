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

package com.wisemapping.rest.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import com.wisemapping.util.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Calendar;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmap {
    @JsonIgnore
    private final Collaborator collaborator;
    @JsonIgnore
    private final Mindmap mindmap;
    @Nullable
    private String properties;

    public RestMindmap() throws WiseMappingException {
        this(new Mindmap(), null);
    }

    public RestMindmap(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator) throws WiseMappingException {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
        if (collaborator != null) {
            final CollaborationProperties collaborationProperties = mindmap.findCollaborationProperties(collaborator, false);
            if (collaborationProperties != null) {
                this.properties = collaborationProperties.getMindmapProperties();
            }
        }
    }

    public void setCreationTime(final String creationTime){
        // Ignore
    }

    public String getCreationTime() {
        final Calendar creationTime = mindmap.getCreationTime();
        String result = null;
        if (creationTime != null) {
            result = TimeUtils.toISO8601(creationTime.getTime());
        }
        return result;
    }

    public String getDescription() {
        return mindmap.getDescription();
    }

    public void setDescription(String description) {
        mindmap.setDescription(description);
    }

    public String getTags() {
        return mindmap.getTags();
    }

    public void setTags(String tags) {
        mindmap.setTags(tags);
    }

    public String getTitle() {
        return mindmap.getTitle();
    }

    public void setTitle(String title) {
        mindmap.setTitle(title);
    }

    public int getId() {
        return mindmap.getId();
    }

    public void setId(int id) {
        mindmap.setId(id);
    }

    public String getCreator() {
        final User creator = mindmap.getCreator();
        return creator != null ? creator.getEmail() : null;
    }

    public void setCreator(String creatorUser) {
    }


    public RestCollaborator getLastModifierUser() {
        final User lastEditor = mindmap.getLastEditor();

        RestCollaborator result = null;
        if (lastEditor != null && mindmap.hasPermissions(collaborator, CollaborationRole.EDITOR)) {
            result = new RestCollaborator(lastEditor);
        }
        return result;
    }

    public void setLastModifierUser(RestUser lastModifierUser) {
    }

    public String getLastModificationTime() {
        final Calendar date = mindmap.getLastModificationTime();
        String result = null;
        if (date != null) {
            result = TimeUtils.toISO8601(date.getTime());
        }
        return result;
    }

    public void setLastModificationTime(final String value) {
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }

    public void setPublic(boolean value) {
        // return mindmap.isPublic();
    }

    public String getXml() throws IOException {
        return mindmap.getXmlStr();
    }

    public void setXml(@Nullable String xml) throws IOException {

        if (xml != null)
            mindmap.setXmlStr(xml);
    }

    public String getOwner() {
        final User owner = mindmap.getCreator();
        return owner != null ? owner.getEmail() : null;
    }

    public void setOwner(String owner) {

    }

    @Nullable
    public String getProperties() {
        return properties;
    }

    public void setProperties(@Nullable String properties) {
        this.properties = properties;
    }

    public boolean getStarred() {
        boolean result = false;
        if (collaborator != null) {
            result = mindmap.isStarred(collaborator);
        }
        return result;
    }

    public void setStarred(boolean value) throws WiseMappingException {
        if (collaborator != null) {
            mindmap.setStarred(collaborator, value);
        }
    }

    @JsonIgnore
    public Mindmap getDelegated() {
        return this.mindmap;
    }
}
