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

package com.wisemapping.listener;

import com.wisemapping.service.StompSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * Event listener for STOMP WebSocket connection events.
 * Handles user connections, disconnections, and subscription events for real-time collaboration.
 */
@Component
public class StompEventListener {

    private static final Logger logger = LogManager.getLogger(StompEventListener.class);

    private final StompSessionService stompSessionService;

    public StompEventListener(StompSessionService stompSessionService) {
        this.stompSessionService = stompSessionService;
    }

    /**
     * Handle WebSocket connection events.
     *
     * @param event The connection event
     */
    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.debug("WebSocket session connected: {}", sessionId);
    }

    /**
     * Handle WebSocket disconnection events.
     *
     * @param event The disconnection event
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.debug("WebSocket session disconnected: {}", sessionId);
        
        // Get user session information and handle user leaving
        StompSessionService.UserSessionInfo sessionInfo = stompSessionService.getUserSession(sessionId);
        if (sessionInfo != null) {
            stompSessionService.handleUserLeave(sessionInfo.getMindmapId(), sessionInfo.getUserId(), sessionId);
        }
    }

    /**
     * Handle subscription events (when users subscribe to mind map topics).
     *
     * @param event The subscription event
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.debug("User subscribed to destination: {} (session: {})", destination, sessionId);
        
        // Extract mind map ID from destination and handle user joining
        if (destination != null && destination.contains("/topic/mindmap/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                String mindmapId = parts[3];
                
                // Get user information from the session
                String userId = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
                
                stompSessionService.handleUserJoin(mindmapId, userId, sessionId);
            }
        }
    }

    /**
     * Handle unsubscription events (when users unsubscribe from mind map topics).
     *
     * @param event The unsubscription event
     */
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.debug("User unsubscribed from destination: {} (session: {})", destination, sessionId);
        
        // Handle user leaving if they're unsubscribing from a mind map topic
        if (destination != null && destination.contains("/topic/mindmap/")) {
            StompSessionService.UserSessionInfo sessionInfo = stompSessionService.getUserSession(sessionId);
            if (sessionInfo != null) {
                stompSessionService.handleUserLeave(sessionInfo.getMindmapId(), sessionInfo.getUserId(), sessionId);
            }
        }
    }
}
