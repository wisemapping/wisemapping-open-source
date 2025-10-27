package com.wisemapping.service.spam;

import com.wisemapping.model.Mindmap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DescriptionLengthStrategyTest {

    private DescriptionLengthStrategy descriptionLengthStrategy;
    private SpamContentExtractor contentExtractor;

    @Mock
    private Resource spamKeywordsResource;

    @BeforeEach
    void setUp() throws Exception {
        // Mock spam keywords file content (minimal for this test)
        String keywordsContent = "test keyword";
        
        InputStream stream = new ByteArrayInputStream(keywordsContent.getBytes());
        when(spamKeywordsResource.getInputStream()).thenReturn(stream);
        
        // Create content extractor with mocked resource
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "spamKeywordsResource", spamKeywordsResource);
        contentExtractor.loadSpamKeywords();
        
        // Create strategy with test configuration
        descriptionLengthStrategy = new DescriptionLengthStrategy(contentExtractor);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "maxDescriptionLength", 200);
    }

    @Test
    void testNullMindmap() {
        assertFalse(descriptionLengthStrategy.detectSpam(null).isSpam());
    }

    @Test
    void testEmptyDescription() throws Exception {
        Mindmap mindmap = createMindmap("Test Title", "", "<map><topic central=\"true\" text=\"Test\"/></map>");
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testNullDescription() throws Exception {
        Mindmap mindmap = createMindmap("Test Title", null, "<map><topic central=\"true\" text=\"Test\"/></map>");
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testShortLegitimateDescription() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="My Project">
                        <topic text="Phase 1"/>
                        <topic text="Phase 2"/>
                        <topic text="Phase 3"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("My Project", "This is a simple project description", xml);
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testLongDescriptionWithoutTitle_ShouldDetectSpam() throws Exception {
        String longDescription = "This is a very long description that exceeds the maximum allowed length. " +
                "It contains a lot of text that would typically indicate spam content. " +
                "This is a very long description that exceeds the maximum allowed length. " +
                "More content here to make it even longer and reach the threshold.";
        
        String xml = """
                <map>
                    <topic central="true" text="Test">
                        <topic text="Child 1"/>
                        <topic text="Child 2"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Test Title", longDescription, xml);
        assertTrue(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testLongDescriptionWithTitleDuplication_ShouldDetectSpam() throws Exception {
        // Real example from user: "EforEnergy is a UK-based energy solutions provider..."
        String title = "EforEnergy";
        String longDescription = "EforEnergy is a UK-based energy solutions provider, specialising in " +
                "business energy contract renewals, new energy quotes, meter installations, and temporary " +
                "electricity supplies for construction sites. We also offer MOP (Meter Operator) contracts.";
        
        String xml = """
                <map>
                    <topic central="true" text="Business">
                        <topic text="Services"/>
                        <topic text="Contact"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, longDescription, xml);
        SpamDetectionResult result = descriptionLengthStrategy.detectSpam(mindmap);
        
        assertTrue(result.isSpam());
        assertTrue(result.getReason().contains("Long description with title duplication detected"));
    }

    @Test
    void testTitleInDescription_ShouldDetectSpam() throws Exception {
        String title = "My Business Name";
        String description = "Welcome to My Business Name! We are a leading provider of quality services. " +
                "Contact us today for more information about our offerings.";
        
        String xml = """
                <map>
                    <topic central="true" text="Business">
                        <topic text="Services"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        SpamDetectionResult result = descriptionLengthStrategy.detectSpam(mindmap);
        
        assertTrue(result.isSpam());
        assertTrue(result.getReason().contains("Title text found in description"));
    }

    @Test
    void testTitleInDescriptionCaseInsensitive_ShouldDetectSpam() throws Exception {
        String title = "MyCompany";
        String description = "mycompany is a great place to work with excellent services and products. " +
                "We have been in business for many years serving customers worldwide.";
        
        String xml = """
                <map>
                    <topic central="true" text="About">
                        <topic text="Info"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        assertTrue(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testShortDescriptionWithTitle_ShouldNotDetectSpam() throws Exception {
        // Short descriptions (<=50 chars) with title should not trigger spam detection
        String title = "Project Alpha";
        String description = "Project Alpha overview";
        
        String xml = """
                <map>
                    <topic central="true" text="Project">
                        <topic text="Tasks"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testManyNodesExemption_ShouldNotDetectSpam() throws Exception {
        // Maps with many nodes should be exempted even with long descriptions
        String longDescription = "This is a very long description that exceeds the maximum allowed length. " +
                "It contains a lot of text that would typically indicate spam content. " +
                "This is a very long description that exceeds the maximum allowed length. " +
                "More content here to make it even longer and reach the threshold.";
        
        String xml = """
                <map>
                    <topic central="true" text="Large Project">
                        <topic text="Phase 1"/>
                        <topic text="Phase 2"/>
                        <topic text="Phase 3"/>
                        <topic text="Phase 4"/>
                        <topic text="Phase 5"/>
                        <topic text="Phase 6"/>
                        <topic text="Phase 7"/>
                        <topic text="Phase 8"/>
                        <topic text="Phase 9"/>
                        <topic text="Phase 10"/>
                        <topic text="Phase 11"/>
                        <topic text="Phase 12"/>
                        <topic text="Phase 13"/>
                        <topic text="Phase 14"/>
                        <topic text="Phase 15"/>
                        <topic text="Phase 16"/>
                        <topic text="Phase 17"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Large Project", longDescription, xml);
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testDescriptionAtExactLimit_ShouldNotDetectSpam() throws Exception {
        // Description exactly at 200 chars should not trigger (only > 200)
        String description = "A".repeat(200);
        
        String xml = """
                <map>
                    <topic central="true" text="Test">
                        <topic text="Content"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Test", description, xml);
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testDescriptionJustOverLimit_ShouldDetectSpam() throws Exception {
        // Description at 201 chars should trigger
        String description = "A".repeat(201);
        
        String xml = """
                <map>
                    <topic central="true" text="Test">
                        <topic text="Content"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Test", description, xml);
        assertTrue(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testRealSpamExample_ShouldDetectSpam() throws Exception {
        // Real-world spam example matching the user's description
        String title = "Energy Solutions UK";
        String description = "Energy Solutions UK is a UK-based energy solutions provider, specialising in " +
                "business energy contract renewals, new energy quotes, meter installations, and temporary " +
                "electricity supplies for construction sites. We also offer MOP (Meter Operator) contracts. " +
                "Contact us today for a free consultation.";
        
        String xml = """
                <map>
                    <topic central="true" text="Services">
                        <topic text="Energy"/>
                        <topic text="Contact"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        SpamDetectionResult result = descriptionLengthStrategy.detectSpam(mindmap);
        
        assertTrue(result.isSpam());
        assertNotNull(result.getReason());
        assertNotNull(result.getDetails());
    }

    @Test
    void testStrategyType() {
        assertEquals("DescriptionLength", descriptionLengthStrategy.getType().getStrategyName());
    }

    @Test
    void testSpamDetectionResultDetails_LongDescription() throws Exception {
        String longDescription = "This is a very long description that exceeds the maximum allowed length. " +
                "It contains a lot of text that would typically indicate spam content. " +
                "This is a very long description that exceeds the maximum allowed length. " +
                "More content here to make it even longer.";
        
        String xml = """
                <map>
                    <topic central="true" text="Test">
                        <topic text="Child"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Test", longDescription, xml);
        SpamDetectionResult result = descriptionLengthStrategy.detectSpam(mindmap);
        
        assertTrue(result.isSpam());
        assertTrue(result.getReason().contains("Description exceeds maximum length"));
        assertTrue(result.getDetails().contains("Description length:"));
    }

    @Test
    void testSpamDetectionResultDetails_TitleInDescription() throws Exception {
        String title = "Business Name";
        String description = "Business Name provides excellent services to all customers nationwide. " +
                "We have been serving the community for over 20 years with quality products.";
        
        String xml = """
                <map>
                    <topic central="true" text="About">
                        <topic text="Info"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        SpamDetectionResult result = descriptionLengthStrategy.detectSpam(mindmap);
        
        assertTrue(result.isSpam());
        assertTrue(result.getReason().contains("Title text found in description"));
        assertTrue(result.getDetails().contains("Title:"));
    }

    @Test
    void testWhitespaceOnlyDescription_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Test">
                        <topic text="Content"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap("Test", "   ", xml);
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testPartialTitleMatch_ShouldNotDetectAsSpam() throws Exception {
        // If only part of the title appears in description, it shouldn't necessarily be spam
        String title = "Project Management System";
        String description = "This is about managing projects efficiently with our new system.";
        
        String xml = """
                <map>
                    <topic central="true" text="Project">
                        <topic text="Tasks"/>
                    </topic>
                </map>
                """;
        
        Mindmap mindmap = createMindmap(title, description, xml);
        // This test checks that we need the full title match (case-insensitive)
        // Since "Project Management System" is not fully contained in the description, it should not detect as spam
        assertFalse(descriptionLengthStrategy.detectSpam(mindmap).isSpam());
    }

    private Mindmap createMindmap(String title, String description, String xml) throws Exception {
        Mindmap mindmap = mock(Mindmap.class);
        lenient().when(mindmap.getTitle()).thenReturn(title);
        lenient().when(mindmap.getDescription()).thenReturn(description);
        lenient().when(mindmap.getXmlStr()).thenReturn(xml);
        return mindmap;
    }
}

