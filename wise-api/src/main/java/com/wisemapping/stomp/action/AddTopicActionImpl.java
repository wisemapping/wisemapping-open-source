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
import com.wisemapping.stomp.command.AddTopicCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of the AddTopic action.
 * This action adds a new topic to the mindmap, either as a child of an existing topic
 * or as a root-level topic.
 * 
 * This class is immutable and stateless - it only contains the logic to modify the map.
 */
public class AddTopicActionImpl implements Action<AddTopicCommand> {
    
    @Override
    @NotNull
    public ActionResult doAction(@NotNull AddTopicCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        
        // Validate the command
        validate(command);
        
        // Check if the action can be applied
        if (!canApply(command, mapModel)) {
            throw new ActionExecutionException("Cannot apply AddTopic action to this mindmap");
        }
        
        // Create the new topic
        Topic newTopic = new Topic();
        newTopic.setId(generateTopicId());
        newTopic.setText(command.getText());
        newTopic.setNote(command.getNote());
        newTopic.setLinkUrl(command.getLinkUrl());
        
        // Add the topic to the mindmap
        if (command.getParentTopicId() != null) {
            Topic parentTopic = findTopicById(mapModel, command.getParentTopicId());
            if (parentTopic == null) {
                throw new ActionExecutionException("Parent topic not found: " + command.getParentTopicId());
            }
            parentTopic.addChild(newTopic);
        } else {
            // Add as root topic
            mapModel.addTopic(newTopic);
        }
        
        return ActionResult.success(getActionType(), "Topic added successfully", newTopic);
    }
    
    @Override
    public void update(@NotNull AddTopicCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        // Update metadata to reflect the change
        mapModel.getMetadata().setLastModifiedDate(java.time.LocalDateTime.now());
        
        // Additional post-processing could be added here
        // For example: reindexing, validation, notifications, etc.
    }
    
    @Override
    public void validate(@NotNull AddTopicCommand command) throws ActionExecutionException {
        if (command.getText() == null || command.getText().trim().isEmpty()) {
            throw new ActionExecutionException("Topic text cannot be null or empty");
        }
        
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new ActionExecutionException("User ID cannot be null or empty");
        }
        
        // Validate text length
        if (command.getText().length() > 1000) {
            throw new ActionExecutionException("Topic text cannot exceed 1000 characters");
        }
        
        // Validate note length if present
        if (command.getNote() != null && command.getNote().length() > 10000) {
            throw new ActionExecutionException("Topic note cannot exceed 10000 characters");
        }
        
        // Validate link URL format if present
        if (command.getLinkUrl() != null && !isValidUrl(command.getLinkUrl())) {
            throw new ActionExecutionException("Invalid link URL format");
        }
    }
    
    @Override
    @NotNull
    public String getActionType() {
        return "ADD_TOPIC";
    }
    
    @Override
    public boolean canApply(@NotNull AddTopicCommand command, @NotNull MapModel mapModel) {
        try {
            validate(command);
            
            // Check if parent topic exists (if specified)
            if (command.getParentTopicId() != null) {
                Topic parentTopic = findTopicById(mapModel, command.getParentTopicId());
                return parentTopic != null;
            }
            
            return true;
        } catch (ActionExecutionException e) {
            return false;
        }
    }
    
    // Helper methods
    @NotNull
    private String generateTopicId() {
        // Generate a numeric ID for compatibility with current tests expecting Integer.parseInt
        return String.valueOf(System.currentTimeMillis() + (int)(Math.random() * 1000));
    }
    
    @NotNull
    private Topic findTopicById(@NotNull MapModel mapModel, @NotNull Integer id) {
        return findTopicById(mapModel.getTopics(), id);
    }
    
    @NotNull
    private Topic findTopicById(@NotNull java.util.List<Topic> topics, @NotNull Integer id) {
        for (Topic topic : topics) {
            if (topic.getId() != null && topic.getId().equals(id.toString())) {
                return topic;
            }
            Topic found = findTopicById(topic.getChildren(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    private boolean isValidUrl(@NotNull String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
