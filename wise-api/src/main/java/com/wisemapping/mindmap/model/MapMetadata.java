package com.wisemapping.mindmap.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdDate;

    @Nullable
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastModifiedDate;

    @Nullable
    private String author;

    @NotNull
    private Map<String, String> customProperties = new HashMap<>();

    public MapMetadata() {
    }

    @JsonCreator
    public MapMetadata(@JsonProperty("version") @Nullable String version,
                      @JsonProperty("theme") @Nullable String theme,
                      @JsonProperty("createdDate") @Nullable LocalDateTime createdDate,
                      @JsonProperty("lastModifiedDate") @Nullable LocalDateTime lastModifiedDate,
                      @JsonProperty("author") @Nullable String author,
                      @JsonProperty("customProperties") @NotNull Map<String, String> customProperties) {
        this.version = version;
        this.theme = theme;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.author = author;
        this.customProperties = customProperties != null ? customProperties : new HashMap<>();
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
