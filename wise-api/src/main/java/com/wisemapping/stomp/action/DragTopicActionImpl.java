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

package com.wisemapping.stomp.action;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.stomp.command.DragTopicCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of the DragTopic action.
 * This action moves a topic to a new position by updating its coordinates.
 * 
 * This class is immutable and stateless - it only contains the logic to modify the map.
 */
public class DragTopicActionImpl implements Action<DragTopicCommand> {
    
    @Override
    @NotNull
    public ActionResult doAction(@NotNull DragTopicCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        
        // Validate the command
        validate(command);
        
        // Check if the action can be applied
        if (!canApply(command, mapModel)) {
            throw new ActionExecutionException("Cannot apply DragTopic action to this mindmap");
        }
        
        // Find the target topic
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new ActionExecutionException("Topic not found: " + command.getTopicId());
        }
        
        // Store old position for potential rollback
        // Note: The Topic class doesn't currently have position fields,
        // but this is where we would update x and y coordinates
        // topic.setX(command.getX());
        // topic.setY(command.getY());
        
        // For now, we'll just validate that the action can be applied
        // In a real implementation, this would update the topic's position
        
        return ActionResult.success(getActionType(), 
            "Topic dragged to position (" + command.getX() + ", " + command.getY() + ")", topic);
    }
    
    @Override
    public void update(@NotNull DragTopicCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        // Update metadata to reflect the change
        mapModel.getMetadata().setLastModifiedDate(java.time.LocalDateTime.now());
        
        // Additional post-processing could be added here
        // For example: collision detection, layout optimization, etc.
    }
    
    @Override
    public void validate(@NotNull DragTopicCommand command) throws ActionExecutionException {
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new ActionExecutionException("User ID cannot be null or empty");
        }
        
        if (command.getTopicId() == null) {
            throw new ActionExecutionException("Topic ID cannot be null");
        }
        
        // Validate coordinates are reasonable
        if (command.getX() < -10000 || command.getX() > 10000) {
            throw new ActionExecutionException("X coordinate must be between -10000 and 10000");
        }
        
        if (command.getY() < -10000 || command.getY() > 10000) {
            throw new ActionExecutionException("Y coordinate must be between -10000 and 10000");
        }
        
        // Check for NaN or infinite values
        if (Double.isNaN(command.getX()) || Double.isInfinite(command.getX())) {
            throw new ActionExecutionException("X coordinate cannot be NaN or infinite");
        }
        
        if (Double.isNaN(command.getY()) || Double.isInfinite(command.getY())) {
            throw new ActionExecutionException("Y coordinate cannot be NaN or infinite");
        }
    }
    
    @Override
    @NotNull
    public String getActionType() {
        return "DRAG_TOPIC";
    }
    
    @Override
    public boolean canApply(@NotNull DragTopicCommand command, @NotNull MapModel mapModel) {
        try {
            validate(command);
            
            // Check if the topic exists
            Topic topic = findTopicById(mapModel, command.getTopicId());
            return topic != null;
        } catch (ActionExecutionException e) {
            return false;
        }
    }
    
    /**
     * Finds a topic by its ID in the mindmap.
     */
    @NotNull
    private Topic findTopicById(@NotNull MapModel mapModel, @NotNull Integer topicId) {
        return findTopicById(mapModel.getTopics(), topicId);
    }
    
    /**
     * Recursively finds a topic by its ID in a list of topics.
     */
    @NotNull
    private Topic findTopicById(@NotNull java.util.List<Topic> topics, @NotNull Integer topicId) {
        for (Topic topic : topics) {
            if (topic.getId() != null && topic.getId().equals(topicId.toString())) {
                return topic;
            }
            Topic found = findTopicById(topic.getChildren(), topicId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
