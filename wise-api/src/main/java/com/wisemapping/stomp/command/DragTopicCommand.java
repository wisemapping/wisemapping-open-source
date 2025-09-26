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
 * Command for dragging/moving topics to new positions.
 * Maps to the frontend DragTopicCommand.
 */
public class DragTopicCommand extends StompCommand {

    @NotNull
    private Integer topicId;

    @NotNull
    private Double newPositionX;

    @NotNull
    private Double newPositionY;

    @Nullable
    private Double oldPositionX;

    @Nullable
    private Double oldPositionY;

    public DragTopicCommand() {
        super();
    }

    public DragTopicCommand(@NotNull String mindmapId, @NotNull String userId, @NotNull Integer topicId, 
                           @NotNull Double newPositionX, @NotNull Double newPositionY) {
        super(mindmapId, userId);
        this.topicId = topicId;
        this.newPositionX = newPositionX;
        this.newPositionY = newPositionY;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "DRAG_TOPIC";
    }

    @Override
    public void validate() {
        if (topicId == null) {
            throw new IllegalArgumentException("Topic ID cannot be null");
        }
        if (newPositionX == null || Double.isNaN(newPositionX) || Double.isInfinite(newPositionX)) {
            throw new IllegalArgumentException("Invalid new position X");
        }
        if (newPositionY == null || Double.isNaN(newPositionY) || Double.isInfinite(newPositionY)) {
            throw new IllegalArgumentException("Invalid new position Y");
        }
        if (newPositionX < -10000 || newPositionX > 10000) {
            throw new IllegalArgumentException("New position X must be between -10000 and 10000");
        }
        if (newPositionY < -10000 || newPositionY > 10000) {
            throw new IllegalArgumentException("New position Y must be between -10000 and 10000");
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
    @JsonProperty("newPositionX")
    public Double getNewPositionX() {
        return newPositionX;
    }

    public void setNewPositionX(@NotNull Double newPositionX) {
        this.newPositionX = newPositionX;
    }

    @NotNull
    @JsonProperty("newPositionY")
    public Double getNewPositionY() {
        return newPositionY;
    }

    public void setNewPositionY(@NotNull Double newPositionY) {
        this.newPositionY = newPositionY;
    }

    @Nullable
    @JsonProperty("oldPositionX")
    public Double getOldPositionX() {
        return oldPositionX;
    }

    public void setOldPositionX(@Nullable Double oldPositionX) {
        this.oldPositionX = oldPositionX;
    }

    @Nullable
    @JsonProperty("oldPositionY")
    public Double getOldPositionY() {
        return oldPositionY;
    }

    public void setOldPositionY(@Nullable Double oldPositionY) {
        this.oldPositionY = oldPositionY;
    }

    // Convenience methods for backward compatibility
    @NotNull
    public Double getX() {
        return newPositionX;
    }

    public void setX(@NotNull Double x) {
        this.newPositionX = x;
    }

    @NotNull
    public Double getY() {
        return newPositionY;
    }

    public void setY(@NotNull Double y) {
        this.newPositionY = y;
    }
}
