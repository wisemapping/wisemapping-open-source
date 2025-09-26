/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.stomp.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Command for adding a new topic to a mind map.
 * Maps to the frontend AddTopicCommand.
 */
public class AddTopicCommand extends StompCommand {

    @Nullable
    private String text;

    @Nullable
    private String note;

    @Nullable
    private String linkUrl;

    @Nullable
    private Integer parentTopicId;

    private boolean central = false;

    private boolean bold = false;

    private boolean italic = false;

    public AddTopicCommand() {
        super();
    }

    public AddTopicCommand(@NotNull String mindmapId, @NotNull String userId) {
        super(mindmapId, userId);
    }

    @Override
    @NotNull
    public String getActionType() {
        return "ADD_TOPIC";
    }

    @Override
    public void validate() {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic text cannot be null or empty");
        }
        if (text.length() > 1000) {
            throw new IllegalArgumentException("Topic text cannot exceed 1000 characters");
        }
        if (note != null && note.length() > 10000) {
            throw new IllegalArgumentException("Topic note cannot exceed 10000 characters");
        }
        if (linkUrl != null && !isValidUrl(linkUrl)) {
            throw new IllegalArgumentException("Invalid link URL format");
        }
    }

    @Nullable
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Nullable
    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    @Nullable
    @JsonProperty("linkUrl")
    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(@Nullable String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @Nullable
    @JsonProperty("parentTopicId")
    public Integer getParentTopicId() {
        return parentTopicId;
    }

    public void setParentTopicId(@Nullable Integer parentTopicId) {
        this.parentTopicId = parentTopicId;
    }

    @JsonProperty("central")
    public boolean isCentral() {
        return central;
    }

    public void setCentral(boolean central) {
        this.central = central;
    }

    @JsonProperty("bold")
    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    @JsonProperty("italic")
    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    private boolean isValidUrl(@NotNull String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
