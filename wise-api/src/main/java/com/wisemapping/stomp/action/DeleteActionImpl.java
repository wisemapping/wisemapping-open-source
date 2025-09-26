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
import com.wisemapping.stomp.command.DeleteCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Delete action.
 * This action removes one or more topics from the mindmap.
 */
public class DeleteActionImpl implements Action<DeleteCommand> {
    
    @Override
    @NotNull
    public ActionResult doAction(@NotNull DeleteCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        
        // Validate the command
        validate(command);
        
        // Check if the action can be applied
        if (!canApply(command, mapModel)) {
            throw new ActionExecutionException("Cannot apply Delete action to this mindmap");
        }
        
        List<Topic> deletedTopics = new ArrayList<>();
        
        // Delete each specified topic
        if (command.getTopicIds() != null) {
            for (Integer topicId : command.getTopicIds()) {
                Topic deletedTopic = deleteTopicById(mapModel, topicId);
                if (deletedTopic != null) {
                    deletedTopics.add(deletedTopic);
                }
            }
        }
        
        return ActionResult.success(getActionType(), 
            "Deleted " + deletedTopics.size() + " topic(s) successfully", deletedTopics);
    }
    
    @Override
    public void update(@NotNull DeleteCommand command, @NotNull MapModel mapModel) 
            throws ActionExecutionException {
        // Update metadata to reflect the change
        mapModel.getMetadata().setLastModifiedDate(java.time.LocalDateTime.now());
        
        // Additional post-processing could be added here
        // For example: cleanup orphaned relationships, reindexing, etc.
    }
    
    @Override
    public void validate(@NotNull DeleteCommand command) throws ActionExecutionException {
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new ActionExecutionException("User ID cannot be null or empty");
        }
        
        if (command.getTopicIds() == null || command.getTopicIds().isEmpty()) {
            throw new ActionExecutionException("Topic IDs list cannot be null or empty");
        }
        
        // Validate that we don't try to delete too many topics at once
        if (command.getTopicIds().size() > 100) {
            throw new ActionExecutionException("Cannot delete more than 100 topics at once");
        }
        
        // Validate topic IDs
        for (Integer topicId : command.getTopicIds()) {
            if (topicId == null || topicId <= 0) {
                throw new ActionExecutionException("Invalid topic ID: " + topicId);
            }
        }
    }
    
    @Override
    @NotNull
    public String getActionType() {
        return "DELETE";
    }
    
    @Override
    public boolean canApply(@NotNull DeleteCommand command, @NotNull MapModel mapModel) {
        try {
            validate(command);
            
            // Check if at least one topic exists
            if (command.getTopicIds() != null) {
                for (Integer topicId : command.getTopicIds()) {
                    if (findTopicById(mapModel, topicId) != null) {
                        return true; // At least one topic exists
                    }
                }
            }
            
            return false; // No topics found
        } catch (ActionExecutionException e) {
            return false;
        }
    }
    
    /**
     * Deletes a topic by its ID from the mindmap.
     * 
     * @param mapModel The mindmap model
     * @param topicId The ID of the topic to delete
     * @return The deleted topic, or null if not found
     */
    @NotNull
    private Topic deleteTopicById(@NotNull MapModel mapModel, @NotNull Integer topicId) {
        // First try to delete from root topics
        Topic deleted = deleteFromTopicList(mapModel.getTopics(), topicId);
        if (deleted != null) {
            return deleted;
        }
        
        // If not found in root topics, search recursively in all topics
        for (Topic topic : mapModel.getTopics()) {
            deleted = deleteFromChildren(topic, topicId);
            if (deleted != null) {
                return deleted;
            }
        }
        
        return null; // Topic not found
    }
    
    /**
     * Deletes a topic from a list of topics.
     */
    @NotNull
    private Topic deleteFromTopicList(@NotNull java.util.List<Topic> topics, @NotNull Integer topicId) {
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            if (topic.getId() != null && topic.getId().equals(topicId.toString())) {
                return topics.remove(i);
            }
        }
        return null;
    }
    
    /**
     * Recursively deletes a topic from children of a parent topic.
     */
    @NotNull
    private Topic deleteFromChildren(@NotNull Topic parentTopic, @NotNull Integer topicId) {
        for (int i = 0; i < parentTopic.getChildren().size(); i++) {
            Topic child = parentTopic.getChildren().get(i);
            if (child.getId() != null && child.getId().equals(topicId.toString())) {
                return parentTopic.getChildren().remove(i);
            }
            
            // Recursively search in children
            Topic deleted = deleteFromChildren(child, topicId);
            if (deleted != null) {
                return deleted;
            }
        }
        return null;
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
