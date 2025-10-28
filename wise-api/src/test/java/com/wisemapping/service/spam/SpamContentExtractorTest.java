package com.wisemapping.service.spam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = com.wisemapping.config.AppConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.mindmap.note.max-length=10000"
})
class SpamContentExtractorTest {

    @Autowired
    private SpamContentExtractor extractor;

    @Test
    void testCountNoteCharacters_WithinLimit() {
        // Given
        String noteContent = "This is a test note";

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(noteContent);

        // Then
        assertNotNull(result);
        assertEquals(19, result.getRawLength());
        assertEquals(19, result.getTextLength());
        assertFalse(result.isHtml());
        assertFalse(result.isOverLimit());
        assertTrue(result.getUsagePercentage() < 1.0); // Less than 1% of 10000
    }

    @Test
    void testCountNoteCharacters_AtLimit() {
        // Given
        String noteAtLimit = "x".repeat(10000);

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(noteAtLimit);

        // Then
        assertNotNull(result);
        assertEquals(10000, result.getRawLength());
        assertEquals(10000, result.getTextLength());
        assertFalse(result.isHtml());
        assertFalse(result.isOverLimit());
        assertEquals(100.0, result.getUsagePercentage(), 0.1);
    }

    @Test
    void testCountNoteCharacters_OverLimit() {
        // Given
        String noteOverLimit = "x".repeat(10001);

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(noteOverLimit);

        // Then
        assertNotNull(result);
        assertEquals(10001, result.getRawLength());
        assertEquals(10001, result.getTextLength());
        assertFalse(result.isHtml());
        assertTrue(result.isOverLimit());
        assertTrue(result.getUsagePercentage() > 100.0);
    }

    @Test
    void testCountNoteCharacters_EmptyContent() {
        // Given
        String emptyContent = "";

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(emptyContent);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRawLength());
        assertEquals(0, result.getTextLength());
        assertFalse(result.isHtml());
        assertFalse(result.isOverLimit());
        assertEquals(0.0, result.getUsagePercentage());
    }

    @Test
    void testCountNoteCharacters_NullContent() {
        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRawLength());
        assertEquals(0, result.getTextLength());
        assertFalse(result.isHtml());
        assertFalse(result.isOverLimit());
        assertEquals(0.0, result.getUsagePercentage());
    }

    @Test
    void testCountNoteCharacters_HtmlContent() {
        // Given
        String htmlContent = "<p>This is <strong>HTML</strong> content</p>";

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(htmlContent);

        // Then
        assertNotNull(result);
        assertEquals(44, result.getRawLength()); // Original HTML length
        assertEquals(20, result.getTextLength()); // "This is HTML content" length
        assertTrue(result.isHtml());
        assertFalse(result.isOverLimit()); // Should use text length for validation
        assertTrue(result.getTextLength() < result.getRawLength()); // Sanitized text should be shorter
    }

    @Test
    void testCountNoteCharacters_HtmlContent_OverLimit() {
        // Given - HTML content where text content exceeds limit but raw HTML doesn't
        String longText = "x".repeat(10001); // 10001 characters of text
        String htmlContent = "<p>" + longText + "</p>"; // HTML adds 7 more characters

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(htmlContent);

        // Then
        assertNotNull(result);
        assertEquals(10008, result.getRawLength()); // HTML length (10001 + 7)
        assertEquals(10001, result.getTextLength()); // Text length
        assertTrue(result.isHtml());
        assertTrue(result.isOverLimit()); // Should be over limit based on text length
    }

    @Test
    void testIsHtmlContent_PlainText() {
        // Given
        String plainText = "This is plain text";

        // When
        boolean result = extractor.isHtmlContent(plainText);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsHtmlContent_HtmlContent() {
        // Given
        String htmlContent = "<p>This is HTML content</p>";

        // When
        boolean result = extractor.isHtmlContent(htmlContent);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsHtmlContent_EmptyContent() {
        // Given
        String emptyContent = "";

        // When
        boolean result = extractor.isHtmlContent(emptyContent);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsHtmlContent_NullContent() {
        // When
        boolean result = extractor.isHtmlContent(null);

        // Then
        assertFalse(result);
    }
}
