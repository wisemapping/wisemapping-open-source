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
import com.wisemapping.model.Label;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.util.TimeUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public void setCreationTime(String value){
        // Ignore
    }

    public String getCreationTime() {
        final Calendar creationTime = mindmap.getCreationTime();
        return creationTime != null ? TimeUtils.toISO8601(creationTime.getTime()) : null;
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
     public Set<RestLabel> getLabels() {
         final Set<RestLabel> result = new LinkedHashSet<>();
         final User me = Utils.getUser();
         for (Label label : mindmap.getLabels()) {
             if (label.getCreator().equals(me)) {
                 result.add(new RestLabel(label));
             }
         }
         return result;
     }

    public int getId() {
        return mindmap.getId();
    }

    public void setId(int id) {
    }

    public String getCreator() {
        final User creator = mindmap.getCreator();
        return creator!=null?creator.getFullName():null;
    }

    public void setCreator(String email) {

    }

    public void setCreator() {
        // Do nothing ...
    }

    public String getRole() {
        final Collaboration collaboration = mindmap.findCollaboration(Utils.getUser());
        return collaboration != null ? collaboration.getRole().getLabel() : "none";
    }

    public void setRole(String value) {
        // Do nothing ...
    }

    public String getLastModifierUser() {
        final User user = mindmap.getLastEditor();
        return user != null ? user.getFullName() : "unknown";
    }

    public void setLastModifierUser(String value) {
    }

    public String getLastModificationTime() {
        final Calendar calendar = mindmap.getLastModificationTime();
        return calendar!=null?TimeUtils.toISO8601(calendar.getTime()):null;
    }

    public void setLastModificationTime(String value) {
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }

    public boolean getStarred() {
        return mindmap.isStarred(collaborator);
    }

    public void setStarred(boolean value) {

    }

    @JsonIgnore
    public Mindmap getDelegated() {
        return this.mindmap;
    }
}
