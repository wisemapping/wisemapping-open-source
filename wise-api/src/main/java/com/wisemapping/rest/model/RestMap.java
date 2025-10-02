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

import com.wisemapping.model.Mindmap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class RestMap {
    private Mindmap mindmap;

    // Default constructor for Jackson deserialization
    public RestMap() {
        this.mindmap = new Mindmap();
    }

    public RestMap(Mindmap mindmap) {
        this.mindmap = mindmap;
    }

    public Mindmap getDelegated() {
        return mindmap;
    }

    public int getId() {
        return mindmap.getId();
    }

    public String getTitle() {
        return mindmap.getTitle();
    }

    public void setTitle(String title) {
        mindmap.setTitle(title);
    }

    public String getDescription() {
        return mindmap.getDescription();
    }

    public void setDescription(String description) {
        mindmap.setDescription(description);
    }

    public String getCreatedBy() {
        if (mindmap.getCreator() != null) {
            return mindmap.getCreator().getEmail();
        }
        return null;
    }

    public int getCreatedById() {
        if (mindmap.getCreator() != null) {
            return mindmap.getCreator().getId();
        }
        return 0;
    }

    public String getCreationTime() {
        Calendar creationTime = mindmap.getCreationTime();
        if (creationTime != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }

    public String getLastModificationBy() {
        return mindmap.getLastEditor() != null ? mindmap.getLastEditor().getEmail() : mindmap.getCreator().getEmail();
    }

    public int getLastModificationById() {
        return mindmap.getLastEditor() != null ? mindmap.getLastEditor().getId() : mindmap.getCreator().getId();
    }

    public String getLastModificationTime() {
        Calendar lastModificationTime = mindmap.getLastModificationTime();
        if (lastModificationTime != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(lastModificationTime.toInstant(), ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return getCreationTime();
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }

    public boolean isLocked() {
        // Mindmap model doesn't have lock functionality yet
        // This would need to be implemented in the future
        return false;
    }

    public String getLockedBy() {
        // Mindmap model doesn't have lock functionality yet
        // This would need to be implemented in the future
        return null;
    }

    public boolean isStarred() {
        // This would need to be determined by the current user's starred maps
        // For now, return false as we don't have user context here
        return false;
    }

    public List<String> getLabels() {
        return mindmap.getLabels().stream()
                .map(label -> label.getTitle())
                .collect(Collectors.toList());
    }

    // Additional setters for testing and updates
    public void setPublic(boolean isPublic) {
        mindmap.setPublic(isPublic);
    }

    public void setLocked(boolean isLocked) {
        // Mindmap model doesn't have lock functionality yet
        // This would need to be implemented in the future
        // No-op for now
    }

    public boolean isSpam() {
        return mindmap.isSpamDetected();
    }

    public String getSpamType() {
        if (mindmap.getSpamInfo() != null && mindmap.getSpamInfo().getSpamTypeCode() != null) {
            return mindmap.getSpamInfo().getSpamTypeCode().name();
        }
        return null;
    }

    public String getSpamDetectedDate() {
        if (mindmap.getSpamInfo() != null && mindmap.getSpamInfo().getCreatedAt() != null) {
            Calendar spamDate = mindmap.getSpamInfo().getCreatedAt();
            LocalDateTime dateTime = LocalDateTime.ofInstant(spamDate.toInstant(), ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }

    public String getSpamDescription() {
        return mindmap.getSpamDescription();
    }
}
