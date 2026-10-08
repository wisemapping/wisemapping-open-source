/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.test.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.config.AppConfig;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;

import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@code POST /api/restful/maps/validate-note}
 * ({@link com.wisemapping.rest.MindmapController#validateNoteContent}), the
 * endpoint that drives the editor's note character counter.
 *
 * <p>Assertions are made against the raw JSON because that wire shape — and
 * especially the {@code html} / {@code overLimit} key names Jackson derives
 * from the {@code isHtml()} / {@code isOverLimit()} getters — is what the
 * frontend consumes.
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestNoteValidationTest {

    private static final String VALIDATE_NOTE_URL = "/api/restful/maps/validate-note";

    /** Mirrors {@code app.mindmap.note.max-length}, which defaults to 10000. */
    private static final int MAX_NOTE_LENGTH = 10000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TestRestTemplate restTemplate;
    private RestUser user;

    @LocalServerPort
    private int port;

    @BeforeEach
    void createUser() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(restTemplate, "testPassword123");
    }

    @Test
    @DisplayName("Plain text under the limit reports html=false and the exact remaining character budget")
    void plainTextUnderTheLimitIsNotOverLimit() throws Exception {
        final String note = "A short plain text note.";

        final JsonNode body = validate(note);

        assertEquals(note.length(), body.path("rawLength").asInt(), "rawLength must be the submitted length");
        assertEquals(note.length(), body.path("textLength").asInt(), "Plain text textLength must equal rawLength");
        assertFalse(body.path("html").asBoolean(), "Plain text must not be reported as HTML");
        assertFalse(body.path("overLimit").asBoolean(), "A short note must not be over the limit");
        assertEquals(MAX_NOTE_LENGTH - note.length(), body.path("remainingChars").asInt(),
                "remainingChars must be the limit minus the text length");
    }

    @Test
    @DisplayName("HTML content reports html=true and a textLength with the markup stripped")
    void htmlContentIsCountedOnItsTextOnly() throws Exception {
        final String note = "<p>Hello <strong>world</strong></p>";

        final JsonNode body = validate(note);

        assertTrue(body.path("html").asBoolean(), "HTML markup must be detected");
        assertEquals(note.length(), body.path("rawLength").asInt(), "rawLength must include the markup");

        final int textLength = body.path("textLength").asInt();
        assertTrue(textLength < note.length(),
                "textLength must exclude the markup, got raw=" + note.length() + " text=" + textLength);
        assertFalse(body.path("overLimit").asBoolean(), "A short HTML note must not be over the limit");
        assertEquals(MAX_NOTE_LENGTH - textLength, body.path("remainingChars").asInt(),
                "remainingChars must be computed from the text length, not the raw length");
    }

    @Test
    @DisplayName("Content past the limit reports overLimit=true with a negative remaining budget")
    void contentOverTheLimitIsFlagged() throws Exception {
        final int overage = 25;
        final String note = "a".repeat(MAX_NOTE_LENGTH + overage);

        final JsonNode body = validate(note);

        assertEquals(MAX_NOTE_LENGTH + overage, body.path("textLength").asInt(),
                "textLength must be the submitted length");
        assertTrue(body.path("overLimit").asBoolean(), "A note past the limit must be flagged as over limit");
        assertEquals(-overage, body.path("remainingChars").asInt(),
                "remainingChars must go negative by the overage");
        assertTrue(body.path("usagePercentage").asDouble() > 100.0,
                "usagePercentage must exceed 100, got " + body.path("usagePercentage").asDouble());
    }

    @Test
    @DisplayName("Blank content reports a zero length and the full remaining character budget")
    void blankContentReportsTheFullBudget() throws Exception {
        final JsonNode body = validate("   ");

        assertEquals(0, body.path("rawLength").asInt(), "Blank content must report a zero raw length");
        assertEquals(0, body.path("textLength").asInt(), "Blank content must report a zero text length");
        assertFalse(body.path("html").asBoolean(), "Blank content must not be reported as HTML");
        assertFalse(body.path("overLimit").asBoolean(), "Blank content must not be over the limit");
        assertEquals(MAX_NOTE_LENGTH, body.path("remainingChars").asInt(),
                "Blank content must leave the whole budget");
    }

    @Test
    @DisplayName("validate-note requires authentication")
    void validateNoteRejectsAnonymousCallers() {
        final ResponseEntity<String> response = restTemplate.exchange(VALIDATE_NOTE_URL, HttpMethod.POST,
                new HttpEntity<>("some note", plainTextRequestHeaders()), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "An unauthenticated call must be rejected with 401");
    }

    @NotNull
    private JsonNode validate(@NotNull String noteContent) throws Exception {
        final ResponseEntity<String> response = restTemplate
                .withBasicAuth(user.getEmail(), user.getPassword())
                .exchange(VALIDATE_NOTE_URL, HttpMethod.POST,
                        new HttpEntity<>(noteContent, plainTextRequestHeaders()), String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful(), "validate-note should succeed: " + response);
        return objectMapper.readTree(Objects.requireNonNull(response.getBody(), "A body is expected"));
    }

    @NotNull
    private static HttpHeaders plainTextRequestHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
