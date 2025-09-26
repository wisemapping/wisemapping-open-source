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

package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Message model for mind map updates sent via STOMP WebSocket.
 * Contains information about changes made to a mind map for real-time collaboration.
 */
public class MindmapUpdateMessage {

    @Nullable
    private String userId;

    @Nullable
    private String mindmapId;

    @Nullable
    private String operation; // "create", "update", "delete", "move", "resize"

    @Nullable
    private String elementId; // ID of the topic, relationship, or other element

    @Nullable
    private String elementType; // "topic", "relationship", "icon", "link"

    @Nullable
    private String data; // JSON data containing the changes

    private long timestamp;

    @Nullable
    private String version; // Mind map version for conflict resolution

    public MindmapUpdateMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public MindmapUpdateMessage(@Nullable String operation, @Nullable String elementId, @Nullable String elementType, @Nullable String data) {
        this.operation = operation;
        this.elementId = elementId;
        this.elementType = elementType;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    @Nullable
    @JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(@Nullable String userId) {
        this.userId = userId;
    }

    @Nullable
    @JsonProperty("mindmapId")
    public String getMindmapId() {
        return mindmapId;
    }

    public void setMindmapId(@Nullable String mindmapId) {
        this.mindmapId = mindmapId;
    }

    @Nullable
    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    public void setOperation(@Nullable String operation) {
        this.operation = operation;
    }

    @Nullable
    @JsonProperty("elementId")
    public String getElementId() {
        return elementId;
    }

    public void setElementId(@Nullable String elementId) {
        this.elementId = elementId;
    }

    @Nullable
    @JsonProperty("elementType")
    public String getElementType() {
        return elementType;
    }

    public void setElementType(@Nullable String elementType) {
        this.elementType = elementType;
    }

    @Nullable
    @JsonProperty("data")
    public String getData() {
        return data;
    }

    public void setData(@Nullable String data) {
        this.data = data;
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
        return "MindmapUpdateMessage{" +
                "userId='" + userId + '\'' +
                ", mindmapId='" + mindmapId + '\'' +
                ", operation='" + operation + '\'' +
                ", elementId='" + elementId + '\'' +
                ", elementType='" + elementType + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + timestamp +
                ", version='" + version + '\'' +
                '}';
    }
}
