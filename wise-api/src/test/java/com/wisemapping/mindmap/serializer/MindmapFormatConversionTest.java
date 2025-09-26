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
import com.wisemapping.mindmap.parser.MindmapParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for format conversion between XML and JSON.
 */
public class MindmapFormatConversionTest {

    @Test
    @DisplayName("Should convert XML to JSON and back to XML correctly")
    public void testXmlToJsonToXmlConversion() throws Exception {
        // Start with XML
        String originalXml = createSampleXml();
        
        // Parse XML to MapModel
        MapModel mapModel = MindmapParser.parseXml(originalXml);
        assertNotNull(mapModel);
        
        // Convert to JSON
        String json = JsonMindmapSerializer.serializeToJson(mapModel);
        assertNotNull(json);
        assertTrue(json.trim().startsWith("{"));
        assertTrue(json.trim().endsWith("}"));
        
        // Convert back to MapModel from JSON
        MapModel jsonMapModel = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(jsonMapModel);
        
        // Convert back to XML
        String convertedXml = XmlMindmapSerializer.serializeToXml(jsonMapModel);
        assertNotNull(convertedXml);
        
        // Parse the converted XML to verify it's valid
        MapModel finalMapModel = MindmapParser.parseXml(convertedXml);
        assertNotNull(finalMapModel);
        
        // Verify content is preserved
        assertEquals(mapModel.getTitle(), finalMapModel.getTitle());
        assertEquals(mapModel.getTopics().size(), finalMapModel.getTopics().size());
        assertEquals(mapModel.getTotalTopicCount(), finalMapModel.getTotalTopicCount());
    }

    @Test
    @DisplayName("Should convert JSON to XML and back to JSON correctly")
    public void testJsonToXmlToJsonConversion() throws Exception {
        // Start with a MapModel
        MapModel originalMapModel = createSampleMapModel();
        
        // Convert to JSON
        String json = JsonMindmapSerializer.serializeToJson(originalMapModel);
        assertNotNull(json);
        
        // Convert JSON to MapModel
        MapModel jsonMapModel = JsonMindmapSerializer.deserializeFromJson(json);
        assertNotNull(jsonMapModel);
        
        // Convert to XML
        String xml = XmlMindmapSerializer.serializeToXml(jsonMapModel);
        assertNotNull(xml);
        assertTrue(xml.trim().startsWith("<map"));
        assertTrue(xml.trim().endsWith("</map>"));
        
        // Convert XML back to MapModel
        MapModel xmlMapModel = MindmapParser.parseXml(xml);
        assertNotNull(xmlMapModel);
        
        // Convert back to JSON
        String finalJson = JsonMindmapSerializer.serializeToJson(xmlMapModel);
        assertNotNull(finalJson);
        
        // Parse both JSON strings to compare
        MapModel finalMapModel = JsonMindmapSerializer.deserializeFromJson(finalJson);
        assertNotNull(finalMapModel);
        
        // Verify content is preserved
        assertEquals(originalMapModel.getTitle(), finalMapModel.getTitle());
        assertEquals(originalMapModel.getTopics().size(), finalMapModel.getTopics().size());
        assertEquals(originalMapModel.getTotalTopicCount(), finalMapModel.getTotalTopicCount());
    }

    @Test
    @DisplayName("Should preserve complex nested structure during format conversion")
    public void testComplexNestedStructureConversion() throws Exception {
        // Create complex mindmap
        MapModel complexMap = createComplexMapModel();
        
        // Convert to JSON
        String json = JsonMindmapSerializer.serializeToJson(complexMap);
        MapModel jsonMap = JsonMindmapSerializer.deserializeFromJson(json);
        
        // Convert to XML
        String xml = XmlMindmapSerializer.serializeToXml(jsonMap);
        MapModel xmlMap = MindmapParser.parseXml(xml);
        
        // Verify complex structure is preserved
        assertEquals(complexMap.getTotalTopicCount(), xmlMap.getTotalTopicCount());
        
        Topic originalCentral = complexMap.getCentralTopic();
        Topic xmlCentral = xmlMap.getCentralTopic();
        assertNotNull(originalCentral);
        assertNotNull(xmlCentral);
        
        assertEquals(originalCentral.getChildren().size(), xmlCentral.getChildren().size());
        
        // Verify nested children
        if (!originalCentral.getChildren().isEmpty()) {
            Topic originalChild = originalCentral.getChildren().get(0);
            Topic xmlChild = xmlCentral.getChildren().get(0);
            assertEquals(originalChild.getChildren().size(), xmlChild.getChildren().size());
        }
    }

