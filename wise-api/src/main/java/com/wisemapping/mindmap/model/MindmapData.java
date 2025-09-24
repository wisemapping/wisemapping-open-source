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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Core mindmap data structure that represents the essential mindmap information
 * without JPA dependencies. This class can be reused across different contexts.
 */
public class MindmapData implements Serializable {
    
    private String id;
    private String title;
    private String description;
    private String xmlContent;
    private boolean isPublic;
    private long creationTime;
    private long lastModificationTime;
    private String creatorId;
    private String lastEditorId;
    
    public MindmapData() {
    }
    
    public MindmapData(@NotNull String title, @NotNull String xmlContent) {
        this.title = title;
        this.xmlContent = xmlContent;
        this.creationTime = System.currentTimeMillis();
        this.lastModificationTime = this.creationTime;
    }
    
    @Nullable
    public String getId() {
        return id;
    }
    
    public void setId(@Nullable String id) {
        this.id = id;
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
    
    @Nullable
    public String getXmlContent() {
        return xmlContent;
    }
    
    public void setXmlContent(@Nullable String xmlContent) {
        this.xmlContent = xmlContent;
        this.lastModificationTime = System.currentTimeMillis();
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    public long getLastModificationTime() {
        return lastModificationTime;
    }
    
    public void setLastModificationTime(long lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }
    
    @Nullable
    public String getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(@Nullable String creatorId) {
        this.creatorId = creatorId;
    }
    
    @Nullable
    public String getLastEditorId() {
        return lastEditorId;
    }
    
    public void setLastEditorId(@Nullable String lastEditorId) {
        this.lastEditorId = lastEditorId;
    }
    
    /**
     * Gets the XML content as a byte array using UTF-8 encoding.
     * 
     * @return XML content as bytes
     */
    @NotNull
    public byte[] getXmlContentAsBytes() {
        if (xmlContent == null) {
            return new byte[0];
        }
        return xmlContent.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Sets the XML content from a byte array using UTF-8 encoding.
     * 
     * @param xmlBytes XML content as bytes
     */
    public void setXmlContentFromBytes(@NotNull byte[] xmlBytes) {
        this.xmlContent = new String(xmlBytes, StandardCharsets.UTF_8);
        this.lastModificationTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a default mindmap with the given title.
     * 
     * @param title The title for the mindmap
     * @return A new MindmapData instance with default XML structure
     */
    @NotNull
    public static MindmapData createDefault(@NotNull String title) {
        String defaultXml = "<map version=\"tango\" theme=\"prism\">" +
                "<topic central=\"true\" text=\"" + escapeXmlAttribute(title) + "\"/></map>";
        return new MindmapData(title, defaultXml);
    }
    
    /**
     * Escapes XML attribute values.
     * 
     * @param value The value to escape
     * @return Escaped value safe for XML attributes
     */
    @NotNull
    private static String escapeXmlAttribute(@NotNull String value) {
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MindmapData that = (MindmapData) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "MindmapData{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", isPublic=" + isPublic +
                ", creationTime=" + creationTime +
                '}';
    }
}
