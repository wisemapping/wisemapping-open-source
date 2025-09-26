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

package com.wisemapping.rest;

import com.wisemapping.service.StompSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for STOMP WebSocket session information and management.
 * Provides endpoints to query active users and session information.
 */
@RestController
@RequestMapping("/api/stomp")
public class StompInfoController {

    private final StompSessionService stompSessionService;

    @Autowired
    public StompInfoController(StompSessionService stompSessionService) {
        this.stompSessionService = stompSessionService;
    }

    /**
     * Get active users for a specific mind map.
     *
     * @param mindmapId The ID of the mind map
     * @return List of active user IDs
     */
    @GetMapping("/mindmap/{mindmapId}/active-users")
    @PreAuthorize("hasPermission(#mindmapId, 'mindmap', 'read')")
    public ResponseEntity<Map<String, Object>> getActiveUsers(@PathVariable String mindmapId) {
        Set<String> activeUsers = stompSessionService.getActiveUsers(mindmapId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mindmapId", mindmapId);
        response.put("activeUsers", activeUsers);
        response.put("userCount", activeUsers.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get WebSocket connection information.
     *
     * @return Connection information including endpoints and configuration
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("websocketEndpoint", "/ws");
        info.put("websocketEndpointNative", "/ws-native");
        info.put("messageBrokerTopics", new String[]{
            "/topic/mindmap/{mindmapId}/updates",
            "/topic/mindmap/{mindmapId}/presence",
            "/topic/mindmap/{mindmapId}/cursor",
            "/topic/mindmap/{mindmapId}/typing"
        });
        info.put("messageBrokerQueues", new String[]{
            "/user/queue/private"
        });
        info.put("applicationDestinations", new String[]{
            "/app/mindmap/{mindmapId}/update",
            "/app/mindmap/{mindmapId}/presence",
            "/app/mindmap/{mindmapId}/cursor",
            "/app/mindmap/{mindmapId}/typing",
            "/app/private"
        });
        info.put("sockjsEnabled", true);
        info.put("corsEnabled", true);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Get session statistics.
     *
     * @return Statistics about active sessions
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSessionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count total active sessions across all mind maps
        int totalActiveUsers = stompSessionService.getActiveUsers().values().stream()
            .mapToInt(Set::size)
            .sum();
        
        stats.put("totalActiveUsers", totalActiveUsers);
        stats.put("activeMindmaps", stompSessionService.getActiveUsers().size());
        stats.put("totalSessions", stompSessionService.getUserSessions().size());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Send a test message to a specific mind map topic.
     * This is useful for testing the STOMP integration.
     *
     * @param mindmapId The ID of the mind map
     * @param message The test message
     * @return Success response
     */
    @PostMapping("/mindmap/{mindmapId}/test-message")
    @PreAuthorize("hasPermission(#mindmapId, 'mindmap', 'write')")
    public ResponseEntity<Map<String, Object>> sendTestMessage(
            @PathVariable String mindmapId,
            @RequestBody Map<String, Object> message) {
        
        stompSessionService.broadcastToMindmap(mindmapId, "updates", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mindmapId", mindmapId);
        response.put("message", "Test message sent successfully");
        
        return ResponseEntity.ok(response);
    }
}
