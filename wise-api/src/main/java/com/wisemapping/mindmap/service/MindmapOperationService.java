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
import com.wisemapping.stomp.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Service for applying operations to mindmap models.
 * This service mirrors the frontend mindmap operations but works with the backend MapModel.
 */
public class MindmapOperationService {
    
    /**
     * Adds a new topic to the mindmap.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The add topic command
     * @return The created topic
     * @throws MindmapOperationException if the operation fails
     */
    @NotNull
    public Topic addTopic(@NotNull MapModel mapModel, @NotNull AddTopicCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        Topic newTopic = new Topic();
        newTopic.setId(generateTopicId());
        newTopic.setText(command.getText());
        newTopic.setNote(command.getNote());
        newTopic.setLinkUrl(command.getLinkUrl());
        
        if (command.getParentTopicId() != null) {
            Topic parentTopic = findTopicById(mapModel, command.getParentTopicId());
            if (parentTopic == null) {
                throw new MindmapOperationException("Parent topic not found: " + command.getParentTopicId());
            }
            parentTopic.addChild(newTopic);
        } else {
            // Add as root topic
            mapModel.addTopic(newTopic);
        }
        
        return newTopic;
    }
    
    /**
     * Adds a feature to an existing topic.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The add feature command
     * @throws MindmapOperationException if the operation fails
     */
    public void addFeature(@NotNull MapModel mapModel, @NotNull AddFeatureCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new MindmapOperationException("Topic not found: " + command.getTopicId());
        }
        
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
                throw new MindmapOperationException("Unsupported feature type: " + command.getFeatureType());
        }
    }
    
    /**
     * Changes a feature of an existing topic.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The change feature command
     * @throws MindmapOperationException if the operation fails
     */
    public void changeFeature(@NotNull MapModel mapModel, @NotNull ChangeFeatureCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new MindmapOperationException("Topic not found: " + command.getTopicId());
        }
        
        switch (command.getFeatureType().toLowerCase()) {
            case "text":
                topic.setText(command.getNewValue());
                break;
            case "note":
                topic.setNote(command.getNewValue());
                break;
            case "link":
                topic.setLinkUrl(command.getNewValue());
                break;
            default:
                throw new MindmapOperationException("Unsupported feature type for change: " + command.getFeatureType());
        }
    }
    
    /**
     * Removes a feature from a topic.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The remove feature command
     * @throws MindmapOperationException if the operation fails
     */
    public void removeFeature(@NotNull MapModel mapModel, @NotNull RemoveFeatureCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new MindmapOperationException("Topic not found: " + command.getTopicId());
        }
        
        switch (command.getFeatureType().toLowerCase()) {
            case "note":
                topic.setNote(null);
                break;
            case "link":
                topic.setLinkUrl(null);
                break;
            default:
                throw new MindmapOperationException("Unsupported feature type for removal: " + command.getFeatureType());
        }
    }
    
    /**
     * Deletes topics from the mindmap.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The delete command
     * @throws MindmapOperationException if the operation fails
     */
    public void deleteTopics(@NotNull MapModel mapModel, @NotNull DeleteCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        if (command.getTopicIds() != null) {
            for (Integer topicId : command.getTopicIds()) {
                Topic topic = findTopicById(mapModel, topicId);
                if (topic != null) {
                    removeTopicFromMindmap(mapModel, topic);
                }
            }
        }
    }
    
    /**
     * Drags a topic to a new position.
     * 
     * @param mapModel The mindmap model to modify
     * @param command The drag topic command
     * @throws MindmapOperationException if the operation fails
     */
    public void dragTopic(@NotNull MapModel mapModel, @NotNull DragTopicCommand command) throws MindmapOperationException {
        validateCommand(command);
        
        Topic topic = findTopicById(mapModel, command.getTopicId());
        if (topic == null) {
            throw new MindmapOperationException("Topic not found: " + command.getTopicId());
        }
        
        // For now, we'll just validate that the operation can be performed
        // In a real implementation, this would handle positioning logic
        // which would depend on the specific UI framework being used
    }
    
    /**
     * Finds a topic by its ID in the mindmap.
     * 
     * @param mapModel The mindmap model to search
     * @param topicId The topic ID to find
     * @return The topic if found, null otherwise
     */
    @Nullable
    private Topic findTopicById(@NotNull MapModel mapModel, @NotNull Integer topicId) {
        return findTopicById(mapModel, topicId.toString());
    }
    
    /**
     * Finds a topic by its ID in the mindmap.
     * 
     * @param mapModel The mindmap model to search
     * @param topicId The topic ID to find
     * @return The topic if found, null otherwise
     */
    @Nullable
    private Topic findTopicById(@NotNull MapModel mapModel, @NotNull String topicId) {
        List<Topic> allTopics = mapModel.getAllTopics();
        return allTopics.stream()
                .filter(topic -> topicId.equals(topic.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Removes a topic from the mindmap and all its children.
     * 
     * @param mapModel The mindmap model to modify
     * @param topicToRemove The topic to remove
     */
    private void removeTopicFromMindmap(@NotNull MapModel mapModel, @NotNull Topic topicToRemove) {
        // Remove from root topics
        mapModel.getTopics().remove(topicToRemove);
        
        // Remove from parent topics
        for (Topic rootTopic : mapModel.getTopics()) {
            removeTopicFromParent(rootTopic, topicToRemove);
        }
    }
    
    /**
     * Recursively removes a topic from its parent.
     * 
     * @param parent The parent topic to search
     * @param topicToRemove The topic to remove
     * @return true if the topic was found and removed
     */
    private boolean removeTopicFromParent(@NotNull Topic parent, @NotNull Topic topicToRemove) {
        if (parent.getChildren().remove(topicToRemove)) {
            return true;
        }
        
        for (Topic child : parent.getChildren()) {
            if (removeTopicFromParent(child, topicToRemove)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Generates a unique topic ID.
     * 
     * @return A unique topic ID
     */
    @NotNull
    private String generateTopicId() {
        return String.valueOf(System.currentTimeMillis() + (int)(Math.random() * 1000));
    }
    
    /**
     * Validates a command.
     * 
     * @param command The command to validate
     * @throws MindmapOperationException if validation fails
     */
    private void validateCommand(@NotNull StompCommand command) throws MindmapOperationException {
        try {
            command.validate();
        } catch (Exception e) {
            throw new MindmapOperationException("Command validation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exception thrown when mindmap operations fail.
     */
    public static class MindmapOperationException extends Exception {
        public MindmapOperationException(@NotNull String message) {
            super(message);
        }
        
        public MindmapOperationException(@NotNull String message, @NotNull Throwable cause) {
            super(message, cause);
        }
    }
}
