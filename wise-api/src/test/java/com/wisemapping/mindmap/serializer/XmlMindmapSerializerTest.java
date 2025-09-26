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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for XML serialization of MapModel.
 */
public class XmlMindmapSerializerTest {

    @Test
    @DisplayName("Should serialize simple mindmap to XML correctly")
    public void testSimpleMindmapSerialization() throws Exception {
        MapModel map = createSimpleMindmap();
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        assertTrue(xml.trim().startsWith("<map"));
        assertTrue(xml.trim().endsWith("</map>"));
        assertTrue(xml.contains("Simple Mindmap"));
        assertTrue(xml.contains("Central Topic"));
    }

    @Test
    @DisplayName("Should serialize complex mindmap with nested topics to XML")
    public void testComplexMindmapSerialization() throws Exception {
        MapModel map = createComplexMindmap();
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        
        // Verify XML structure
        assertTrue(xml.contains("<map"));
        assertTrue(xml.contains("</map>"));
        assertTrue(xml.contains("<topic"));
        assertTrue(xml.contains("</topic>"));
        
        // Verify topic content
        assertTrue(xml.contains("Central Topic"));
        assertTrue(xml.contains("Child 1"));
        assertTrue(xml.contains("Child 2"));
        assertTrue(xml.contains("Grandchild"));
        
        // Verify attributes
        assertTrue(xml.contains("central=\"true\""));
        assertTrue(xml.contains("id=\"central-1\""));
    }

    @Test
    @DisplayName("Should serialize mindmap with notes and links to XML")
    public void testMindmapWithNotesAndLinksSerialization() throws Exception {
        MapModel map = createMindmapWithNotesAndLinks();
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        
        // Verify note content (should be in CDATA)
        assertTrue(xml.contains("<note>"));
        assertTrue(xml.contains("</note>"));
        assertTrue(xml.contains("This is a note"));
        
        // Verify link content
        assertTrue(xml.contains("<link"));
        assertTrue(xml.contains("url=\"http://example.com\""));
        assertTrue(xml.contains("</link>"));
    }

    @Test
    @DisplayName("Should serialize mindmap with metadata to XML")
    public void testMindmapWithMetadataSerialization() throws Exception {
        MapModel map = createMindmapWithMetadata();
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        
        // Verify map attributes
        assertTrue(xml.contains("name=\"Mindmap with Metadata\""));
        assertTrue(xml.contains("version=\"1.0\""));
        assertTrue(xml.contains("theme=\"prism\""));
    }

    @Test
    @DisplayName("Should handle XML round-trip correctly")
    public void testXmlRoundTrip() throws Exception {
        MapModel originalMap = createComplexMindmap();
        
        // Serialize to XML
        String xml = XmlMindmapSerializer.serializeToXml(originalMap);
        assertNotNull(xml);
        
        // Parse back to MapModel using existing parser
        MapModel parsedMap = MindmapParser.parseXml(xml);
        assertNotNull(parsedMap);
        
        // Verify the round-trip
        assertEquals(originalMap.getTitle(), parsedMap.getTitle());
        assertEquals(originalMap.getTopics().size(), parsedMap.getTopics().size());
        assertEquals(originalMap.getTotalTopicCount(), parsedMap.getTotalTopicCount());
        
        // Verify central topic
        Topic originalCentral = originalMap.getCentralTopic();
        Topic parsedCentral = parsedMap.getCentralTopic();
        assertNotNull(originalCentral);
        assertNotNull(parsedCentral);
        assertEquals(originalCentral.getText(), parsedCentral.getText());
        assertEquals(originalCentral.isCentral(), parsedCentral.isCentral());
    }

    @Test
    @DisplayName("Should handle special characters in XML correctly")
    public void testSpecialCharactersInXml() throws Exception {
        MapModel map = new MapModel("Test & \"Special\" Characters");
        
        Topic topic = new Topic("Topic with <tags> and 'quotes'");
        topic.setNote("Note with <b>HTML</b> content & special chars");
        topic.setLinkUrl("http://example.com/path?param=value&other=test");
        map.addTopic(topic);
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        
        // Verify that special characters are properly escaped or in CDATA
        assertTrue(xml.contains("Test &amp;"));
        assertTrue(xml.contains("&quot;Special&quot;"));
        
        // Parse back to verify round-trip
        MapModel parsedMap = MindmapParser.parseXml(xml);
        assertNotNull(parsedMap);
        assertEquals(map.getTitle(), parsedMap.getTitle());
        assertEquals(map.getTopics().get(0).getText(), parsedMap.getTopics().get(0).getText());
    }

    @Test
    @DisplayName("Should handle empty mindmap serialization")
    public void testEmptyMindmapSerialization() throws Exception {
        MapModel emptyMap = new MapModel();
        
        String xml = XmlMindmapSerializer.serializeToXml(emptyMap);
        assertNotNull(xml);
        assertTrue(xml.trim().startsWith("<map"));
        assertTrue(xml.trim().endsWith("</map>"));
        
        // Parse back to verify
        MapModel parsedMap = MindmapParser.parseXml(xml);
        assertNotNull(parsedMap);
        assertTrue(parsedMap.getTopics().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception for null input")
    public void testNullInputSerialization() {
        assertThrows(Exception.class, () -> {
            XmlMindmapSerializer.serializeToXml(null);
        });
    }

    @Test
    @DisplayName("Should serialize mindmap with HTML content in notes")
    public void testMindmapWithHtmlContentInNotes() throws Exception {
        MapModel map = new MapModel("Mindmap with HTML Notes");
        
        Topic topic = new Topic("Topic with HTML Note");
        topic.setNote("<p>This is <strong>bold</strong> text with <a href=\"http://example.com\">a link</a></p>");
        map.addTopic(topic);
        
        String xml = XmlMindmapSerializer.serializeToXml(map);
        assertNotNull(xml);
        
        // Verify HTML content is in CDATA
        assertTrue(xml.contains("<![CDATA["));
        assertTrue(xml.contains("<p>This is <strong>bold</strong>"));
        assertTrue(xml.contains("]]>"));
        
        // Parse back to verify round-trip
        MapModel parsedMap = MindmapParser.parseXml(xml);
        assertNotNull(parsedMap);
        assertEquals(topic.getNote(), parsedMap.getTopics().get(0).getNote());
    }

    /**
     * Creates a simple mindmap for testing.
     */
    private MapModel createSimpleMindmap() {
        MapModel map = new MapModel("Simple Mindmap");
        
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
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("central-1");
        
        Topic child1 = new Topic("Child 1");
        child1.setId("child-1");
        
        Topic child2 = new Topic("Child 2");
        child2.setId("child-2");
        
        Topic grandchild = new Topic("Grandchild");
        grandchild.setId("grandchild-1");
        
        child2.addChild(grandchild);
        centralTopic.addChild(child1);
        centralTopic.addChild(child2);
        
        map.addTopic(centralTopic);
        
        return map;
    }

    /**
     * Creates a mindmap with notes and links for testing.
     */
    private MapModel createMindmapWithNotesAndLinks() {
        MapModel map = new MapModel("Mindmap with Notes and Links");
        
        Topic topic = new Topic("Topic with Content");
        topic.setNote("This is a note with content");
        topic.setLinkUrl("http://example.com");
        map.addTopic(topic);
        
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
        map.setMetadata(metadata);
        
        Topic topic = new Topic("Test Topic");
        map.addTopic(topic);
        
        return map;
    }
}
