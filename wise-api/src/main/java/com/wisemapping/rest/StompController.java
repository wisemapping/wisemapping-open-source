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

import com.wisemapping.rest.model.*;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * STOMP message controller for real-time mind map collaboration.
 * Handles WebSocket messages for collaborative editing, user presence,
 * and real-time updates.
 */
@Controller
public class StompController {

    private static final Logger logger = LogManager.getLogger(StompController.class);

    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private MindmapService mindmapService;

    public StompController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle typed commands from clients.
     * Processes commands and broadcasts them to all users viewing the same mind map.
     *
     * @param mindmapId The ID of the mind map being updated
     * @param command The typed command containing the operation details
     * @param headerAccessor Access to message headers and session info
     * @return The processed command
     */
    @MessageMapping("/mindmap/{mindmapId}/command")
    @SendTo("/topic/mindmap/{mindmapId}/commands")
    public StompCommand handleMindmapCommand(
            @DestinationVariable String mindmapId,
            @Payload StompCommand command,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // Get user information from security context
        Authentication auth = (Authentication) headerAccessor.getUser();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Set the user who sent the command
        command.setUserId(username);
        command.setMindmapId(mindmapId);
        command.setTimestamp(System.currentTimeMillis());
        
        logger.debug("Mind map command from user {} for mindmap {}: {}", username, mindmapId, command.getCommandType());
        
        try {
            // Validate the command
            command.validate();
            
            // Process the command based on its type
            processCommand(command, mindmapId, username);
            
        } catch (Exception e) {
            logger.error("Error processing command {} from user {}: {}", command.getCommandType(), username, e.getMessage());
            // You could send an error message back to the user here
        }
        
        return command;
    }

    /**
     * Handle user presence updates (user joining/leaving a mind map session).
     *
     * @param mindmapId The ID of the mind map
     * @param message The presence message
     * @param headerAccessor Access to message headers
     * @return The processed presence message
     */
    @MessageMapping("/mindmap/{mindmapId}/presence")
    @SendTo("/topic/mindmap/{mindmapId}/presence")
    public UserPresenceMessage handleUserPresence(
            @DestinationVariable String mindmapId,
            @Payload UserPresenceMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Authentication auth = (Authentication) headerAccessor.getUser();
        String username = auth != null ? auth.getName() : "anonymous";
        
        message.setUserId(username);
        message.setTimestamp(System.currentTimeMillis());
        
        logger.debug("User presence update from user {} for mindmap {}", username, mindmapId);
        
        return message;
    }

    /**
     * Handle cursor position updates for collaborative editing.
     *
     * @param mindmapId The ID of the mind map
     * @param cursorData The cursor position data
     * @param headerAccessor Access to message headers
     */
    @MessageMapping("/mindmap/{mindmapId}/cursor")
    public void handleCursorUpdate(
            @DestinationVariable String mindmapId,
            @Payload String cursorData,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Authentication auth = (Authentication) headerAccessor.getUser();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Create cursor message with user info
        String cursorMessage = String.format("{\"userId\":\"%s\",\"cursor\":%s,\"timestamp\":%d}", 
                username, cursorData, System.currentTimeMillis());
        
        // Send to all users viewing this mind map except the sender
        messagingTemplate.convertAndSend("/topic/mindmap/" + mindmapId + "/cursor", cursorMessage);
    }

    /**
     * Handle private messages between users (for chat functionality).
     *
     * @param message The private message
     * @param headerAccessor Access to message headers
     */
    @MessageMapping("/private")
    @SendToUser("/queue/private")
    public String handlePrivateMessage(
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Authentication auth = (Authentication) headerAccessor.getUser();
        String username = auth != null ? auth.getName() : "anonymous";
        
        logger.debug("Private message from user {}", username);
        
        return String.format("{\"from\":\"%s\",\"message\":%s,\"timestamp\":%d}", 
                username, message, System.currentTimeMillis());
    }

    /**
     * Handle typing indicators for collaborative editing.
     *
     * @param mindmapId The ID of the mind map
     * @param typingData The typing indicator data
     * @param headerAccessor Access to message headers
     */
    @MessageMapping("/mindmap/{mindmapId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable String mindmapId,
            @Payload String typingData,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Authentication auth = (Authentication) headerAccessor.getUser();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Create typing message with user info
        String typingMessage = String.format("{\"userId\":\"%s\",\"typing\":%s,\"timestamp\":%d}", 
                username, typingData, System.currentTimeMillis());
        
        // Send to all users viewing this mind map except the sender
        messagingTemplate.convertAndSend("/topic/mindmap/" + mindmapId + "/typing", typingMessage);
    }

