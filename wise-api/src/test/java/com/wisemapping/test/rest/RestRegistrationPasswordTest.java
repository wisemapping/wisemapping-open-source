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

import com.wisemapping.config.AppConfig;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the registration password checks. The controller used to dereference the
 * password before running the validator, so a payload without a {@code password} field blew up with
 * a {@code NullPointerException} and answered 500.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Registration Password Validation Tests")
class RestRegistrationPasswordTest {

    private static final Logger logger = LoggerFactory.getLogger(RestRegistrationPasswordTest.class);

    /** Rendering of the validator's {@code FIELD_REQUIRED} rejection. */
    private static final String FIELD_REQUIRED_MESSAGE = "Required field cannot be left blank";

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
    }

    @Test
    @DisplayName("A registration without a password field should not answer a server error")
    void shouldNotFailWithServerErrorOnMissingPassword() {
        final ResponseEntity<String> response = register("""
                {"email":"no-password-%d@example.org","firstname":"No","lastname":"Password","acceptedTerms":true}
                """.formatted(System.nanoTime()));

        logger.debug("Registration without a password answered {}", response.getStatusCode());

        assertFalse(response.getStatusCode().is5xxServerError(),
                "A missing password must not be a server error. Got " + response.getStatusCode()
                        + ". Body: " + response.getBody());
        assertTrue(response.getStatusCode().is4xxClientError(),
                "A missing password must be a client error. Got " + response.getStatusCode()
                        + ". Body: " + response.getBody());
    }

    @Test
    @DisplayName("A registration with an explicitly null password should not answer a server error")
    void shouldNotFailWithServerErrorOnNullPassword() {
        final ResponseEntity<String> response = register("""
                {"email":"null-password-%d@example.org","password":null,"firstname":"Null","lastname":"Password","acceptedTerms":true}
                """.formatted(System.nanoTime()));

        assertFalse(response.getStatusCode().is5xxServerError(),
                "A null password must not be a server error. Got " + response.getStatusCode()
                        + ". Body: " + response.getBody());
        assertTrue(response.getStatusCode().is4xxClientError(),
                "A null password must be a client error. Got " + response.getStatusCode()
                        + ". Body: " + response.getBody());
    }

    @Test
    @DisplayName("A too short password should still be rejected by the length check")
    void shouldStillRejectTooShortPassword() {
        final String tooShort = "a".repeat(Account.MIN_PASSWORD_LENGTH_SIZE - 1);
        final ResponseEntity<String> response = register("""
                {"email":"short-password-%d@example.org","password":"%s","firstname":"Short","lastname":"Password","acceptedTerms":true}
                """.formatted(System.nanoTime(), tooShort));

        // Moving verify() ahead of the length checks must not let a short password through, and the
        // rejection must still come from the length check rather than the validator's blank-field rule.
        assertFalse(response.getStatusCode().is2xxSuccessful(),
                "A too short password must be rejected. Got " + response.getStatusCode()
                        + ". Body: " + response.getBody());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().contains(FIELD_REQUIRED_MESSAGE),
                "A non blank short password must not be reported as a blank field. Body: " + response.getBody());
    }

    @Test
    @DisplayName("A too long password should keep its dedicated error message")
    void shouldKeepTooLongPasswordMessage() {
        final String tooLong = "a".repeat(Account.MAX_PASSWORD_LENGTH_SIZE + 1);
        final ResponseEntity<String> response = register("""
                {"email":"long-password-%d@example.org","password":"%s","firstname":"Long","lastname":"Password","acceptedTerms":true}
                """.formatted(System.nanoTime(), tooLong));

        assertTrue(response.getStatusCode().is4xxClientError(),
                "Got " + response.getStatusCode() + ". Body: " + response.getBody());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toLowerCase().contains("less than 40 characters"),
                "The too-long password message must be preserved. Body: " + response.getBody());
    }

    @NotNull
    private ResponseEntity<String> register(@NotNull final String json) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> request = new HttpEntity<>(json, headers);
        return restTemplate.exchange(BASE_REST_URL + "/users", HttpMethod.POST, request, String.class);
    }
}
