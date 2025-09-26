package com.wisemapping.mindmap.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a topic in a mindmap as a pure domain model.
 * This class focuses on the business concept of a topic without XML implementation details.
 */
public class Topic implements Serializable {

    @Nullable
    private String id;

    @Nullable
    private String text;

    @Nullable
    private String note;

    @Nullable
    private String linkUrl;

    private boolean central = false;

    @NotNull
    private List<Topic> children = new ArrayList<>();

    public Topic() {
    }

    @JsonCreator
    public Topic(@JsonProperty("id") @Nullable String id,
                @JsonProperty("text") @Nullable String text,
                @JsonProperty("note") @Nullable String note,
                @JsonProperty("linkUrl") @Nullable String linkUrl,
                @JsonProperty("central") boolean central,
                @JsonProperty("children") @NotNull List<Topic> children) {
        this.id = id;
        this.text = text;
        this.note = note;
        this.linkUrl = linkUrl;
        this.central = central;
        this.children = children != null ? children : new ArrayList<>();
    }

    public Topic(@Nullable String text) {
        this.text = text;
    }

    public Topic(@Nullable String text, @Nullable String note) {
        this.text = text;
        this.note = note;
    }

    public Topic(@Nullable String text, @Nullable String note, @Nullable String linkUrl) {
        this.text = text;
        this.note = note;
        this.linkUrl = linkUrl;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Nullable
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    @Nullable
    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(@Nullable String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public boolean isCentral() {
        return central;
    }

    public void setCentral(boolean central) {
        this.central = central;
    }

    @NotNull
    public List<Topic> getChildren() {
        return children;
    }

    public void setChildren(@NotNull List<Topic> children) {
        this.children = children;
    }

    /**
     * Adds a child topic.
     * 
     * @param child The child topic to add
     */
    public void addChild(@NotNull Topic child) {
        this.children.add(child);
    }

    /**
     * Gets all child topics recursively.
     * 
     * @return List of all child topics
     */
    @NotNull
    @JsonIgnore
    public List<Topic> getAllChildTopics() {
        List<Topic> allChildren = new ArrayList<>();
        for (Topic child : children) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildTopics());
        }
        return allChildren;
    }

    /**
     * Checks if this topic has any content (text, note, or link).
     * 
     * @return true if the topic has content, false otherwise
     */
    @JsonIgnore
    public boolean hasContent() {
        return (text != null && !text.trim().isEmpty()) ||
               (note != null && !note.trim().isEmpty()) ||
               (linkUrl != null && !linkUrl.trim().isEmpty());
    }

    /**
     * Checks if this topic has a note.
     * 
     * @return true if the topic has a note, false otherwise
     */
    @JsonIgnore
    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }

    /**
     * Checks if this topic has a link.
     * 
     * @return true if the topic has a link, false otherwise
     */
    @JsonIgnore
    public boolean hasLink() {
        return linkUrl != null && !linkUrl.trim().isEmpty();
    }

    /**
     * Gets the total number of characters in this topic's content.
     * 
     * @return Total character count
     */
    @JsonIgnore
    public int getContentLength() {
        int length = 0;
        if (text != null) {
            length += text.length();
        }
        if (note != null) {
            length += note.length();
        }
        if (linkUrl != null) {
            length += linkUrl.length();
        }
        return length;
    }

    /**
     * Gets the effective text content (text or note if text is empty).
     *
     * @return The effective text content
     */
    @Nullable
    @JsonIgnore
    public String getEffectiveText() {
        if (text != null && !text.trim().isEmpty()) {
            return text;
        }
        return note;
    }

    /**
     * Gets the note content.
     *
     * @return The note content
     */
    @Nullable
    @JsonIgnore
    public String getNoteContent() {
        return note;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", hasNote=" + hasNote() +
                ", hasLink=" + hasLink() +
                ", central=" + central +
                ", childrenCount=" + children.size() +
                '}';
    }
}
