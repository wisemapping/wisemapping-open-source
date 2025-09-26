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
import com.wisemapping.stomp.command.AddFeatureCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of the AddFeature action.
 * This action adds features (notes, links, etc.) to existing topics.
 * 
 * This class is immutable and stateless - it only contains the logic to modify the map.
 */
public class AddFeatureActionImpl implements Action<AddFeatureCommand> {
    
    @Override
    @NotNull
    public ActionResult doAction(@NotNull AddFeatureCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        
        // Validate the command
        validate(command);
        
        // Check if the action can be applied
        if (!canApply(command, mapModel)) {
            throw new ActionExecutionException("Cannot apply AddFeature action to this mindmap");
        }
        
        // Find the target topic
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new ActionExecutionException("Topic not found: " + command.getTopicId());
        }
        
        // Apply the feature based on type
        switch (command.getFeatureType().toLowerCase()) {
            case "note":
                if (command.getFeatureValue() != null) {
                    topic.setNote(command.getFeatureValue());
                }
                break;
            case "link":
                if (command.getFeatureValue() != null) {
                    topic.setLinkUrl(command.getFeatureValue());
                }
                break;
            default:
                throw new ActionExecutionException("Unsupported feature type: " + command.getFeatureType());
        }
        
        return ActionResult.success(getActionType(), 
            "Feature '" + command.getFeatureType() + "' added successfully", topic);
    }
    
    @Override
    public void update(@NotNull AddFeatureCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        // Update metadata to reflect the change
        mapModel.getMetadata().setLastModifiedDate(java.time.LocalDateTime.now());
        
        // Additional post-processing could be added here
        // For example: reindexing, validation, notifications, etc.
    }
    
    @Override
    public void validate(@NotNull AddFeatureCommand command) throws ActionExecutionException {
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new ActionExecutionException("User ID cannot be null or empty");
        }
        
        if (command.getTopicId() == null) {
            throw new ActionExecutionException("Topic ID cannot be null");
        }
        
        if (command.getFeatureType() == null || command.getFeatureType().trim().isEmpty()) {
            throw new ActionExecutionException("Feature type cannot be null or empty");
        }
        
        // Validate feature type
        String featureType = command.getFeatureType().toLowerCase();
        if (!isValidFeatureType(featureType)) {
            throw new ActionExecutionException("Invalid feature type: " + command.getFeatureType());
        }
        
        // Validate feature value based on type
        if (command.getFeatureValue() != null) {
            validateFeatureValue(featureType, command.getFeatureValue());
        }
    }
    
    @Override
    @NotNull
    public String getActionType() {
        return "ADD_FEATURE";
    }
    
    @Override
    public boolean canApply(@NotNull AddFeatureCommand command, @NotNull MapModel mapModel) {
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
        return featureType.equals("note") || featureType.equals("link");
    }
    
    /**
     * Validates a feature value based on its type.
     */
    private void validateFeatureValue(@NotNull String featureType, @NotNull String value) 
            throws ActionExecutionException {
        switch (featureType) {
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