    @Test
    @DisplayName("Should preserve metadata during format conversion")
    public void testMetadataPreservationDuringConversion() throws Exception {
        MapModel mapWithMetadata = createMapModelWithMetadata();
        
        // Convert through JSON
        String json = JsonMindmapSerializer.serializeToJson(mapWithMetadata);
        MapModel jsonMap = JsonMindmapSerializer.deserializeFromJson(json);
        
        // Convert through XML
        String xml = XmlMindmapSerializer.serializeToXml(jsonMap);
        MapModel xmlMap = MindmapParser.parseXml(xml);
        
        // Verify metadata preservation
        MapMetadata originalMetadata = mapWithMetadata.getMetadata();
        MapMetadata xmlMetadata = xmlMap.getMetadata();
        
        assertEquals(originalMetadata.getVersion(), xmlMetadata.getVersion());
        assertEquals(originalMetadata.getTheme(), xmlMetadata.getTheme());
        assertEquals(originalMetadata.getAuthor(), xmlMetadata.getAuthor());
        assertEquals(originalMetadata.getCustomProperties().size(), xmlMetadata.getCustomProperties().size());
    }

    @Test
    @DisplayName("Should preserve special characters during format conversion")
    public void testSpecialCharactersPreservationDuringConversion() throws Exception {
        MapModel mapWithSpecialChars = createMapModelWithSpecialCharacters();
        
        // Convert through JSON
        String json = JsonMindmapSerializer.serializeToJson(mapWithSpecialChars);
        MapModel jsonMap = JsonMindmapSerializer.deserializeFromJson(json);
        
        // Convert through XML
        String xml = XmlMindmapSerializer.serializeToXml(jsonMap);
        MapModel xmlMap = MindmapParser.parseXml(xml);
        
        // Verify special characters are preserved
        Topic originalTopic = mapWithSpecialChars.getTopics().get(0);
        Topic xmlTopic = xmlMap.getTopics().get(0);
        
        assertEquals(originalTopic.getText(), xmlTopic.getText());
        assertEquals(originalTopic.getNote(), xmlTopic.getNote());
        assertEquals(originalTopic.getLinkUrl(), xmlTopic.getLinkUrl());
    }

    @Test
    @DisplayName("Should handle HTML content in notes during format conversion")
    public void testHtmlContentInNotesDuringConversion() throws Exception {
        MapModel mapWithHtml = createMapModelWithHtmlContent();
        
        // Convert through JSON
        String json = JsonMindmapSerializer.serializeToJson(mapWithHtml);
        MapModel jsonMap = JsonMindmapSerializer.deserializeFromJson(json);
        
        // Convert through XML
        String xml = XmlMindmapSerializer.serializeToXml(jsonMap);
        MapModel xmlMap = MindmapParser.parseXml(xml);
        
        // Verify HTML content is preserved
        Topic originalTopic = mapWithHtml.getTopics().get(0);
        Topic xmlTopic = xmlMap.getTopics().get(0);
        
        assertEquals(originalTopic.getNote(), xmlTopic.getNote());
        assertTrue(xmlTopic.getNote().contains("<p>"));
        assertTrue(xmlTopic.getNote().contains("<strong>"));
        assertTrue(xmlTopic.getNote().contains("</strong>"));
    }

