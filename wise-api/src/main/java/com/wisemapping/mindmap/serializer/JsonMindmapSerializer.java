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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wisemapping.mindmap.model.MapModel;
import org.jetbrains.annotations.NotNull;

/**
 * Serializes and deserializes MapModel to/from JSON format.
 */
public class JsonMindmapSerializer {
    
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    /**
     * Creates a configured ObjectMapper for JSON serialization.
     */
    @NotNull
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }
    
    /**
     * Serializes a MapModel to JSON string.
     * 
     * @param mapModel The map model to serialize
     * @return JSON string representation
     * @throws SerializationException if serialization fails
     */
    @NotNull
    public static String serializeToJson(@NotNull MapModel mapModel) throws SerializationException {
        try {
            return objectMapper.writeValueAsString(mapModel);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize MapModel to JSON", e);
        }
    }
    
    /**
     * Deserializes a MapModel from JSON string.
     * 
     * @param jsonString The JSON string to deserialize
     * @return Deserialized MapModel
     * @throws SerializationException if deserialization fails
     */
    @NotNull
    public static MapModel deserializeFromJson(@NotNull String jsonString) throws SerializationException {
        try {
            return objectMapper.readValue(jsonString, MapModel.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deserialize MapModel from JSON", e);
        }
    }
    
    /**
     * Exception thrown when JSON serialization/deserialization fails.
     */
    public static class SerializationException extends Exception {
        public SerializationException(@NotNull String message, @NotNull Throwable cause) {
            super(message, cause);
        }
    }
}
