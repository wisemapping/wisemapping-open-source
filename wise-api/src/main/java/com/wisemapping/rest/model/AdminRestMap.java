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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wisemapping.model.Mindmap;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight DTO for admin console mindmap listing.
 * Optimized to avoid N+1 queries and expensive operations.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminRestMap {
    
    private int id;
    private String title;
    private String description;
    private String createdBy;
    private int createdById;
    private String creationTime;
    private String lastModificationBy;
    private int lastModificationById;
    private String lastModificationTime;
    private boolean isPublic;
    private boolean isSpam;
    private String spamType;
    private String spamDetectedDate;
    private String spamDescription;
    private boolean isCreatorSuspended;
    private int collaboratorCount;

    // Default constructor for Jackson deserialization
    public AdminRestMap() {
    }

    /**
     * Constructor that safely accesses eagerly loaded entities to avoid N+1 queries.
     * Assumes the Mindmap has been loaded with proper JOIN FETCH clauses.
     */
    public AdminRestMap(Mindmap mindmap) {
        this.id = mindmap.getId();
        this.title = mindmap.getTitle();
        this.description = mindmap.getDescription();
        this.isPublic = mindmap.isPublic();
        this.isSpam = mindmap.isSpamDetected();
        
        // Safe access to eagerly loaded creator
        if (mindmap.getCreator() != null) {
            this.createdBy = mindmap.getCreator().getEmail();
            this.createdById = mindmap.getCreator().getId();
            this.isCreatorSuspended = mindmap.getCreator().isSuspended();
        }
        
        // Safe access to eagerly loaded last editor
        if (mindmap.getLastEditor() != null) {
            this.lastModificationBy = mindmap.getLastEditor().getEmail();
            this.lastModificationById = mindmap.getLastEditor().getId();
        } else if (mindmap.getCreator() != null) {
            // Fallback to creator if no last editor
            this.lastModificationBy = mindmap.getCreator().getEmail();
            this.lastModificationById = mindmap.getCreator().getId();
        }
        
        // Format creation time
        if (mindmap.getCreationTime() != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                mindmap.getCreationTime().toInstant(), 
                ZoneId.systemDefault());
            this.creationTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        // Format last modification time
        if (mindmap.getLastModificationTime() != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                mindmap.getLastModificationTime().toInstant(), 
                ZoneId.systemDefault());
            this.lastModificationTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            // Fallback to creation time
            this.lastModificationTime = this.creationTime;
        }
        
        // Safe access to eagerly loaded spam info
        if (mindmap.getSpamInfo() != null) {
            if (mindmap.getSpamInfo().getSpamTypeCode() != null) {
                this.spamType = mindmap.getSpamInfo().getSpamTypeCode().name();
            }
            
            // Set spam detected date
            if (mindmap.getSpamInfo().getUpdatedAt() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                    mindmap.getSpamInfo().getUpdatedAt().toInstant(), 
                    ZoneId.systemDefault());
                this.spamDetectedDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            // Set spam description
            this.spamDescription = mindmap.getSpamInfo().getSpamDescription();
        }
        
        // Use pre-computed collaborator count to avoid initializing the collection
        this.collaboratorCount = mindmap.getCollaboratorCount();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getCreatedById() {
        return createdById;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public String getLastModificationBy() {
        return lastModificationBy;
    }

    public int getLastModificationById() {
        return lastModificationById;
    }

    public String getLastModificationTime() {
        return lastModificationTime;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public String getSpamType() {
        return spamType;
    }

    public String getSpamDescription() {
        return spamDescription;
    }

    // Setters for testing and updates
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public void setLastModificationBy(String lastModificationBy) {
        this.lastModificationBy = lastModificationBy;
    }

    public void setLastModificationById(int lastModificationById) {
        this.lastModificationById = lastModificationById;
    }

    public void setLastModificationTime(String lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }

    public void setSpamType(String spamType) {
        this.spamType = spamType;
    }

    public String getSpamDetectedDate() {
        return spamDetectedDate;
    }

    public void setSpamDetectedDate(String spamDetectedDate) {
        this.spamDetectedDate = spamDetectedDate;
    }

    public void setSpamDescription(String spamDescription) {
        this.spamDescription = spamDescription;
    }

    public boolean isCreatorSuspended() {
        return isCreatorSuspended;
    }

    public void setCreatorSuspended(boolean isCreatorSuspended) {
        this.isCreatorSuspended = isCreatorSuspended;
    }

    public int getCollaboratorCount() {
        return collaboratorCount;
    }

    public void setCollaboratorCount(int collaboratorCount) {
        this.collaboratorCount = collaboratorCount;
    }
}
