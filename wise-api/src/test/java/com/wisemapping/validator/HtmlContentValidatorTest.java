package com.wisemapping.validator;

import com.wisemapping.exceptions.HtmlContentValidationException;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamContentExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
    "app.mindmap.note.max-length=10000"
})
class HtmlContentValidatorTest {

    @Mock
    private Mindmap mindmap;

    @Autowired
    private HtmlContentValidator validator;

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
        String longNote = "x".repeat(10001); // 10001 characters, exceeding the 10000 limit

        // When & Then
        HtmlContentValidationException exception = assertThrows(
            HtmlContentValidationException.class,
            () -> validator.validateNoteContent(longNote)
        );

        assertEquals("LENGTH", exception.getValidationType());
        assertTrue(exception.getMessage().contains("10000"));
        assertTrue(exception.getMessage().contains("10001"));
    }

    @Test
    void testValidateNoteContent_AtLimit_ShouldNotThrow() {
        // Given
        String noteAtLimit = "x".repeat(10000); // Exactly 10000 characters

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
