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

package com.wisemapping.mindmap.serializer;

import com.wisemapping.mindmap.model.MapModel;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for serializing and deserializing MapModel to/from different formats.
 */
public interface MindmapSerializer {
    
    /**
     * Serializes a MapModel to string format.
     * 
     * @param mapModel The map model to serialize
     * @return String representation in the serializer's format
     * @throws SerializationException if serialization fails
     */
    @NotNull
    String serialize(@NotNull MapModel mapModel) throws SerializationException;
    
    /**
     * Deserializes a MapModel from string format.
     * 
     * @param content The string content to deserialize
     * @return Deserialized MapModel
     * @throws SerializationException if deserialization fails
     */
    @NotNull
    MapModel deserialize(@NotNull String content) throws SerializationException;
    
    /**
     * Gets the format name this serializer handles.
     * 
     * @return Format name (e.g., "JSON", "XML")
     */
    @NotNull
    String getFormatName();
    
    /**
     * Gets the MIME type for this format.
     * 
     * @return MIME type (e.g., "application/json", "application/xml")
     */
    @NotNull
    String getMimeType();
    
    /**
     * Checks if the given content appears to be in this serializer's format.
     * 
     * @param content The content to check
     * @return true if the content appears to be in this format
     */
    boolean isFormat(@NotNull String content);
    
    /**
     * Exception thrown when serialization/deserialization fails.
     */
    class SerializationException extends Exception {
        public SerializationException(@NotNull String message, @NotNull Throwable cause) {
            super(message, cause);
        }
        
        public SerializationException(@NotNull String message) {
            super(message);
        }
    }
}
