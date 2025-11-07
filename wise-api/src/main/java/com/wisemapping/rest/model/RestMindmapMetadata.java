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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Account;
import com.wisemapping.util.TimeUtils;
import java.util.Calendar;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmapMetadata {
    private String jsonProps;
    private boolean locked;
    private String title;
    private String isLockedBy;

    private String creatorFullName;
    private String role;
    
    // Extended fields to match MapInfo
    private String description;
    private String createdBy; // Email
    private String creationTime;
    private String lastModificationBy;
    private String lastModificationTime;
    private boolean starred;
    private boolean public_;

    // Default constructor for Jackson deserialization
    public RestMindmapMetadata() {
    }

    private RestMindmapMetadata(String title, String jsonProps, String creatorFullName, boolean locked, String isLockedBy, String role) {
        this.jsonProps = jsonProps;
        this.title = title;
        this.locked = locked;
        this.isLockedBy = isLockedBy;
        this.creatorFullName = creatorFullName;
        this.role = role;
    }

    public static RestMindmapMetadata create(Mindmap mindmap, Collaborator collaborator, String jsonProps,
                                             boolean locked, String isLockedBy) {
        return new RestMindmapMetadata(mindmap, collaborator, jsonProps, locked, isLockedBy);
    }

    public static RestMindmapMetadata createAnonymous(Mindmap mindmap, String jsonProps,
                                                      boolean locked, String isLockedBy) {
        return new RestMindmapMetadata(mindmap, null, jsonProps, locked, isLockedBy);
    }

    private RestMindmapMetadata(Mindmap mindmap, Collaborator collaborator, String jsonProps, boolean locked, String isLockedBy) {
        this(
                mindmap.getTitle(),
                jsonProps,
                mindmap.getCreator() != null ? mindmap.getCreator().getFullName() : null,
                locked,
                isLockedBy,
                resolveRole(mindmap, collaborator)
        );

        // Extended fields
        this.description = mindmap.getDescription();
        this.createdBy = mindmap.getCreator() != null ? mindmap.getCreator().getEmail() : null;
        Calendar creationTime = mindmap.getCreationTime();
        this.creationTime = creationTime != null ? TimeUtils.toISO8601(creationTime.getTime()) : null;

        Account lastEditor = mindmap.getLastEditor();
        this.lastModificationBy = lastEditor != null ? lastEditor.getFullName() : (mindmap.getCreator() != null ? mindmap.getCreator().getFullName() : "unknown");
        Calendar lastModificationTime = mindmap.getLastModificationTime();
        this.lastModificationTime = lastModificationTime != null ? TimeUtils.toISO8601(lastModificationTime.getTime()) : null;

        this.starred = collaborator != null && mindmap.isStarred(collaborator);
        this.public_ = mindmap.isPublic();
    }

    private static String resolveRole(Mindmap mindmap, Collaborator collaborator) {
        if (collaborator == null) {
            return "none";
        }
        if (collaborator instanceof Account && mindmap.isCreator((Account) collaborator)) {
            return "owner";
        }
        return mindmap.findCollaboration(collaborator)
                .map(value -> value.getRole().getLabel())
                .orElse("none");
    }

    public String getJsonProps() {
        return jsonProps;
    }

    public void setJsonProps(String jsonProps) {
        this.jsonProps = jsonProps;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsLockedBy() {
        return isLockedBy;
    }

    public void setIsLockedBy(String isLockedBy) {
        this.isLockedBy = isLockedBy;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastModificationBy() {
        return lastModificationBy;
    }

    public void setLastModificationBy(String lastModificationBy) {
        this.lastModificationBy = lastModificationBy;
    }

    public String getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(String lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    @JsonProperty("public")
    public boolean isPublic() {
        return public_;
    }

    @JsonProperty("public")
    public void setPublic(boolean public_) {
        this.public_ = public_;
    }
}
