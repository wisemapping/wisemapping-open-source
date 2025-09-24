package com.wisemapping.service.spam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpamContentExtractorTest {

    private SpamContentExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new SpamContentExtractor();
    }

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
        assertTrue(result.getUsagePercentage() < 1.0); // Less than 1% of 5000
    }

    @Test
    void testCountNoteCharacters_AtLimit() {
        // Given
        String noteAtLimit = "x".repeat(5000);

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(noteAtLimit);

        // Then
        assertNotNull(result);
        assertEquals(5000, result.getRawLength());
        assertEquals(5000, result.getTextLength());
        assertFalse(result.isHtml());
        assertFalse(result.isOverLimit());
        assertEquals(100.0, result.getUsagePercentage(), 0.1);
    }

    @Test
    void testCountNoteCharacters_OverLimit() {
        // Given
        String noteOverLimit = "x".repeat(5001);

        // When
        SpamContentExtractor.NoteCharacterCount result = extractor.countNoteCharacters(noteOverLimit);

        // Then
        assertNotNull(result);
        assertEquals(5001, result.getRawLength());
        assertEquals(5001, result.getTextLength());
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
        assertTrue(result.isHtml());
        assertFalse(result.isOverLimit());
        assertTrue(result.getTextLength() < result.getRawLength()); // Sanitized text should be shorter
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
