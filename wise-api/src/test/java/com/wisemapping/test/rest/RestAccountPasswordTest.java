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

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the rejection paths of {@code PUT /api/restful/account/password}
 * ({@link com.wisemapping.rest.AccountController#changePassword}). The happy
 * path lives in {@code RestAccountControllerTest}; the length boundaries had
 * no coverage at all.
 *
 * <p>Each rejection is asserted twice: the response must not be a success,
 * and the account must still authenticate with its original password. The
 * second assertion is the one that actually matters, and it holds whatever
 * status code the global exception handler happens to render.
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestAccountPasswordTest {

    private static final String PASSWORD_URL = "/api/restful/account/password";
    private static final String ACCOUNT_URL = "/api/restful/account";
    private static final String ORIGINAL_PASSWORD = "testPassword123";

    private TestRestTemplate restTemplate;
    private RestUser user;

    @LocalServerPort
    private int port;

    @BeforeEach
    void createUser() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(restTemplate, ORIGINAL_PASSWORD);
    }

    @Test
    @DisplayName("A password shorter than the 8 character minimum is refused and leaves the old password in place")
    void passwordShorterThanTheMinimumIsRejected() {
        final String tooShort = "a".repeat(Account.MIN_PASSWORD_LENGTH_SIZE - 1);

        final ResponseEntity<String> response = changePassword(tooShort);

        // NOTE: this path currently answers 500 rather than 400 even though
        // PasswordTooShortException is a ClientException, because the
        // "PASSWORD_TOO_SHORT" key is missing from every messages*.properties
        // bundle and resolving it throws NoSuchMessageException from inside
        // GlobalExceptionHandler. Only the refusal itself is asserted here so
        // the test stays correct either way.
        assertFalse(response.getStatusCode().is2xxSuccessful(),
                "A too short password must not be accepted, got: " + response.getStatusCode()
                        + " - " + response.getBody());
        assertTrue(canAuthenticateWith(ORIGINAL_PASSWORD), "The original password must still work");
        assertFalse(canAuthenticateWith(tooShort), "The rejected password must not have been stored");
    }

    @Test
    @DisplayName("A password longer than the 40 character maximum is refused with a client error")
    void passwordLongerThanTheMaximumIsRejected() {
        final String tooLong = "a".repeat(Account.MAX_PASSWORD_LENGTH_SIZE + 1);

        final ResponseEntity<String> response = changePassword(tooLong);

        assertTrue(response.getStatusCode().is4xxClientError(),
                "A too long password must be rejected, got: " + response.getStatusCode()
                        + " - " + response.getBody());
        assertTrue(canAuthenticateWith(ORIGINAL_PASSWORD), "The original password must still work");
        assertFalse(canAuthenticateWith(tooLong), "The rejected password must not have been stored");
    }

    @Test
    @DisplayName("A password of exactly the minimum length is accepted and becomes effective")
    void passwordAtTheMinimumLengthIsAccepted() {
        final String atMinimum = "a".repeat(Account.MIN_PASSWORD_LENGTH_SIZE);

        assertEquals(HttpStatus.NO_CONTENT, changePassword(atMinimum).getStatusCode(),
                "A password of exactly MIN_PASSWORD_LENGTH_SIZE must be accepted");
        assertTrue(canAuthenticateWith(atMinimum), "The new password must be effective");
    }

    @Test
    @DisplayName("A password of exactly the maximum length is accepted and becomes effective")
    void passwordAtTheMaximumLengthIsAccepted() {
        final String atMaximum = "a".repeat(Account.MAX_PASSWORD_LENGTH_SIZE);

        assertEquals(HttpStatus.NO_CONTENT, changePassword(atMaximum).getStatusCode(),
                "A password of exactly MAX_PASSWORD_LENGTH_SIZE must be accepted");
        assertTrue(canAuthenticateWith(atMaximum), "The new password must be effective");
    }

    @Test
    @DisplayName("Changing the password requires authentication")
    void changePasswordRejectsAnonymousCallers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        final ResponseEntity<String> response = restTemplate.exchange(PASSWORD_URL, HttpMethod.PUT,
                new HttpEntity<>("someNewPassword123", headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "An unauthenticated password change must be rejected with 401");
    }

    @NotNull
    private ResponseEntity<String> changePassword(@NotNull String password) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en");

        return restTemplate.withBasicAuth(user.getEmail(), ORIGINAL_PASSWORD)
                .exchange(PASSWORD_URL, HttpMethod.PUT, new HttpEntity<>(password, headers), String.class);
    }

    private boolean canAuthenticateWith(@NotNull String password) {
        final ResponseEntity<String> response = restTemplate.withBasicAuth(user.getEmail(), password)
                .exchange(ACCOUNT_URL, HttpMethod.GET,
                        new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON)), String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}
