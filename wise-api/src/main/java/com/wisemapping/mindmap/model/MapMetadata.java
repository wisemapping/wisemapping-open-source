package com.wisemapping.mindmap.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents metadata for a mindmap as a pure domain model.
 * This class contains information about the mindmap that is not part of its core structure.
 */
public class MapMetadata implements Serializable {

    @Nullable
    private String version;

    @Nullable
    private String theme;

    @Nullable
    private LocalDateTime createdDate;

    @Nullable
    private LocalDateTime lastModifiedDate;

    @Nullable
    private String author;

    @NotNull
    private Map<String, String> customProperties = new HashMap<>();

    public MapMetadata() {
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    @Nullable
    public String getTheme() {
        return theme;
    }

    public void setTheme(@Nullable String theme) {
        this.theme = theme;
    }

    @Nullable
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(@Nullable LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Nullable
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(@Nullable LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    @NotNull
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(@NotNull Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * Sets a custom property.
     * 
     * @param key The property key
     * @param value The property value
     */
    public void setCustomProperty(@NotNull String key, @NotNull String value) {
        this.customProperties.put(key, value);
    }

    /**
     * Gets a custom property.
     * 
     * @param key The property key
     * @return The property value, or null if not found
     */
    @Nullable
    public String getCustomProperty(@NotNull String key) {
        return this.customProperties.get(key);
    }

    /**
     * Checks if a custom property exists.
     * 
     * @param key The property key
     * @return true if the property exists, false otherwise
     */
    public boolean hasCustomProperty(@NotNull String key) {
        return this.customProperties.containsKey(key);
    }

    @Override
    public String toString() {
        return "MapMetadata{" +
                "version='" + version + '\'' +
                ", theme='" + theme + '\'' +
                ", author='" + author + '\'' +
                ", customPropertiesCount=" + customProperties.size() +
                '}';
    }
}
