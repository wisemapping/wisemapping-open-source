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
class ContactInfoSpamStrategyTest {

    private ContactInfoSpamStrategy contactInfoSpamStrategy;
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
        contactInfoSpamStrategy = new ContactInfoSpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(contactInfoSpamStrategy, "minNodesExemption", 15);
    }

    @Test
    void testNullMindmap() {
        assertFalse(contactInfoSpamStrategy.detectSpam(null).isSpam());
    }

    @Test
    void testEmptyMindmap() throws Exception {
        Mindmap mindmap = createMindmap("Test", "Description", "");
        assertFalse(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testLegitimateMap() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Project Planning">
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
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Project Plan", "Legitimate project planning", xml);
        assertFalse(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testCompleteContactInfo_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Contact Us">
                        <topic text="Visit us at www.example.com"/>
                        <topic text="Call us at (555) 123-4567"/>
                        <topic text="123 Main Street, City, State 12345"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Contact", "Our contact information", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testWebsiteAndPhoneWithKeywords_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Get in Touch">
                        <topic text="Visit our website: https://business.com"/>
                        <topic text="Call now: +1-555-987-6543"/>
                        <topic text="Contact us today for consultation"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Contact", "Contact us for free consultation", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testMultipleContactPatterns_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Business Info">
                        <topic text="www.company.com"/>
                        <topic text="info@company.com"/>
                        <topic text="555-123-4567"/>
                        <topic text="123 Business Ave"/>
                        <topic text="Call today for appointment"/>
                        <topic text="Visit us for consultation"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Business", "Contact us for business", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testExcessiveContactPatterns_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Contact Information">
                        <topic text="www.site1.com"/>
                        <topic text="www.site2.com"/>
                        <topic text="www.site3.com"/>
                        <topic text="www.site4.com"/>
                        <topic text="www.site5.com"/>
                        <topic text="www.site6.com"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Sites", "Multiple websites", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testPhoneNumberFormats_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Call Us">
                        <topic text="+1-555-123-4567"/>
                        <topic text="(555) 123-4567"/>
                        <topic text="555.123.4567"/>
                        <topic text="555 123 4567"/>
                        <topic text="Contact us now"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Phone", "Call us for appointment", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testAddressFormats_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Visit Us">
                        <topic text="123 Main Street, City, State 12345"/>
                        <topic text="456 Oak Avenue, Town, Province A1B 2C3"/>
                        <topic text="789 Pine Road, Village, Country 1234 AB"/>
                        <topic text="Find us at our location"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Location", "Visit us at our location", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testEmailAddresses_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Email Us">
                        <topic text="contact@business.com"/>
                        <topic text="info@company.org"/>
                        <topic text="sales@enterprise.net"/>
                        <topic text="Get in touch with us"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Email", "Contact us via email", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testSocialMediaHandles_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Follow Us">
                        <topic text="@businessname"/>
                        <topic text="facebook.com/ourbusiness"/>
                        <topic text="twitter.com/ourcompany"/>
                        <topic text="instagram.com/ourbrand"/>
                        <topic text="Reach out to us"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Social", "Follow us on social media", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testContactKeywords_ShouldDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Business Services">
                        <topic text="Contact us for consultation"/>
                        <topic text="Get in touch today"/>
                        <topic text="Call now for appointment"/>
                        <topic text="Visit us for free estimate"/>
                        <topic text="Reach out for booking"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Services", "Contact us for services", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testLegitimateContentWithSomeContactInfo_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Company Overview">
                        <topic text="Our Mission">
                            <topic text="Provide quality services"/>
                            <topic text="Customer satisfaction"/>
                        </topic>
                        <topic text="Our Team">
                            <topic text="Experienced professionals"/>
                            <topic text="Dedicated staff"/>
                        </topic>
                        <topic text="Contact">
                            <topic text="General inquiries"/>
                        </topic>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Company", "About our company", xml);
        assertFalse(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testSingleWebsite_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Resources">
                        <topic text="Check out www.example.com for more info"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Resources", "Helpful resources", xml);
        assertFalse(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testSinglePhoneNumber_ShouldNotDetectSpam() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Emergency">
                        <topic text="Emergency number: 911"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Emergency", "Emergency contact", xml);
        assertFalse(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testStrategyName() {
        assertEquals("ContactInfo", contactInfoSpamStrategy.getStrategyName());
    }

    @Test
    void testNewExampleFromUser_ShouldDetectSpam() throws Exception {
        // Test case based on the new example provided by user
        String xml = """
                <map>
                    <topic central="true" text="Business Contact">
                        <topic text="Visit our website: www.example-business.com"/>
                        <topic text="Call us: +1-555-123-4567"/>
                        <topic text="Address: 123 Business St, City, State 12345"/>
                        <topic text="Email: contact@business.com"/>
                        <topic text="Get in touch today"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Contact Info", "Business contact information", xml);
        assertTrue(contactInfoSpamStrategy.detectSpam(mindmap).isSpam());
    }

    @Test
    void testSpamDetectionResultDetails() throws Exception {
        String xml = """
                <map>
                    <topic central="true" text="Contact Us">
                        <topic text="Visit us at www.example.com"/>
                        <topic text="Call us at (555) 123-4567"/>
                        <topic text="123 Main Street, City, State 12345"/>
                    </topic>
                </map>
                """;
        Mindmap mindmap = createMindmap("Contact", "Our contact information", xml);
        
        var result = contactInfoSpamStrategy.detectSpam(mindmap);
        assertTrue(result.isSpam());
        assertTrue(result.getReason().contains("Contact info spam detected - contains address, website, and phone"));
        assertTrue(result.getDetails().contains("HasWebsite: true"));
        assertTrue(result.getDetails().contains("HasPhone: true"));
        assertTrue(result.getDetails().contains("HasAddress: true"));
    }

    private Mindmap createMindmap(String title, String description, String xml) throws Exception {
        Mindmap mindmap = mock(Mindmap.class);
        lenient().when(mindmap.getTitle()).thenReturn(title);
        lenient().when(mindmap.getDescription()).thenReturn(description);
        lenient().when(mindmap.getXmlStr()).thenReturn(xml);
        return mindmap;
    }
}
