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

package com.wisemapping.service;

import com.wisemapping.rest.model.UserPresenceMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Service for managing STOMP WebSocket sessions and user presence in mind map editing sessions.
 * Tracks active users and manages real-time collaboration features.
 */
@Service
public class StompSessionService {

    private static final Logger logger = LogManager.getLogger(StompSessionService.class);

    private final SimpMessagingTemplate messagingTemplate;
    
    // Track active users per mind map
    private final Map<String, Set<String>> mindmapActiveUsers = new ConcurrentHashMap<>();
    
    // Track user session information
    private final Map<String, UserSessionInfo> userSessions = new ConcurrentHashMap<>();

    public StompSessionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle user joining a mind map editing session.
     *
     * @param mindmapId The ID of the mind map
     * @param userId The user ID
     * @param sessionId The WebSocket session ID
     */
    public void handleUserJoin(String mindmapId, String userId, String sessionId) {
        logger.debug("User {} joining mind map {}", userId, mindmapId);
        
        // Add user to active users set for this mind map
        mindmapActiveUsers.computeIfAbsent(mindmapId, k -> new CopyOnWriteArraySet<>()).add(userId);
        
        // Store session information
        userSessions.put(sessionId, new UserSessionInfo(userId, mindmapId));
        
        // Notify other users about the new user
        UserPresenceMessage presenceMessage = new UserPresenceMessage("join", mindmapId);
        presenceMessage.setUserId(userId);
        presenceMessage.setSessionId(sessionId);
        
        messagingTemplate.convertAndSend("/topic/mindmap/" + mindmapId + "/presence", presenceMessage);
    }

    /**
     * Handle user leaving a mind map editing session.
     *
     * @param mindmapId The ID of the mind map
     * @param userId The user ID
     * @param sessionId The WebSocket session ID
     */
    public void handleUserLeave(String mindmapId, String userId, String sessionId) {
        logger.debug("User {} leaving mind map {}", userId, mindmapId);
        
        // Remove user from active users set
        Set<String> activeUsers = mindmapActiveUsers.get(mindmapId);
        if (activeUsers != null) {
            activeUsers.remove(userId);
            if (activeUsers.isEmpty()) {
                mindmapActiveUsers.remove(mindmapId);
            }
        }
        
        // Remove session information
        userSessions.remove(sessionId);
        
        // Notify other users about the user leaving
        UserPresenceMessage presenceMessage = new UserPresenceMessage("leave", mindmapId);
        presenceMessage.setUserId(userId);
        presenceMessage.setSessionId(sessionId);
        
        messagingTemplate.convertAndSend("/topic/mindmap/" + mindmapId + "/presence", presenceMessage);
    }

    /**
     * Get active users for a specific mind map.
     *
     * @param mindmapId The ID of the mind map
     * @return Set of active user IDs
     */
    public Set<String> getActiveUsers(String mindmapId) {
        return mindmapActiveUsers.getOrDefault(mindmapId, new CopyOnWriteArraySet<>());
    }

    /**
     * Get user session information by session ID.
     *
     * @param sessionId The WebSocket session ID
     * @return User session information or null if not found
     */
    public UserSessionInfo getUserSession(String sessionId) {
        return userSessions.get(sessionId);
    }

    /**
     * Broadcast a message to all users viewing a specific mind map.
     *
     * @param mindmapId The ID of the mind map
     * @param destination The destination topic
     * @param message The message to broadcast
     */
    public void broadcastToMindmap(String mindmapId, String destination, Object message) {
        messagingTemplate.convertAndSend("/topic/mindmap/" + mindmapId + "/" + destination, message);
    }

    /**
     * Send a private message to a specific user.
     *
     * @param userId The user ID
     * @param destination The destination queue
     * @param message The message to send
     */
    public void sendToUser(String userId, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(userId, destination, message);
    }

    /**
     * Get all active users across all mind maps.
     *
     * @return Map of mind map ID to set of active user IDs
     */
    public Map<String, Set<String>> getActiveUsers() {
        return new HashMap<>(mindmapActiveUsers);
    }

    /**
     * Get all user sessions.
     *
     * @return Map of session ID to user session information
     */
    public Map<String, UserSessionInfo> getUserSessions() {
        return new HashMap<>(userSessions);
    }

    /**
     * Inner class to store user session information.
     */
    public static class UserSessionInfo {
        private final String userId;
        private final String mindmapId;
        private final long joinTime;

        public UserSessionInfo(String userId, String mindmapId) {
            this.userId = userId;
            this.mindmapId = mindmapId;
            this.joinTime = System.currentTimeMillis();
        }

        public String getUserId() {
            return userId;
        }

        public String getMindmapId() {
            return mindmapId;
        }

        public long getJoinTime() {
            return joinTime;
        }
    }
}
