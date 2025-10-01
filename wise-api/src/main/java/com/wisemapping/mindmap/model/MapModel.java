/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.mindmap.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a mindmap as a pure domain model.
 * This model focuses on the business concepts of a mindmap without XML implementation details.
 */
public class MapModel implements Serializable {
    
    @Nullable
    private String title;
    
    @Nullable
    private String description;
    
    @NotNull
    private List<Topic> topics = new ArrayList<>();
    
    @NotNull
    private MapMetadata metadata = new MapMetadata();
    
    public MapModel() {
    }
    
    public MapModel(@NotNull String title) {
        this.title = title;
    }
    
    @Nullable
    public String getTitle() {
        return title;
    }
    
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    
    @Nullable
    public String getDescription() {
        return description;
    }
    
    public void setDescription(@Nullable String description) {
        this.description = description;
    }
    
    @NotNull
    public List<Topic> getTopics() {
        return topics;
    }
    
    public void setTopics(@NotNull List<Topic> topics) {
        this.topics = topics;
    }
    
    @NotNull
    public MapMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(@NotNull MapMetadata metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Adds a topic to the mindmap.
     * 
     * @param topic The topic to add
     */
    public void addTopic(@NotNull Topic topic) {
        this.topics.add(topic);
    }
    
    /**
     * Gets the central topic (the main topic of the mindmap).
     * 
     * @return The central topic, or null if not found
     */
    @Nullable
    public Topic getCentralTopic() {
        return topics.stream()
                .filter(Topic::isCentral)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets all topics including nested ones.
     * 
     * @return List of all topics in the mindmap
     */
    @NotNull
    public List<Topic> getAllTopics() {
        List<Topic> allTopics = new ArrayList<>();
        for (Topic topic : topics) {
            allTopics.add(topic);
            allTopics.addAll(topic.getAllChildTopics());
        }
        return allTopics;
    }
    
    /**
     * Gets the total number of topics in the mindmap.
     * 
     * @return Total topic count
     */
    public int getTotalTopicCount() {
        return getAllTopics().size();
    }
    
    /**
     * Gets all text content from the mindmap.
     * 
     * @return List of all text content
     */
    @NotNull
    public List<String> getAllTextContent() {
        return getAllTopics().stream()
                .map(Topic::getText)
                .filter(text -> text != null && !text.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all note content from the mindmap.
     * 
     * @return List of all note content
     */
    @NotNull
    public List<String> getAllNoteContent() {
        return getAllTopics().stream()
                .map(Topic::getNote)
                .filter(note -> note != null && !note.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all link URLs from the mindmap.
     * 
     * @return List of all link URLs
     */
    @NotNull
    public List<String> getAllLinkUrls() {
        return getAllTopics().stream()
                .map(Topic::getLinkUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Counts the number of topics with content (text, notes, or links).
     * 
     * @return Number of topics with content
     */
    public int countTopicsWithContent() {
        return (int) getAllTopics().stream()
                .filter(Topic::hasContent)
                .count();
    }
    
    /**
     * Counts the number of topics with notes.
     * 
     * @return Number of topics with notes
     */
    public int countTopicsWithNotes() {
        return (int) getAllTopics().stream()
                .filter(topic -> topic.getNote() != null && !topic.getNote().trim().isEmpty())
                .count();
    }
    
    /**
     * Counts the number of topics with links.
     * 
     * @return Number of topics with links
     */
    public int countTopicsWithLinks() {
        return (int) getAllTopics().stream()
                .filter(topic -> topic.getLinkUrl() != null && !topic.getLinkUrl().trim().isEmpty())
                .count();
    }
    
    @Override
    public String toString() {
        return "MapModel{" +
                "title='" + title + '\'' +
                ", topicCount=" + topics.size() +
                '}';
    }
}
