package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import com.wisemapping.service.spam.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SpamDetectionServiceTest {

    private SpamDetectionService spamDetectionService;
    private SpamContentExtractor contentExtractor;

    @Mock
    private Resource spamKeywordsResource;

    @BeforeEach
    void setUp() throws Exception {
        // Mock spam keywords file content
        String keywordsContent = """
                ceo
                chief executive
                business opportunity
                investment
                cryptocurrency
                make money
                work from home
                mlm
                guaranteed
                click here
                """;
        
        InputStream stream = new ByteArrayInputStream(keywordsContent.getBytes());
        when(spamKeywordsResource.getInputStream()).thenReturn(stream);
        
        // Create content extractor with mocked resource
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "spamKeywordsResource", spamKeywordsResource);
        contentExtractor.loadSpamKeywords();
        
        // Create strategies
        SingleNodeWithLinkStrategy singleNodeStrategy = new SingleNodeWithLinkStrategy(contentExtractor);
        KeywordPatternStrategy keywordPatternStrategy = new KeywordPatternStrategy(contentExtractor);
        
        // Create service with strategies
        spamDetectionService = new SpamDetectionService(Arrays.asList(singleNodeStrategy, keywordPatternStrategy));
    }

    @Test
    void testNullMindmap() {
        assertFalse(spamDetectionService.isSpamContent(null));
    }

    @Test
    void testEmptyMindmap() throws Exception {
        Mindmap mindmap = createMindmap("Test", "Description", "");
        assertFalse(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testLegitimateMap() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Project Planning">
                        <topic text="Phase 1"/>
                        <topic text="Phase 2"/>
                        <topic text="Phase 3"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Project Plan", "Legitimate project planning", xml);
        assertFalse(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testSingleNodeWithLink_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Amazing CEO Opportunity">
                        <link url="http://spam-site.com"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("CEO Opportunity", "Great business", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testSingleNodeWithoutLink_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="CEO Meeting Notes"/>
                </map>
                """;
        Mindmap mindmap = createMindmap("Meeting Notes", "Notes from CEO meeting", xml);
        assertFalse(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testMultipleNodesWithLinks_NoSpamKeywords_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Useful Resources">
                        <topic text="Documentation">
                            <link url="https://docs.example.com"/>
                        </topic>
                        <topic text="Tools">
                            <link url="https://tools.example.com"/>
                        </topic>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Resources", "Helpful links", xml);
        assertFalse(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testFewNodesWithLinksAndCeoKeywords_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="CEO Business Opportunity">
                        <topic text="Investment Details">
                            <link url="http://spam-investment.com"/>
                        </topic>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Investment", "CEO opportunity", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testKeywordSpamDetection() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Main Topic">
                        <topic text="CEO position available"/>
                        <topic text="Great investment opportunity"/>
                        <topic text="Make money fast"/>
                        <topic text="Work from home"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Business", "Multiple spam keywords", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testPatternSpamDetection() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Visit www.spam-site.com for $5000 profit"/>
                </map>
                """;
        Mindmap mindmap = createMindmap("Money", "URL and money patterns", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testCryptoSpamDetection() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Bitcoin Investment">
                        <topic text="WhatsApp +1234567890"/>
                        <topic text="Guaranteed 50% profit"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Crypto", "Bitcoin spam", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testMlmSpamDetection() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Join our team">
                        <topic text="Level 5 achievable"/>
                        <topic text="Build your downline"/>
                        <topic text="MLM opportunity"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Team", "MLM spam", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testUrgencySpamDetection() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Limited time offer">
                        <topic text="Only 3 spots left"/>
                        <topic text="Expires in 24 hours"/>
                        <topic text="Act now"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Urgent", "Urgency tactics", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testMixedKeywordAndPattern_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="CEO role available">
                        <topic text="Contact: www.business-opportunity.com"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Position", "Mixed spam indicators", xml);
        assertTrue(spamDetectionService.isSpamContent(mindmap));
    }

    @Test
    void testBorderlineContent_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Company Structure">
                        <topic text="CEO"/>
                        <topic text="Marketing Team"/>
                        <topic text="Development"/>
                        <topic text="Sales"/>
                        <topic text="Support"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Company", "Legitimate org chart", xml);
        assertFalse(spamDetectionService.isSpamContent(mindmap));
    }

    private Mindmap createMindmap(String title, String description, String xml) throws Exception {
        Mindmap mindmap = mock(Mindmap.class);
        lenient().when(mindmap.getTitle()).thenReturn(title);
        lenient().when(mindmap.getDescription()).thenReturn(description);
        lenient().when(mindmap.getXmlStr()).thenReturn(xml);
        return mindmap;
    }
}