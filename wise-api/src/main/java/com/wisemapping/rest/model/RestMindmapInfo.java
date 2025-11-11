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
import java.util.Collections;
import java.util.LinkedHashSet;
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
    private final Collaboration userCollaboration;
    @JsonIgnore
    private Set<RestLabel> restLabels;

    @JsonIgnore
    private int mapId = -1;

    private final Collaborator collaborator;

    private String cachedCreationTime;
    private String cachedDescription;
    private String cachedTitle;
    private String cachedCreatorName;
    private String cachedRole;
    private String cachedLastModifierName;
    private String cachedLastModificationTime;
    private Boolean cachedPublic;
    private Boolean cachedSpamDetected;
    private Boolean cachedStarred;

    public RestMindmapInfo() {
        this(new Mindmap(), null, null);

    }

    public RestMindmapInfo(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator) {
        this(mindmap, collaborator, null);
    }

    public RestMindmapInfo(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator,
                           @Nullable Collaboration userCollaboration) {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
        this.userCollaboration = userCollaboration;
        this.restLabels = buildLabelsForUser(this.mindmap, this.collaborator);
        cacheMindmapProperties();
    }

    public void setCreationTime(String value) {
        // Ignore
    }

    public String getCreationTime() {
        if (cachedCreationTime != null) {
            return cachedCreationTime;
        }
        final Calendar creationTime = mindmap.getCreationTime();
        return creationTime != null ? TimeUtils.toISO8601(creationTime.getTime()) : null;
    }

    public String getDescription() {
        if (cachedDescription != null) {
            return cachedDescription;
        }
        return mindmap.getDescription();
    }

    public void setDescription(String description) {
        this.cachedDescription = description;
        mindmap.setDescription(description);
    }

    public String getTitle() {
        if (cachedTitle != null) {
            return cachedTitle;
        }
        return mindmap.getTitle();
    }

    public void setTitle(String title) {
        this.cachedTitle = title;
        mindmap.setTitle(title);
    }

    public Set<RestLabel> getLabels() {
        if (this.restLabels == null) {
            final Account me = Utils.getUser();
            this.restLabels = buildLabelsForUser(mindmap, me);
        }
        return this.restLabels;
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
        if (cachedCreatorName != null) {
            return cachedCreatorName;
        }
        final Account creator = mindmap.getCreator();
        return creator != null ? creator.getFullName() : null;
    }

    public void setCreator(String email) {

    }

    public void setCreator() {
        // Do nothing ...
    }

    public String getRole() {
        if (cachedRole != null) {
            return cachedRole;
        }
        return resolveRole(this.mindmap, Utils.getUser(), null);
    }

    public void setRole(String value) {
        // Do nothing ...
    }

    public String getLastModifierUser() {
        if (cachedLastModifierName != null) {
            return cachedLastModifierName;
        }
        final Account user = mindmap.getLastEditor();
        return user != null ? user.getFullName() : "unknown";
    }

    public void setLastModifierUser(String value) {
    }

    public String getLastModificationTime() {
        if (cachedLastModificationTime != null) {
            return cachedLastModificationTime;
        }
        final Calendar calendar = mindmap.getLastModificationTime();
        return calendar != null ? TimeUtils.toISO8601(calendar.getTime()) : null;
    }

    public void setLastModificationTime(String value) {
    }

    public boolean getPublic() {
        if (cachedPublic != null) {
            return cachedPublic;
        }
        return mindmap.isPublic();
    }

    public boolean getSpamDetected() {
        if (cachedSpamDetected != null) {
            return cachedSpamDetected;
        }
        return mindmap.isSpamDetected();
    }

    public boolean getStarred() {
        if (cachedStarred != null) {
            return cachedStarred;
        }
        return resolveStarred(mindmap, collaborator, userCollaboration);
    }

    public void setStarred(boolean value) {
        this.cachedStarred = value;
        if (userCollaboration != null && userCollaboration.getCollaborationProperties() != null) {
            userCollaboration.getCollaborationProperties().setStarred(value);
        }
    }

    @JsonIgnore
    public Mindmap getDelegated() {
        return this.mindmap;
    }

    private void cacheMindmapProperties() {
        this.cachedCreationTime = formatCalendar(mindmap.getCreationTime());
        this.cachedDescription = mindmap.getDescription();
        this.cachedTitle = mindmap.getTitle();
        this.cachedCreatorName = formatCollaboratorName(mindmap.getCreator());
        this.cachedRole = resolveRole(mindmap, collaborator, userCollaboration);
        this.cachedLastModifierName = formatCollaboratorName(mindmap.getLastEditor());
        this.cachedLastModificationTime = formatCalendar(mindmap.getLastModificationTime());
        this.cachedPublic = mindmap.isPublic();
        this.cachedSpamDetected = mindmap.isSpamDetected();
        this.cachedStarred = resolveStarred(mindmap, collaborator, userCollaboration);
    }

    private static String formatCalendar(Calendar calendar) {
        return calendar != null ? TimeUtils.toISO8601(calendar.getTime()) : null;
    }

    private static String formatCollaboratorName(Account account) {
        return account != null ? account.getFullName() : null;
    }

    private static String resolveRole(@NotNull Mindmap mindmap,
                                      @Nullable Collaborator collaborator,
                                      @Nullable Collaboration collaboration) {
        Collaboration resolved = collaboration;
        if (resolved == null && collaborator != null) {
            resolved = mindmap.findCollaboration(collaborator).orElse(null);
        }
        if (resolved != null) {
            return resolved.getRole().getLabel();
        }
        return ROLE_NONE;
    }

    private static boolean resolveStarred(@NotNull Mindmap mindmap,
                                          @Nullable Collaborator collaborator,
                                          @Nullable Collaboration collaboration) {
        if (collaboration != null && collaboration.getCollaborationProperties() != null) {
            return collaboration.getCollaborationProperties().getStarred();
        }
        if (collaborator != null) {
            return mindmap.isStarred(collaborator);
        }
        return false;
    }

    private static Set<RestLabel> buildLabelsForUser(@NotNull Mindmap mindmap,
                                                     @Nullable Collaborator collaborator) {
        if (collaborator == null) {
            return Collections.emptySet();
        }
        return mindmap.getLabels()
                .stream()
                .filter(label -> collaborator.equals(label.getCreator()))
                .map(RestMindmapInfo::initializeLabel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static RestLabel initializeLabel(@NotNull MindmapLabel label) {
        // Touch the properties we expose in JSON so Hibernate initializes proxies before serialization
        label.getTitle();
        label.getColor();
        if (label.getParent() != null) {
            label.getParent().getTitle();
            label.getParent().getColor();
        }
        return new RestLabel(label);
    }
}
