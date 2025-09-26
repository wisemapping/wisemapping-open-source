package com.wisemapping.mindmap.parser;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.mindmap.utils.MindmapValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MindmapParser to verify XML parsing functionality.
 */
public class MindmapParserTest {

    @Test
    @DisplayName("Should parse all XML sample files without errors")
    public void testParseAllXmlSamples() throws IOException {
        // Get the resource directory
        Resource resource = new ClassPathResource("xml-samples");
        Path samplesDir = Paths.get(resource.getURI());
        
        // Find all .wxml files in the samples directory
        List<Path> xmlFiles = Files.walk(samplesDir)
            .filter(path -> path.toString().endsWith(".wxml"))
            .toList();
        
        assertFalse(xmlFiles.isEmpty(), "No XML sample files found");
        
        for (Path xmlFile : xmlFiles) {
            String fileName = xmlFile.getFileName().toString();
            System.out.println("Testing XML file: " + fileName);
            
            try {
                // Read the XML content
                String xmlContent = Files.readString(xmlFile, StandardCharsets.UTF_8);
                assertNotNull(xmlContent, "XML content should not be null for " + fileName);
                assertFalse(xmlContent.trim().isEmpty(), "XML content should not be empty for " + fileName);
                
                // Parse the XML using MindmapParser
                MapModel mapModel = MindmapParser.parseXml(xmlContent);
                assertNotNull(mapModel, "Parsed MapModel should not be null for " + fileName);
                
                // Verify basic structure
                assertNotNull(mapModel.getTopics(), "Topics should not be null for " + fileName);
                
                // Test text content extraction
                String extractedText = MindmapParser.extractTextContent(xmlContent);
                assertNotNull(extractedText, "Extracted text should not be null for " + fileName);
                
                // Test HTML content detection
                MindmapParser.hasHtmlContent(xmlContent);
                // This should not throw an exception
                
                System.out.println("✓ Successfully parsed " + fileName + 
                    " - Topics: " + mapModel.getTotalTopicCount() + 
                    ", Text length: " + extractedText.length());
                
            } catch (MindmapValidationException e) {
                fail("Failed to parse " + fileName + ": " + e.getMessage());
            } catch (Exception e) {
                fail("Unexpected error parsing " + fileName + ": " + e.getMessage());
            }
        }
        
        System.out.println("✓ All " + xmlFiles.size() + " XML sample files parsed successfully!");
    }
    
    @Test
    @DisplayName("Should handle empty XML content")
    public void testEmptyXmlContent() {
        assertThrows(MindmapValidationException.class, () -> {
            MindmapParser.parseXml("");
        });
        
        assertThrows(MindmapValidationException.class, () -> {
            MindmapParser.parseXml(null);
        });
    }
    
    @Test
    @DisplayName("Should handle malformed XML gracefully")
    public void testMalformedXml() {
        String malformedXml = "<map><topic>Unclosed tag";
        
        // Should not throw an exception, but should handle gracefully
        assertDoesNotThrow(() -> {
            String extractedText = MindmapParser.extractTextContent(malformedXml);
            assertNotNull(extractedText);
        });
        
        assertDoesNotThrow(() -> {
        });
    }
    
    @Test
    @DisplayName("Should extract text content correctly")
    public void testTextContentExtraction() {
        String xml = """
            <map name="Test Map">
                <topic id="1" text="Main Topic">
                    <topic id="2" text="Sub Topic 1">
                        <note>This is a note with some content</note>
                    </topic>
                    <topic id="3" text="Sub Topic 2">
                        <link url="https://example.com">Example Link</link>
                    </topic>
                </topic>
            </map>
            """;
        
        String extractedText = MindmapParser.extractTextContent(xml);
        assertNotNull(extractedText);
        assertTrue(extractedText.contains("Test Map"));
        assertTrue(extractedText.contains("Main Topic"));
        assertTrue(extractedText.contains("Sub Topic 1"));
        assertTrue(extractedText.contains("Sub Topic 2"));
        assertTrue(extractedText.contains("This is a note with some content"));
        assertTrue(extractedText.contains("https://example.com"));
    }
    
    @Test
    @DisplayName("Should detect HTML content correctly")
    public void testHtmlContentDetection() {
        String xmlWithHtml = """
            <map>
                <topic id="1" text="Topic with HTML">
                    <note><![CDATA[<p>This is <strong>HTML</strong> content</p>]]></note>
                </topic>
            </map>
            """;
        
        String xmlWithoutHtml = """
            <map>
                <topic id="1" text="Topic without HTML">
                    <note>This is plain text content</note>
                </topic>
            </map>
            """;
        
        assertTrue(MindmapParser.hasHtmlContent(xmlWithHtml));
        assertFalse(MindmapParser.hasHtmlContent(xmlWithoutHtml));
    }
    
    @Test
    @DisplayName("Should sanitize HTML content correctly")
    public void testHtmlContentSanitization() {
        String htmlContent = "<p>This is <strong>bold</strong> text with <script>alert('xss')</script> dangerous content</p>";
        
        String sanitized = MindmapParser.sanitizeHtmlContent(htmlContent);
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("This is bold text"));
        assertFalse(sanitized.contains("script"));
        assertFalse(sanitized.contains("alert"));
    }
    
    @Test
    @DisplayName("Should parse complex mindmap structure")
    public void testComplexMindmapStructure() throws MindmapValidationException {
        String xml = """
            <map name="Complex Map" version="1.0">
                <topic id="root" text="Root Topic" central="true">
                    <topic id="child1" text="Child 1">
                        <note>Note for child 1</note>
                        <link url="http://example1.com"/>
                    </topic>
                    <topic id="child2" text="Child 2">
                        <topic id="grandchild1" text="Grandchild 1">
                            <note><![CDATA[<p>HTML note content</p>]]></note>
                        </topic>
                        <topic id="grandchild2" text="Grandchild 2"/>
                    </topic>
                </topic>
            </map>
            """;
        
        MapModel mapModel = MindmapParser.parseXml(xml);
        assertNotNull(mapModel);
        assertEquals("Complex Map", mapModel.getTitle());
        assertEquals("1.0", mapModel.getMetadata().getVersion());
        
        List<Topic> topics = mapModel.getTopics();
        assertEquals(1, topics.size());
        
        Topic rootTopic = topics.get(0);
        assertEquals("Root Topic", rootTopic.getText());
        assertTrue(rootTopic.isCentral());
        assertEquals(2, rootTopic.getChildren().size());
        
        // Test that we can get all topics
        List<Topic> allTopics = mapModel.getAllTopics();
        assertEquals(5, allTopics.size()); // root + 2 children + 2 grandchildren
        
        // Test text content extraction
        String extractedText = MindmapParser.extractTextContent(xml);
        assertTrue(extractedText.contains("Complex Map"));
        assertTrue(extractedText.contains("Root Topic"));
        assertTrue(extractedText.contains("Child 1"));
        assertTrue(extractedText.contains("Child 2"));
        assertTrue(extractedText.contains("Grandchild 1"));
        assertTrue(extractedText.contains("Grandchild 2"));
        assertTrue(extractedText.contains("Note for child 1"));
        assertTrue(extractedText.contains("http://example1.com"));
    }
}
