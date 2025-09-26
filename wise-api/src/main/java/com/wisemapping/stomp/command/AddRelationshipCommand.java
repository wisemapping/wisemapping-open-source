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
 * Command for adding a relationship between two topics.
 * Maps to the frontend AddRelationshipCommand.
 */
public class AddRelationshipCommand extends StompCommand {

    @NotNull
    private Integer fromTopicId;

    @NotNull
    private Integer toTopicId;

    @Nullable
    private String relationshipType; // "arrow", "line", etc.

    @Nullable
    private String lineColor;

    @Nullable
    private Double lineWidth;

    @Nullable
    private String lineStyle; // "solid", "dashed", "dotted"

    public AddRelationshipCommand() {
        super();
    }

    public AddRelationshipCommand(@NotNull String mindmapId, @NotNull String userId, 
                                 @NotNull Integer fromTopicId, @NotNull Integer toTopicId) {
        super(mindmapId, userId);
        this.fromTopicId = fromTopicId;
        this.toTopicId = toTopicId;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "ADD_RELATIONSHIP";
    }

    @Override
    public void validate() {
        if (fromTopicId == null) {
            throw new IllegalArgumentException("From topic ID cannot be null");
        }
        if (toTopicId == null) {
            throw new IllegalArgumentException("To topic ID cannot be null");
        }
        if (fromTopicId.equals(toTopicId)) {
            throw new IllegalArgumentException("Cannot create relationship from topic to itself");
        }
    }

    @NotNull
    @JsonProperty("fromTopicId")
    public Integer getFromTopicId() {
        return fromTopicId;
    }

    public void setFromTopicId(@NotNull Integer fromTopicId) {
        this.fromTopicId = fromTopicId;
    }

    @NotNull
    @JsonProperty("toTopicId")
    public Integer getToTopicId() {
        return toTopicId;
    }

    public void setToTopicId(@NotNull Integer toTopicId) {
        this.toTopicId = toTopicId;
    }

    @Nullable
    @JsonProperty("relationshipType")
    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(@Nullable String relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Nullable
    @JsonProperty("lineColor")
    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(@Nullable String lineColor) {
        this.lineColor = lineColor;
    }

    @Nullable
    @JsonProperty("lineWidth")
    public Double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(@Nullable Double lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Nullable
    @JsonProperty("lineStyle")
    public String getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(@Nullable String lineStyle) {
        this.lineStyle = lineStyle;
    }
}
