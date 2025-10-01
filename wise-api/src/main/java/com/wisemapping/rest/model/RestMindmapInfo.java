/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
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
import com.wisemapping.model.*;
import com.wisemapping.security.Utils;
import com.wisemapping.util.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmapInfo {

    public static final String ROLE_NONE = "none";
    @JsonIgnore
    private final Mindmap mindmap;
    @JsonIgnore
    private Set<RestLabel> restLabels;

    @JsonIgnore
    private int mapId = -1;

    private final Collaborator collaborator;

    public RestMindmapInfo() {
        this(new Mindmap(), null);

    }

    public RestMindmapInfo(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator) {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
    }

    public void setCreationTime(String value) {
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

    public String getTitle() {
        return mindmap.getTitle();
    }

    public void setTitle(String title) {
        mindmap.setTitle(title);
    }

    public Set<RestLabel> getLabels() {
        // Support test deserialization...
        Set<RestLabel> result = this.restLabels;
        if (result == null) {
            final Account me = Utils.getUser();
            result = mindmap.getLabels().
                    stream()
                    .filter(l -> l.getCreator().equals(me))
                    .map(RestLabel::new)
                    .collect(Collectors.toSet());
        }
        return result;
    }

    public void setLabels(Set<RestLabel> restLabels) {
        this.restLabels = restLabels;
    }

    public int getId() {
        int result = this.mapId;
        if (mapId == -1) {
            result = mindmap.getId();
        }
        return result;
    }

    public void setId(int id) {
        this.mapId = id;
    }

    public String getCreator() {
        final Account creator = mindmap.getCreator();
        return creator != null ? creator.getFullName() : null;
    }

    public void setCreator(String email) {

    }

    public void setCreator() {
        // Do nothing ...
    }

    public String getRole() {
        final Account user = Utils.getUser();
        final Optional<Collaboration> collaboration = mindmap.findCollaboration(user);
        return  collaboration.map(value -> value.getRole().getLabel()).orElse(ROLE_NONE);
    }

    public void setRole(String value) {
        // Do nothing ...
    }

    public String getLastModifierUser() {
        final Account user = mindmap.getLastEditor();
        return user != null ? user.getFullName() : "unknown";
    }

    public void setLastModifierUser(String value) {
    }

    public String getLastModificationTime() {
        final Calendar calendar = mindmap.getLastModificationTime();
        return calendar != null ? TimeUtils.toISO8601(calendar.getTime()) : null;
    }

    public void setLastModificationTime(String value) {
    }

    public boolean getPublic() {
        return mindmap.isPublic();
    }

    public boolean getSpamDetected() {
        return mindmap.isSpamDetected();
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
