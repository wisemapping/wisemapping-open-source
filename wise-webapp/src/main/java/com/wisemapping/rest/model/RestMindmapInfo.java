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

package com.wisemapping.rest.model;


import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.util.TimeUtils;
import org.codehaus.jackson.annotate.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmapInfo {

    @JsonIgnore
    private Mindmap mindmap;
    private Collaborator collaborator;

    public RestMindmapInfo() {
        this(new Mindmap(), null);

    }

    public RestMindmapInfo(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator) {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
    }

    public String getCreationTime() {
        return TimeUtils.toISO8601(mindmap.getCreationTime().getTime());
    }

    public String getDescription() {
        return mindmap.getDescription();
    }

    public String getTags() {
        return mindmap.getTags();
    }

    public String getTitle() {
        return mindmap.getTitle();
    }

    public int getId() {
        return mindmap.getId();
    }

    public String getCreator() {
        return mindmap.getCreator().getFullName();
    }

    public void setCreator() {
        // Do nothing ...
    }

    public String getRole() {
        final Collaboration collaboration = mindmap.findCollaboration(Utils.getUser());
        return collaboration != null ? collaboration.getRole().getLabel() : "none";
    }

    public void setRole() {
        // Do nothing ...
    }

    public String getLastModifierUser() {
        final User user = mindmap.getLastEditor();
        return user != null ? user.getFullName() : "unknown";
    }

    public String getLastModificationTime() {
        final Calendar calendar = mindmap.getLastModificationTime();
        return TimeUtils.toISO8601(calendar.getTime());
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }

    public void setId(int id) {
    }

    public boolean getStarred() {
        return mindmap.isStarred(collaborator);
    }

    public void setStarred(int value) {

    }

    public void setTitle(String title) {
        mindmap.setTitle(title);
    }

    public void setTags(String tags) {
        mindmap.setTags(tags);
    }

    public void setDescription(String description) {
        mindmap.setDescription(description);
    }

    public void setCreator(String email) {

    }

    public void setLastModificationTime(String value) {
    }

    public void setLastModifierUser(String value) {
    }

    @JsonIgnore
    public Mindmap getDelegated() {
        return this.mindmap;
    }
}
