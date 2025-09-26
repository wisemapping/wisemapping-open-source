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

import java.util.Map;

/**
 * Command for changing features of existing topics.
 * Maps to the frontend ChangeFeatureCommand.
 */
public class ChangeFeatureCommand extends StompCommand {

    @NotNull
    private Integer topicId;

    @NotNull
    private Integer featureId;

    @Nullable
    private String featureType;

    @Nullable
    private String newValue;

    @Nullable
    private Map<String, Object> attributes;

    public ChangeFeatureCommand() {
        super();
    }

    public ChangeFeatureCommand(@NotNull String mindmapId, @NotNull String userId, @NotNull Integer topicId, @NotNull Integer featureId) {
        super(mindmapId, userId);
        this.topicId = topicId;
        this.featureId = featureId;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "CHANGE_FEATURE";
    }

    @Override
    public void validate() {
        if (topicId == null) {
            throw new IllegalArgumentException("Topic ID cannot be null");
        }
        if (featureId == null) {
            throw new IllegalArgumentException("Feature ID cannot be null");
        }
        if (newValue != null && newValue.length() > 10000) {
            throw new IllegalArgumentException("New value cannot exceed 10000 characters");
        }
    }

    @NotNull
    @JsonProperty("topicId")
    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(@NotNull Integer topicId) {
        this.topicId = topicId;
    }

    @NotNull
    @JsonProperty("featureId")
    public Integer getFeatureId() {
        return featureId;
    }

    public void setFeatureId(@NotNull Integer featureId) {
        this.featureId = featureId;
    }

    @Nullable
    @JsonProperty("featureType")
    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(@Nullable String featureType) {
        this.featureType = featureType;
    }

    @Nullable
    @JsonProperty("newValue")
    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(@Nullable String newValue) {
        this.newValue = newValue;
    }

    @Nullable
    @JsonProperty("attributes")
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(@Nullable Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
