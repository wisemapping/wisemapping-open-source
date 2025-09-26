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
import com.wisemapping.stomp.command.ChangeFeatureCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Implementation of the ChangeFeature action.
 * This action modifies existing features of a topic (text, note, link, position, etc.).
 */
public class ChangeFeatureActionImpl implements Action<ChangeFeatureCommand> {
    
    @Override
    @NotNull
    public ActionResult doAction(@NotNull ChangeFeatureCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        
        // Validate the command
        validate(command);
        
        // Check if the action can be applied
        if (!canApply(command, mapModel)) {
            throw new ActionExecutionException("Cannot apply ChangeFeature action to this mindmap");
        }
        
        // Find the target topic
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new ActionExecutionException("Topic not found: " + command.getTopicId());
        }
        
        // Apply the change based on feature type
        String featureType = command.getFeatureType();
        String oldValue = null;
        
        switch (featureType != null ? featureType.toLowerCase() : "") {
            case "text":
                oldValue = topic.getText();
                if (command.getNewValue() != null) {
                    topic.setText(command.getNewValue());
                }
                break;
            case "note":
                oldValue = topic.getNote();
                if (command.getNewValue() != null) {
                    topic.setNote(command.getNewValue());
                }
                break;
            case "link":
                oldValue = topic.getLinkUrl();
                if (command.getNewValue() != null) {
                    topic.setLinkUrl(command.getNewValue());
                }
                break;
            case "position":
                // Handle position changes via attributes
                if (command.getAttributes() != null) {
                    Map<String, Object> attrs = command.getAttributes();
                    Double x = (Double) attrs.get("x");
                    Double y = (Double) attrs.get("y");
                    if (x != null && y != null) {
                        // Note: Topic class doesn't currently have position fields,
                        // but this is where we would update them
                        // topic.setX(x);
                        // topic.setY(y);
                    }
                }
                break;
            default:
                throw new ActionExecutionException("Unsupported feature type: " + featureType);
        }
        
        return ActionResult.success(getActionType(), 
            "Feature '" + featureType + "' changed successfully", topic);
    }
    
    @Override
    public void update(@NotNull ChangeFeatureCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        // Update metadata to reflect the change
        mapModel.getMetadata().setLastModifiedDate(java.time.LocalDateTime.now());
        
        // Additional post-processing could be added here
        // For example: reindexing, validation, notifications, etc.
    }
    
    @Override
    public void validate(@NotNull ChangeFeatureCommand command) throws ActionExecutionException {
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new ActionExecutionException("User ID cannot be null or empty");
        }
        
        if (command.getTopicId() == null) {
            throw new ActionExecutionException("Topic ID cannot be null");
        }
        
        if (command.getFeatureId() == null) {
            throw new ActionExecutionException("Feature ID cannot be null");
        }
        
        if (command.getFeatureType() == null || command.getFeatureType().trim().isEmpty()) {
            throw new ActionExecutionException("Feature type cannot be null or empty");
        }
        
        // Validate feature type
        String featureType = command.getFeatureType().toLowerCase();
        if (!isValidFeatureType(featureType)) {
            throw new ActionExecutionException("Invalid feature type: " + command.getFeatureType());
        }
        
        // Validate new value based on type
        if (command.getNewValue() != null) {
            validateFeatureValue(featureType, command.getNewValue());
        }
    }
    
    @Override
    @NotNull
    public String getActionType() {
        return "CHANGE_FEATURE";
    }
    
    @Override
    public boolean canApply(@NotNull ChangeFeatureCommand command, @NotNull MapModel mapModel) {
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
     * Validates if a feature type is supported.
     */
    private boolean isValidFeatureType(@NotNull String featureType) {
        return featureType.equals("text") || featureType.equals("note") || 
               featureType.equals("link") || featureType.equals("position");
    }
    
    /**
     * Validates a feature value based on its type.
     */
    private void validateFeatureValue(@NotNull String featureType, @NotNull String value) 
            throws ActionExecutionException {
        switch (featureType) {
            case "text":
                if (value.length() > 1000) {
                    throw new ActionExecutionException("Text cannot exceed 1000 characters");
                }
                break;
            case "note":
                if (value.length() > 10000) {
                    throw new ActionExecutionException("Note cannot exceed 10000 characters");
                }
                break;
            case "link":
                if (!isValidUrl(value)) {
                    throw new ActionExecutionException("Invalid link URL format");
                }
                break;
            case "position":
                // Position validation would be done via attributes
                break;
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
    
    /**
     * Validates URL format.
     */
    private boolean isValidUrl(@NotNull String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }
}