    @Test
    @DisplayName("Should use serializer interface implementations correctly")
    public void testSerializerInterfaceImplementations() throws Exception {
        MapModel map = createSampleMapModel();
        
        // Test JSON serializer implementation
        JsonMindmapSerializerImpl jsonSerializer = new JsonMindmapSerializerImpl();
        assertTrue(jsonSerializer.isFormat("{\"title\":\"test\"}"));
        assertFalse(jsonSerializer.isFormat("<map>test</map>"));
        assertEquals("JSON", jsonSerializer.getFormatName());
        assertEquals("application/json", jsonSerializer.getMimeType());
        
        String json = jsonSerializer.serialize(map);
        MapModel deserializedMap = jsonSerializer.deserialize(json);
        assertEquals(map.getTitle(), deserializedMap.getTitle());
        
        // Test XML serializer implementation
        XmlMindmapSerializerImpl xmlSerializer = new XmlMindmapSerializerImpl();
        assertTrue(xmlSerializer.isFormat("<map>test</map>"));
        assertFalse(xmlSerializer.isFormat("{\"title\":\"test\"}"));
        assertEquals("XML", xmlSerializer.getFormatName());
        assertEquals("application/xml", xmlSerializer.getMimeType());
        
        String xml = xmlSerializer.serialize(map);
        MapModel xmlDeserializedMap = xmlSerializer.deserialize(xml);
        assertEquals(map.getTitle(), xmlDeserializedMap.getTitle());
    }

    /**
     * Creates a sample XML for testing.
     */
    private String createSampleXml() {
        return """
            <map name="Sample Mindmap" version="1.0" theme="prism">
                <topic id="1" text="Central Topic" central="true">
                    <topic id="2" text="Child 1">
                        <note><![CDATA[This is a note for child 1]]></note>
                    </topic>
                    <topic id="3" text="Child 2">
                        <link url="http://example.com"/>
                    </topic>
                </topic>
            </map>
            """;
    }

    /**
     * Creates a sample MapModel for testing.
     */
    private MapModel createSampleMapModel() {
        MapModel map = new MapModel("Sample Mindmap");
        
        MapMetadata metadata = new MapMetadata();
        metadata.setVersion("1.0");
        metadata.setTheme("prism");
        map.setMetadata(metadata);
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("1");
        
        Topic child1 = new Topic("Child 1");
        child1.setId("2");
        child1.setNote("This is a note for child 1");
        
        Topic child2 = new Topic("Child 2");
        child2.setId("3");
        child2.setLinkUrl("http://example.com");
        
        centralTopic.addChild(child1);
        centralTopic.addChild(child2);
        map.addTopic(centralTopic);
        
        return map;
    }

    /**
     * Creates a complex MapModel with nested topics.
     */
    private MapModel createComplexMapModel() {
        MapModel map = new MapModel("Complex Mindmap");
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("1");
        
        Topic child1 = new Topic("Child 1");
        child1.setId("2");
        
        Topic grandchild1 = new Topic("Grandchild 1");
        grandchild1.setId("3");
        
        Topic grandchild2 = new Topic("Grandchild 2");
        grandchild2.setId("4");
        
        child1.addChild(grandchild1);
        child1.addChild(grandchild2);
        
        Topic child2 = new Topic("Child 2");
        child2.setId("5");
        
        centralTopic.addChild(child1);
        centralTopic.addChild(child2);
        map.addTopic(centralTopic);
        
        return map;
    }

    /**
     * Creates a MapModel with metadata.
     */
    private MapModel createMapModelWithMetadata() {
        MapModel map = new MapModel("Mindmap with Metadata");
        
        MapMetadata metadata = new MapMetadata();
        metadata.setVersion("2.0");
        metadata.setTheme("classic");
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

    /**
     * Creates a MapModel with special characters.
     */
    private MapModel createMapModelWithSpecialCharacters() {
        MapModel map = new MapModel("Mindmap with Special Characters");
        
        Topic topic = new Topic("Topic with & \"special\" characters");
        topic.setNote("Note with <tags> and 'quotes'");
        topic.setLinkUrl("http://example.com/path?param=value&other=test");
        map.addTopic(topic);
        
        return map;
    }

    /**
     * Creates a MapModel with HTML content in notes.
     */
    private MapModel createMapModelWithHtmlContent() {
        MapModel map = new MapModel("Mindmap with HTML Content");
        
        Topic topic = new Topic("Topic with HTML Note");
        topic.setNote("<p>This is <strong>bold</strong> text with <a href=\"http://example.com\">a link</a></p>");
        map.addTopic(topic);
        
        return map;
    }
}
