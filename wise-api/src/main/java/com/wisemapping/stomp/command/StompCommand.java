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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all STOMP commands that map to frontend mind map operations.
 * This provides a typed command system instead of untyped messages.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = AddTopicCommand.class, name = "ADD_TOPIC"),
        @JsonSubTypes.Type(value = DeleteCommand.class, name = "DELETE"),
        @JsonSubTypes.Type(value = ChangeFeatureCommand.class, name = "CHANGE_FEATURE"),
        @JsonSubTypes.Type(value = AddFeatureCommand.class, name = "ADD_FEATURE"),
        @JsonSubTypes.Type(value = RemoveFeatureCommand.class, name = "REMOVE_FEATURE"),
        @JsonSubTypes.Type(value = DragTopicCommand.class, name = "DRAG_TOPIC"),
        @JsonSubTypes.Type(value = AddRelationshipCommand.class, name = "ADD_RELATIONSHIP"),
        @JsonSubTypes.Type(value = MoveControlPointCommand.class, name = "MOVE_CONTROL_POINT"),
        @JsonSubTypes.Type(value = GenericFunctionCommand.class, name = "GENERIC_FUNCTION")
    })
public abstract class StompCommand {

    @NotNull
    private String mindmapId = "";

    @NotNull
    private String userId = "";

    @NotNull
    private String actionId = "";

    private long timestamp;

    @Nullable
    private String version;

    protected StompCommand() {
        this.actionId = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    protected StompCommand(@NotNull String mindmapId, @NotNull String userId) {
        this();
        this.mindmapId = mindmapId;
        this.userId = userId;
    }

    /**
     * Get the action type identifier.
     *
     * @return The action type
     */
    @NotNull
    public abstract String getActionType();

    /**
     * Validate the action parameters.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public abstract void validate();

    @NotNull
    @JsonProperty("mindmapId")
    public String getMindmapId() {
        return mindmapId;
    }

    public void setMindmapId(@NotNull String mindmapId) {
        this.mindmapId = mindmapId;
    }

    @NotNull
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) {
        this.userId = userId;
    }

    @NotNull
    @JsonProperty("actionId")
    public String getActionId() {
        return actionId;
    }

    public void setActionId(@NotNull String actionId) {
        this.actionId = actionId;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "StompCommand{" +
                "mindmapId='" + mindmapId + '\'' +
                ", userId='" + userId + '\'' +
                ", actionId='" + actionId + '\'' +
                ", timestamp=" + timestamp +
                ", version='" + version + '\'' +
                '}';
    }
}
