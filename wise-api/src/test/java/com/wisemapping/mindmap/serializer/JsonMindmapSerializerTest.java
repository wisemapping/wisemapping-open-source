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
import com.wisemapping.mindmap.model.MapMetadata;
import com.wisemapping.mindmap.model.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JSON serialization/deserialization of MapModel.
 */
public class JsonMindmapSerializerTest {

    @Test
    @DisplayName("Should serialize and deserialize simple mindmap correctly")
    public void testSimpleMindmapSerialization() throws Exception {
        // Create a simple mindmap
        MapModel originalMap = createSimpleMindmap();
        
        // Serialize to JSON
        String json = JsonMindmapSerializer.serializeToJson(originalMap);
        assertNotNull(json);
        assertFalse(json.trim().isEmpty());
        
        // Deserialize back to MapModel
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap);
        
        // Verify the content
        assertEquals(originalMap.getTitle(), deserializedMap.getTitle());
        assertEquals(originalMap.getDescription(), deserializedMap.getDescription());
        assertEquals(originalMap.getTopics().size(), deserializedMap.getTopics().size());
        
        Topic originalTopic = originalMap.getTopics().get(0);
        Topic deserializedTopic = deserializedMap.getTopics().get(0);
        assertEquals(originalTopic.getText(), deserializedTopic.getText());
        assertEquals(originalTopic.isCentral(), deserializedTopic.isCentral());
    }

    @Test
    @DisplayName("Should serialize and deserialize complex mindmap with nested topics")
    public void testComplexMindmapSerialization() throws Exception {
        // Create a complex mindmap
        MapModel originalMap = createComplexMindmap();
        
        // Serialize to JSON
        String json = JsonMindmapSerializer.serializeToJson(originalMap);
        assertNotNull(json);
        
        // Deserialize back to MapModel
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap);
        
        // Verify the structure
        assertEquals(originalMap.getTopics().size(), deserializedMap.getTopics().size());
        assertEquals(originalMap.getTotalTopicCount(), deserializedMap.getTotalTopicCount());
        
        // Verify nested structure
        Topic originalRoot = originalMap.getCentralTopic();
        Topic deserializedRoot = deserializedMap.getCentralTopic();
        assertNotNull(originalRoot);
        assertNotNull(deserializedRoot);
        assertEquals(originalRoot.getChildren().size(), deserializedRoot.getChildren().size());
        
        // Verify child topics
        Topic originalChild = originalRoot.getChildren().get(0);
        Topic deserializedChild = deserializedRoot.getChildren().get(0);
        assertEquals(originalChild.getText(), deserializedChild.getText());
        assertEquals(originalChild.getNote(), deserializedChild.getNote());
        assertEquals(originalChild.getLinkUrl(), deserializedChild.getLinkUrl());
    }

    @Test
    @DisplayName("Should handle mindmap with metadata correctly")
    public void testMindmapWithMetadataSerialization() throws Exception {
        // Create mindmap with metadata
        MapModel originalMap = createMindmapWithMetadata();
        
        // Serialize to JSON
        String json = JsonMindmapSerializer.serializeToJson(originalMap);
        assertNotNull(json);
        
        // Deserialize back to MapModel
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap);
        
        // Verify metadata
        MapMetadata originalMetadata = originalMap.getMetadata();
        MapMetadata deserializedMetadata = deserializedMap.getMetadata();
        
        assertEquals(originalMetadata.getVersion(), deserializedMetadata.getVersion());
        assertEquals(originalMetadata.getTheme(), deserializedMetadata.getTheme());
        assertEquals(originalMetadata.getAuthor(), deserializedMetadata.getAuthor());
        assertEquals(originalMetadata.getCustomProperties().size(), deserializedMetadata.getCustomProperties().size());
        
        // Verify custom properties
        for (Map.Entry<String, String> entry : originalMetadata.getCustomProperties().entrySet()) {
            assertEquals(entry.getValue(), deserializedMetadata.getCustomProperty(entry.getKey()));
        }
    }

    @Test
    @DisplayName("Should handle empty mindmap")
    public void testEmptyMindmapSerialization() throws Exception {
        MapModel emptyMap = new MapModel();
        
        String json = JsonMindmapSerializer.serializeToJson(emptyMap);
        assertNotNull(json);
        
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap);
        assertNull(deserializedMap.getTitle());
        assertNull(deserializedMap.getDescription());
        assertTrue(deserializedMap.getTopics().isEmpty());
    }

    @Test
    @DisplayName("Should handle mindmap with special characters")
    public void testMindmapWithSpecialCharacters() throws Exception {
        MapModel mapWithSpecialChars = new MapModel();
        mapWithSpecialChars.setTitle("Test & \"Special\" Characters");
        mapWithSpecialChars.setDescription("Description with <tags> and 'quotes'");
        
        Topic topic = new Topic("Topic with \n newlines and \t tabs");
        topic.setNote("Note with <b>HTML</b> content");
        topic.setLinkUrl("http://example.com/path?param=value&other=test");
        mapWithSpecialChars.addTopic(topic);
        
        String json = JsonMindmapSerializer.serializeToJson(mapWithSpecialChars);
        assertNotNull(json);
        
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap);
        
        assertEquals(mapWithSpecialChars.getTitle(), deserializedMap.getTitle());
        assertEquals(mapWithSpecialChars.getDescription(), deserializedMap.getDescription());
        assertEquals(mapWithSpecialChars.getTopics().get(0).getText(), deserializedMap.getTopics().get(0).getText());
        assertEquals(mapWithSpecialChars.getTopics().get(0).getNote(), deserializedMap.getTopics().get(0).getNote());
        assertEquals(mapWithSpecialChars.getTopics().get(0).getLinkUrl(), deserializedMap.getTopics().get(0).getLinkUrl());
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON")
    public void testInvalidJsonDeserialization() {
        String invalidJson = "{ invalid json content";
        
        assertThrows(JsonMindmapSerializer.SerializationException.class, () -> {
            JsonMindmapSerializer.deserializeFromJson(invalidJson);
        });
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    public void testNullInputSerialization() {
        // Jackson serializes null to "null" string
        assertDoesNotThrow(() -> {
            String result = JsonMindmapSerializer.serializeToJson(null);
            assertEquals("null", result);
        });
        
        // Jackson deserializes "null" string to null object
        assertDoesNotThrow(() -> {
            MapModel result = JsonMindmapSerializer.deserializeFromJson("null");
            assertNull(result);
        });
    }

    @Test
    @DisplayName("Should serialize and deserialize with LocalDateTime correctly")
    public void testLocalDateTimeSerialization() throws Exception {
        MapModel map = new MapModel();
        MapMetadata metadata = new MapMetadata();
        metadata.setCreatedDate(LocalDateTime.of(2023, 12, 25, 10, 30, 45));
        metadata.setLastModifiedDate(LocalDateTime.of(2023, 12, 26, 14, 20, 10));
        map.setMetadata(metadata);
        
        String json = JsonMindmapSerializer.serializeToJson(map);
        assertNotNull(json);
        assertTrue(json.contains("2023-12-25T10:30:45"));
        assertTrue(json.contains("2023-12-26T14:20:10"));
        
        MapModel deserializedMap = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(deserializedMap.getMetadata().getCreatedDate());
        assertNotNull(deserializedMap.getMetadata().getLastModifiedDate());
        assertEquals(metadata.getCreatedDate(), deserializedMap.getMetadata().getCreatedDate());
        assertEquals(metadata.getLastModifiedDate(), deserializedMap.getMetadata().getLastModifiedDate());
    }

    /**
     * Creates a simple mindmap for testing.
     */
    private MapModel createSimpleMindmap() {
        MapModel map = new MapModel("Simple Mindmap");
        map.setDescription("A simple test mindmap");
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        map.addTopic(centralTopic);
        
        return map;
    }

    /**
     * Creates a complex mindmap with nested topics for testing.
     */
    private MapModel createComplexMindmap() {
        MapModel map = new MapModel("Complex Mindmap");
        map.setDescription("A complex test mindmap with nested topics");
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("central-1");
        
        Topic child1 = new Topic("Child 1");
        child1.setId("child-1");
        child1.setNote("This is a note for child 1");
        child1.setLinkUrl("http://example1.com");
        
        Topic child2 = new Topic("Child 2");
        child2.setId("child-2");
        
        Topic grandchild = new Topic("Grandchild");
        grandchild.setId("grandchild-1");
        grandchild.setNote("This is a grandchild note");
        
        child2.addChild(grandchild);
        centralTopic.addChild(child1);
        centralTopic.addChild(child2);
        
        map.addTopic(centralTopic);
        
        return map;
    }

    /**
     * Creates a mindmap with metadata for testing.
     */
    private MapModel createMindmapWithMetadata() {
        MapModel map = new MapModel("Mindmap with Metadata");
        
        MapMetadata metadata = new MapMetadata();
        metadata.setVersion("1.0");
        metadata.setTheme("prism");
        metadata.setAuthor("Test Author");
        metadata.setCreatedDate(LocalDateTime.now());
        metadata.setLastModifiedDate(LocalDateTime.now());
        metadata.setCustomProperty("custom1", "value1");
        metadata.setCustomProperty("custom2", "value2");
        
        map.setMetadata(metadata);
        
        Topic topic = new Topic("Test Topic");
        map.addTopic(topic);
        
        return map;
    }
}
