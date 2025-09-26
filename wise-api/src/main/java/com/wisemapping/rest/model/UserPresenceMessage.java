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
 * Message model for user presence updates sent via STOMP WebSocket.
 * Tracks when users join or leave a mind map editing session.
 */
public class UserPresenceMessage {

    @Nullable
    private String userId;

    @Nullable
    private String action; // "join", "leave", "active", "idle"

    @Nullable
    private String mindmapId;

    @Nullable
    private String userDisplayName;

    @Nullable
    private String userColor; // Color assigned to the user for visual identification

    private long timestamp;

    @Nullable
    private String sessionId; // WebSocket session ID

    public UserPresenceMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public UserPresenceMessage(@Nullable String action, @Nullable String mindmapId) {
        this.action = action;
        this.mindmapId = mindmapId;
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
    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    public void setAction(@Nullable String action) {
        this.action = action;
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
    @JsonProperty("userDisplayName")
    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(@Nullable String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    @Nullable
    @JsonProperty("userColor")
    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(@Nullable String userColor) {
        this.userColor = userColor;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    @JsonProperty("sessionId")
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(@Nullable String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "UserPresenceMessage{" +
                "userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", mindmapId='" + mindmapId + '\'' +
                ", userDisplayName='" + userDisplayName + '\'' +
                ", userColor='" + userColor + '\'' +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