    /**
     * Process the command based on its type.
     * This method delegates to specific command handlers.
     *
     * @param command The command to process
     * @param mindmapId The mind map ID
     * @param username The username
     */
    private void processCommand(StompCommand command, String mindmapId, String username) {
        switch (command.getCommandType()) {
            case "ADD_TOPIC":
                processAddTopicCommand((AddTopicCommand) command, mindmapId, username);
                break;
            case "DELETE":
                processDeleteCommand((DeleteCommand) command, mindmapId, username);
                break;
            case "CHANGE_FEATURE":
                processChangeFeatureCommand((ChangeFeatureCommand) command, mindmapId, username);
                break;
            case "ADD_FEATURE":
                processAddFeatureCommand((AddFeatureCommand) command, mindmapId, username);
                break;
            case "REMOVE_FEATURE":
                processRemoveFeatureCommand((RemoveFeatureCommand) command, mindmapId, username);
                break;
            case "DRAG_TOPIC":
                processDragTopicCommand((DragTopicCommand) command, mindmapId, username);
                break;
            case "ADD_RELATIONSHIP":
                processAddRelationshipCommand((AddRelationshipCommand) command, mindmapId, username);
                break;
            case "MOVE_CONTROL_POINT":
                processMoveControlPointCommand((MoveControlPointCommand) command, mindmapId, username);
                break;
            case "GENERIC_FUNCTION":
                processGenericFunctionCommand((GenericFunctionCommand) command, mindmapId, username);
                break;
            default:
                logger.warn("Unknown command type: {}", command.getCommandType());
        }
    }

    private void processAddTopicCommand(AddTopicCommand command, String mindmapId, String username) {
        // TODO: Implement add topic logic
        logger.debug("Processing ADD_TOPIC command for topic: {}", command.getText());
    }

    private void processDeleteCommand(DeleteCommand command, String mindmapId, String username) {
        // TODO: Implement delete logic
        logger.debug("Processing DELETE command for entities: {}", command.getTopicIds());
    }

    private void processChangeFeatureCommand(ChangeFeatureCommand command, String mindmapId, String username) {
        // TODO: Implement change feature logic
        logger.debug("Processing CHANGE_FEATURE command for topic {}: {}", command.getTopicId(), command.getFeatureType());
    }

    private void processAddFeatureCommand(AddFeatureCommand command, String mindmapId, String username) {
        // TODO: Implement add feature logic
        logger.debug("Processing ADD_FEATURE command for topic {}: {}", command.getTopicId(), command.getFeatureType());
    }

    private void processRemoveFeatureCommand(RemoveFeatureCommand command, String mindmapId, String username) {
        // TODO: Implement remove feature logic
        logger.debug("Processing REMOVE_FEATURE command for topic {}: {}", command.getTopicId(), command.getFeatureId());
    }

    private void processDragTopicCommand(DragTopicCommand command, String mindmapId, String username) {
        // TODO: Implement drag topic logic
        logger.debug("Processing DRAG_TOPIC command for topic {}: ({}, {})", command.getTopicId(), command.getNewPositionX(), command.getNewPositionY());
    }

    private void processAddRelationshipCommand(AddRelationshipCommand command, String mindmapId, String username) {
        // TODO: Implement add relationship logic
        logger.debug("Processing ADD_RELATIONSHIP command from {} to {}", command.getFromTopicId(), command.getToTopicId());
    }

    private void processMoveControlPointCommand(MoveControlPointCommand command, String mindmapId, String username) {
        // TODO: Implement move control point logic
        logger.debug("Processing MOVE_CONTROL_POINT command for relationship {}: ({}, {})", command.getRelationshipId(), command.getNewPositionX(), command.getNewPositionY());
    }

    private void processGenericFunctionCommand(GenericFunctionCommand command, String mindmapId, String username) {
        // TODO: Implement generic function logic
        logger.debug("Processing GENERIC_FUNCTION command: {}", command.getFunctionName());
    }
}
