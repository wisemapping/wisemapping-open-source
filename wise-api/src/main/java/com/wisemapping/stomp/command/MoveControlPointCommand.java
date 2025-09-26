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
 * Command for moving control points of relationships.
 * Maps to the frontend MoveControlPointCommand.
 */
public class MoveControlPointCommand extends StompCommand {

    @NotNull
    private Integer relationshipId;

    @NotNull
    private Integer controlPointId;

    @NotNull
    private Double newPositionX;

    @NotNull
    private Double newPositionY;

    @Nullable
    private Double oldPositionX;

    @Nullable
    private Double oldPositionY;

    public MoveControlPointCommand() {
        super();
    }

    public MoveControlPointCommand(@NotNull String mindmapId, @NotNull String userId, 
                                 @NotNull Integer relationshipId, @NotNull Integer controlPointId,
                                 @NotNull Double newPositionX, @NotNull Double newPositionY) {
        super(mindmapId, userId);
        this.relationshipId = relationshipId;
        this.controlPointId = controlPointId;
        this.newPositionX = newPositionX;
        this.newPositionY = newPositionY;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "MOVE_CONTROL_POINT";
    }

    @Override
    public void validate() {
        if (relationshipId == null) {
            throw new IllegalArgumentException("Relationship ID cannot be null");
        }
        if (controlPointId == null) {
            throw new IllegalArgumentException("Control point ID cannot be null");
        }
        if (newPositionX == null || Double.isNaN(newPositionX) || Double.isInfinite(newPositionX)) {
            throw new IllegalArgumentException("Invalid new position X");
        }
        if (newPositionY == null || Double.isNaN(newPositionY) || Double.isInfinite(newPositionY)) {
            throw new IllegalArgumentException("Invalid new position Y");
        }
    }

    @NotNull
    @JsonProperty("relationshipId")
    public Integer getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(@NotNull Integer relationshipId) {
        this.relationshipId = relationshipId;
    }

    @NotNull
    @JsonProperty("controlPointId")
    public Integer getControlPointId() {
        return controlPointId;
    }

    public void setControlPointId(@NotNull Integer controlPointId) {
        this.controlPointId = controlPointId;
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
}
