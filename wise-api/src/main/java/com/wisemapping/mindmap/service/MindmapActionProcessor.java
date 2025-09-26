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

package com.wisemapping.mindmap.service;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.rest.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Service responsible for processing STOMP actions and applying them to MapModel instances.
 * This class serves as the main entry point for applying frontend actions to backend mindmap models.
 * 
 * It takes a StompAction and a MapModel, validates the action, and applies the appropriate changes
 * to the mindmap model, ensuring data consistency and proper error handling.
 */
public class MindmapActionProcessor {
    
    private final MindmapOperationService operationService;
    
    public MindmapActionProcessor() {
        this.operationService = new MindmapOperationService();
    }
    
    /**
     * Processes a STOMP action and applies it to the given MapModel.
     * 
     * @param action The STOMP action to process
     * @param mapModel The mindmap model to modify
     * @return The result of the action processing
     * @throws MindmapActionProcessingException if the action cannot be processed
     */
    @NotNull
    public ActionProcessingResult processAction(@NotNull StompActions action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        
        try {
            // Validate the action before processing
            action.validate();
            
            // Process the action based on its type
            switch (action.getActionType()) {
                case "ADD_TOPIC":
                    return processAddTopicAction((AddTopicAction) action, mapModel);
                    
                case "DELETE":
                    return processDeleteAction((DeleteCommand) action, mapModel);
                    
                case "CHANGE_FEATURE":
                    return processChangeFeatureAction((ChangeFeatureCommand) action, mapModel);
                    
                case "ADD_FEATURE":
                    return processAddFeatureAction((AddFeatureAction) action, mapModel);
                    
                case "REMOVE_FEATURE":
                    return processRemoveFeatureAction((RemoveFeatureCommand) action, mapModel);
                    
                case "DRAG_TOPIC":
                    return processDragTopicAction((DragTopicAction) action, mapModel);
                    
                case "ADD_RELATIONSHIP":
                    return processAddRelationshipAction((AddRelationshipAction) action, mapModel);
                    
                case "MOVE_CONTROL_POINT":
                    return processMoveControlPointAction((MoveControlPointAction) action, mapModel);
                    
                case "GENERIC_FUNCTION":
                    return processGenericFunctionAction((GenericFunctionCommand) action, mapModel);
                    
                default:
                    throw new MindmapActionProcessingException("Unknown action type: " + action.getActionType());
            }
            
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to process action: " + action.getActionType(), e);
        }
    }
    
    /**
     * Processes an AddTopicAction.
     */
    @NotNull
    private ActionProcessingResult processAddTopicAction(@NotNull AddTopicAction action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            Topic newTopic = operationService.addTopic(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Topic added successfully", newTopic);
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to add topic", e);
        }
    }
    
    /**
     * Processes a DeleteAction.
     */
    @NotNull
    private ActionProcessingResult processDeleteAction(@NotNull DeleteCommand action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.deleteTopics(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Topics deleted successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to delete topics", e);
        }
    }
    
    /**
     * Processes a ChangeFeatureAction.
     */
    @NotNull
    private ActionProcessingResult processChangeFeatureAction(@NotNull ChangeFeatureCommand action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.changeFeature(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Feature changed successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to change feature", e);
        }
    }
    
    /**
     * Processes an AddFeatureAction.
     */
    @NotNull
    private ActionProcessingResult processAddFeatureAction(@NotNull AddFeatureAction action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.addFeature(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Feature added successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to add feature", e);
        }
    }
    
    /**
     * Processes a RemoveFeatureAction.
     */
    @NotNull
    private ActionProcessingResult processRemoveFeatureAction(@NotNull RemoveFeatureCommand action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.removeFeature(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Feature removed successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to remove feature", e);
        }
    }
    
    /**
     * Processes a DragTopicAction.
     */
    @NotNull
    private ActionProcessingResult processDragTopicAction(@NotNull DragTopicAction action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.dragTopic(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Topic dragged successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to drag topic", e);
        }
    }
    
    /**
     * Processes an AddRelationshipAction.
     */
    @NotNull
    private ActionProcessingResult processAddRelationshipAction(@NotNull AddRelationshipAction action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.addRelationship(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Relationship added successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to add relationship", e);
        }
    }
    
    /**
     * Processes a MoveControlPointAction.
     */
    @NotNull
    private ActionProcessingResult processMoveControlPointAction(@NotNull MoveControlPointAction action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.moveControlPoint(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Control point moved successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to move control point", e);
        }
    }
    
    /**
     * Processes a GenericFunctionAction.
     */
    @NotNull
    private ActionProcessingResult processGenericFunctionAction(@NotNull GenericFunctionCommand action, @NotNull MapModel mapModel) 
            throws MindmapActionProcessingException {
        try {
            operationService.executeGenericFunction(mapModel, action);
            return ActionProcessingResult.success(action.getActionType(), "Generic function executed successfully");
        } catch (Exception e) {
            throw new MindmapActionProcessingException("Failed to execute generic function", e);
        }
    }
    
    /**
     * Result class for action processing.
     */
    public static class ActionProcessingResult {
        private final boolean success;
        private final String actionType;
        private final String message;
        private final Object result;
        
        private ActionProcessingResult(boolean success, String actionType, String message, Object result) {
            this.success = success;
            this.actionType = actionType;
            this.message = message;
            this.result = result;
        }
        
        @NotNull
        public static ActionProcessingResult success(@NotNull String actionType, @NotNull String message) {
            return new ActionProcessingResult(true, actionType, message, null);
        }
        
        @NotNull
        public static ActionProcessingResult success(@NotNull String actionType, @NotNull String message, @Nullable Object result) {
            return new ActionProcessingResult(true, actionType, message, result);
        }
        
        @NotNull
        public static ActionProcessingResult failure(@NotNull String actionType, @NotNull String message) {
            return new ActionProcessingResult(false, actionType, message, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        @NotNull
        public String getActionType() {
            return actionType;
        }
        
        @NotNull
        public String getMessage() {
            return message;
        }
        
        @Nullable
        public Object getResult() {
            return result;
        }
    }
    
    /**
     * Exception class for action processing errors.
     */
    public static class MindmapActionProcessingException extends Exception {
        public MindmapActionProcessingException(String message) {
            super(message);
        }
        
        public MindmapActionProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
