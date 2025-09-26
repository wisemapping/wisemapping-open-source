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
 * Command for adding features (notes, links, etc.) to existing topics.
 * Maps to the frontend AddFeatureCommand.
 */
public class AddFeatureCommand extends StompCommand {

    @NotNull
    private Integer topicId;

    @NotNull
    private String featureType;

    @Nullable
    private String featureValue;

    @Nullable
    private Integer featureId;

    public AddFeatureCommand() {
        super();
    }

    public AddFeatureCommand(@NotNull String mindmapId, @NotNull String userId, @NotNull Integer topicId, @NotNull String featureType) {
        super(mindmapId, userId);
        this.topicId = topicId;
        this.featureType = featureType;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "ADD_FEATURE";
    }

    @Override
    public void validate() {
        if (topicId == null) {
            throw new IllegalArgumentException("Topic ID cannot be null");
        }
        if (featureType == null || featureType.trim().isEmpty()) {
            throw new IllegalArgumentException("Feature type cannot be null or empty");
        }
        if (featureValue != null && featureValue.length() > 10000) {
            throw new IllegalArgumentException("Feature value cannot exceed 10000 characters");
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
    @JsonProperty("featureType")
    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(@NotNull String featureType) {
        this.featureType = featureType;
    }

    @Nullable
    @JsonProperty("featureValue")
    public String getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(@Nullable String featureValue) {
        this.featureValue = featureValue;
    }

    @Nullable
    @JsonProperty("featureId")
    public Integer getFeatureId() {
        return featureId;
    }

    public void setFeatureId(@Nullable Integer featureId) {
        this.featureId = featureId;
    }
}
