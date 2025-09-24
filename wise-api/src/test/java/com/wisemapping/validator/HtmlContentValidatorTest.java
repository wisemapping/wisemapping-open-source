package com.wisemapping.validator;

import com.wisemapping.exceptions.HtmlContentValidationException;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamContentExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HtmlContentValidatorTest {

    @Mock
    private SpamContentExtractor contentExtractor;

    @Mock
    private Mindmap mindmap;

    private HtmlContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new HtmlContentValidator(contentExtractor);
        // Set the maxNoteLength to 5000 using reflection
        ReflectionTestUtils.setField(validator, "maxNoteLength", 5000);
    }

    @Test
    void testValidateNoteContent_WithinLimit_ShouldNotThrow() {
        // Given
        String noteContent = "This is a short note";

        // When & Then
        assertDoesNotThrow(() -> validator.validateNoteContent(noteContent));
    }

    @Test
    void testValidateNoteContent_ExceedsLimit_ShouldThrow() {
        // Given
        String longNote = "x".repeat(5001); // 5001 characters, exceeding the 5000 limit

        // When & Then
        HtmlContentValidationException exception = assertThrows(
            HtmlContentValidationException.class,
            () -> validator.validateNoteContent(longNote)
        );

        assertEquals("LENGTH", exception.getValidationType());
        assertTrue(exception.getMessage().contains("5000"));
        assertTrue(exception.getMessage().contains("5001"));
    }

    @Test
    void testValidateNoteContent_AtLimit_ShouldNotThrow() {
        // Given
        String noteAtLimit = "x".repeat(5000); // Exactly 5000 characters

        // When & Then
        assertDoesNotThrow(() -> validator.validateNoteContent(noteAtLimit));
    }

    @Test
    void testValidateNoteContent_EmptyContent_ShouldNotThrow() {
        // Given
        String emptyContent = "";

        // When & Then
        assertDoesNotThrow(() -> validator.validateNoteContent(emptyContent));
    }

    @Test
    void testValidateNoteContent_NullContent_ShouldNotThrow() {
        // When & Then
        assertDoesNotThrow(() -> validator.validateNoteContent(null));
    }
}
