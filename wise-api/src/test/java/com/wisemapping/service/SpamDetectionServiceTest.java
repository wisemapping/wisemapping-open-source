package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private MetricsService metricsService;

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
        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        KeywordPatternStrategy keywordPatternStrategy = new KeywordPatternStrategy(contentExtractor);
        
        // Set test configuration for node count exemption (use a high value for tests)
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 100);
        ReflectionTestUtils.setField(keywordPatternStrategy, "minNodesExemption", 100);
        
        // Create service with strategies (excluding UserBehaviorStrategy for unit tests)
        spamDetectionService = new SpamDetectionService(Arrays.asList(fewNodesStrategy, keywordPatternStrategy));
        
        // Inject the mocked MetricsService and SpamContentExtractor
        ReflectionTestUtils.setField(spamDetectionService, "metricsService", metricsService);
        ReflectionTestUtils.setField(spamDetectionService, "spamContentExtractor", contentExtractor);
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
    void testMultipleNodesWithLinks_NoSpamKeywords_ShouldDetectSpam() throws Exception {
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
        assertTrue(spamDetectionService.isSpamContent(mindmap)); // Now detects spam due to updated SingleNodeWithLinkStrategy
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

    @Test
    void testNodeCountExemption_WithHighNodeCount_ShouldNotDetectSpam() throws Exception {
        // Create a mindmap with 20 nodes (more than the test threshold of 100, but we'll test with a lower threshold)
        String xml = """
                <map>
                    <topic central="true" text="Complex Business Plan">
                        <topic text="Market Analysis">
                            <topic text="Target Market"/>
                            <topic text="Competition"/>
                            <topic text="Market Size"/>
                        </topic>
                        <topic text="Financial Projections">
                            <topic text="Revenue"/>
                            <topic text="Costs"/>
                            <topic text="Profit"/>
                        </topic>
                        <topic text="Operations">
                            <topic text="Team"/>
                            <topic text="Processes"/>
                            <topic text="Technology"/>
                        </topic>
                        <topic text="Marketing">
                            <topic text="Strategy"/>
                            <topic text="Channels"/>
                            <topic text="Budget"/>
                        </topic>
                        <topic text="Risk Management">
                            <topic text="Risks"/>
                            <topic text="Mitigation"/>
                            <topic text="Contingency"/>
                        </topic>
                        <topic text="Implementation">
                            <topic text="Timeline"/>
                            <topic text="Milestones"/>
                        </topic>
                    </topic>
                </map>
                """;
        
        // Create a new service instance with a lower threshold (16) to test the exemption
        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        KeywordPatternStrategy keywordPatternStrategy = new KeywordPatternStrategy(contentExtractor);
        
        // Set a lower threshold for this test (16 nodes)
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 16);
        ReflectionTestUtils.setField(keywordPatternStrategy, "minNodesExemption", 16);
        
        SpamDetectionService testService = new SpamDetectionService(Arrays.asList(fewNodesStrategy, keywordPatternStrategy));
        ReflectionTestUtils.setField(testService, "metricsService", metricsService);
        ReflectionTestUtils.setField(testService, "spamContentExtractor", contentExtractor);
        
        // Create a mindmap with spam keywords but high node count
        Mindmap mindmap = createMindmap("Business Plan", "CEO opportunity make money fast", xml);
        
        // Should NOT be detected as spam due to high node count (20 nodes > 16 threshold)
        assertFalse(testService.isSpamContent(mindmap));
    }

    @Test
    void testNodeCountExemption_WithLowNodeCount_ShouldDetectSpam() throws Exception {
        // Create a mindmap with only 3 nodes (below the test threshold)
        String xml = """
                <map>
                    <topic central="true" text="CEO Opportunity">
                        <topic text="Make money fast"/>
                        <topic text="Investment opportunity"/>
                    </topic>
                </map>
                """;
        
        // Create a new service instance with a higher threshold (5) to test spam detection
        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        KeywordPatternStrategy keywordPatternStrategy = new KeywordPatternStrategy(contentExtractor);
        
        // Set a higher threshold for this test (5 nodes)
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 5);
        ReflectionTestUtils.setField(keywordPatternStrategy, "minNodesExemption", 5);
        
        SpamDetectionService testService = new SpamDetectionService(Arrays.asList(fewNodesStrategy, keywordPatternStrategy));
        ReflectionTestUtils.setField(testService, "metricsService", metricsService);
        ReflectionTestUtils.setField(testService, "spamContentExtractor", contentExtractor);
        
        // Create a mindmap with spam keywords and low node count
        Mindmap mindmap = createMindmap("Opportunity", "CEO make money fast investment", xml);
        
        // SHOULD be detected as spam due to low node count (3 nodes < 5 threshold) and spam keywords
        assertTrue(testService.isSpamContent(mindmap));
    }

    @Test
    void testNodeCountExemption_AtThreshold_ShouldDetectSpam() throws Exception {
        // Create a mindmap with exactly 5 nodes (at the threshold)
        String xml = """
                <map>
                    <topic central="true" text="CEO Business">
                        <topic text="Investment"/>
                        <topic text="Make money"/>
                        <topic text="Fast profit"/>
                        <topic text="Opportunity"/>
                    </topic>
                </map>
                """;
        
        // Create a new service instance with threshold of 5
        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        KeywordPatternStrategy keywordPatternStrategy = new KeywordPatternStrategy(contentExtractor);
        
        // Set threshold to 5 nodes
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 5);
        ReflectionTestUtils.setField(keywordPatternStrategy, "minNodesExemption", 5);
        
        SpamDetectionService testService = new SpamDetectionService(Arrays.asList(fewNodesStrategy, keywordPatternStrategy));
        ReflectionTestUtils.setField(testService, "metricsService", metricsService);
        ReflectionTestUtils.setField(testService, "spamContentExtractor", contentExtractor);
        
        // Create a mindmap with spam keywords and exactly threshold node count
        Mindmap mindmap = createMindmap("Business", "CEO investment make money fast", xml);
        
        // SHOULD be detected as spam because 5 nodes is NOT > 5 threshold (it's equal)
        assertTrue(testService.isSpamContent(mindmap));
    }

    private Mindmap createMindmap(String title, String description, String xml) throws Exception {
        Mindmap mindmap = mock(Mindmap.class);
        lenient().when(mindmap.getTitle()).thenReturn(title);
        lenient().when(mindmap.getDescription()).thenReturn(description);
        lenient().when(mindmap.getXmlStr()).thenReturn(xml);
        return mindmap;
    }
}