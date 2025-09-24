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

package com.wisemapping.mindmap.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when mindmap validation fails.
 * This exception is specific to mindmap parsing and validation operations.
 */
public class MindmapValidationException extends Exception {
    
    private final String validationType;
    
    /**
     * Creates a new mindmap validation exception.
     * 
     * @param message The error message
     */
    public MindmapValidationException(@NotNull String message) {
        super(message);
        this.validationType = "GENERAL";
    }
    
    /**
     * Creates a new mindmap validation exception with a specific validation type.
     * 
     * @param message The error message
     * @param validationType The type of validation that failed
     */
    public MindmapValidationException(@NotNull String message, @NotNull String validationType) {
        super(message);
        this.validationType = validationType;
    }
    
    /**
     * Creates a new mindmap validation exception with a cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public MindmapValidationException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
        this.validationType = "GENERAL";
    }
    
    /**
     * Creates a new mindmap validation exception with a specific validation type and cause.
     * 
     * @param message The error message
     * @param validationType The type of validation that failed
     * @param cause The underlying cause
     */
    public MindmapValidationException(@NotNull String message, @NotNull String validationType, @NotNull Throwable cause) {
        super(message, cause);
        this.validationType = validationType;
    }
    
    /**
     * Gets the type of validation that failed.
     * 
     * @return The validation type
     */
    @NotNull
    public String getValidationType() {
        return validationType;
    }
    
    /**
     * Creates an exception for empty mindmap content.
     * 
     * @return A new MindmapValidationException for empty content
     */
    @NotNull
    public static MindmapValidationException emptyMindmap() {
        return new MindmapValidationException("Mindmap content is empty", "EMPTY");
    }
    
    /**
     * Creates an exception for invalid mindmap format.
     * 
     * @param content The invalid content (truncated for display)
     * @return A new MindmapValidationException for invalid format
     */
    @NotNull
    public static MindmapValidationException invalidFormat(@NotNull String content) {
        String truncatedContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        return new MindmapValidationException("Invalid mindmap format: " + truncatedContent, "FORMAT");
    }
    
    /**
     * Creates an exception for mindmap that is too large.
     * 
     * @param nodeCount The number of nodes in the mindmap
     * @return A new MindmapValidationException for oversized mindmap
     */
    @NotNull
    public static MindmapValidationException tooBigMindmap(int nodeCount) {
        return new MindmapValidationException(
            "Mindmap contains too many nodes: " + nodeCount + " (maximum supported: 4000)", 
            "SIZE"
        );
    }
    
    /**
     * Creates an exception for XML parsing errors.
     * 
     * @param cause The underlying parsing exception
     * @return A new MindmapValidationException for parsing errors
     */
    @NotNull
    public static MindmapValidationException parsingError(@NotNull Throwable cause) {
        return new MindmapValidationException("Failed to parse mindmap XML: " + cause.getMessage(), "PARSING", cause);
    }
}
