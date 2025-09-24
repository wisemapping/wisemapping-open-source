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

package com.wisemapping.mindmap.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single topic/node in a mindmap.
 */
public class MindmapNode implements Serializable {
    
    @Nullable
    private String id;
    
    @Nullable
    private String text;
    
    @Nullable
    private String textContent;
    
    @Nullable
    private String noteContent;
    
    @Nullable
    private String linkUrl;
    
    @Nullable
    private String shape;
    
    @Nullable
    private String fontStyle;
    
    @Nullable
    private String bgColor;
    
    @Nullable
    private String brColor;
    
    @Nullable
    private String position;
    
    @Nullable
    private Integer order;
    
    private boolean central = false;
    
    private boolean shrink = false;
    
    @NotNull
    private List<MindmapNode> childTopics = new ArrayList<>();
    
    public MindmapNode() {
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
    public String getTextContent() {
        return textContent;
    }
    
    public void setTextContent(@Nullable String textContent) {
        this.textContent = textContent;
    }
    
    @Nullable
    public String getNoteContent() {
        return noteContent;
    }
    
    public void setNoteContent(@Nullable String noteContent) {
        this.noteContent = noteContent;
    }
    
    @Nullable
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(@Nullable String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    @Nullable
    public String getShape() {
        return shape;
    }
    
    public void setShape(@Nullable String shape) {
        this.shape = shape;
    }
    
    @Nullable
    public String getFontStyle() {
        return fontStyle;
    }
    
    public void setFontStyle(@Nullable String fontStyle) {
        this.fontStyle = fontStyle;
    }
    
    @Nullable
    public String getBgColor() {
        return bgColor;
    }
    
    public void setBgColor(@Nullable String bgColor) {
        this.bgColor = bgColor;
    }
    
    @Nullable
    public String getBrColor() {
        return brColor;
    }
    
    public void setBrColor(@Nullable String brColor) {
        this.brColor = brColor;
    }
    
    @Nullable
    public String getPosition() {
        return position;
    }
    
    public void setPosition(@Nullable String position) {
        this.position = position;
    }
    
    @Nullable
    public Integer getOrder() {
        return order;
    }
    
    public void setOrder(@Nullable Integer order) {
        this.order = order;
    }
    
    public boolean isCentral() {
        return central;
    }
    
    public void setCentral(boolean central) {
        this.central = central;
    }
    
    public boolean isShrink() {
        return shrink;
    }
    
    public void setShrink(boolean shrink) {
        this.shrink = shrink;
    }
    
    @NotNull
    public List<MindmapNode> getChildTopics() {
        return childTopics;
    }
    
    public void setChildTopics(@NotNull List<MindmapNode> childTopics) {
        this.childTopics = childTopics;
    }
    
    /**
     * Adds a child topic to this node.
     * 
     * @param childTopic The child topic to add
     */
    public void addChildTopic(@NotNull MindmapNode childTopic) {
        this.childTopics.add(childTopic);
    }
    
    /**
     * Gets all child topics recursively.
     * 
     * @return List of all descendant topics
     */
    @NotNull
    public List<MindmapNode> getAllChildTopics() {
        List<MindmapNode> allChildren = new ArrayList<>();
        for (MindmapNode child : childTopics) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildTopics());
        }
        return allChildren;
    }
    
    /**
     * Gets the effective text content (either text attribute or textContent element).
     * 
     * @return The effective text content
     */
    @Nullable
    public String getEffectiveText() {
        if (textContent != null && !textContent.trim().isEmpty()) {
            return textContent;
        }
        return text;
    }
    
    /**
     * Checks if this node has any content (text, notes, or links).
     * 
     * @return true if the node has content
     */
    public boolean hasContent() {
        return (getEffectiveText() != null && !getEffectiveText().trim().isEmpty()) ||
               (noteContent != null && !noteContent.trim().isEmpty()) ||
               (linkUrl != null && !linkUrl.trim().isEmpty());
    }
    
    /**
     * Checks if this node has child topics.
     * 
     * @return true if the node has children
     */
    public boolean hasChildren() {
        return !childTopics.isEmpty();
    }
    
    @Override
    public String toString() {
        return "MindmapNode{" +
                "id='" + id + '\'' +
                ", text='" + getEffectiveText() + '\'' +
                ", central=" + central +
                ", childCount=" + childTopics.size() +
                '}';
    }
}
