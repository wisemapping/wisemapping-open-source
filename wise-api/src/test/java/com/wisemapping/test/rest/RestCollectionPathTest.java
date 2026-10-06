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
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.rest.model.RestUserRegistration;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the collection endpoints that used to be reachable only through their
 * trailing-slash form ({@code /labels/}, {@code /maps/}, {@code /users/}). Both the canonical
 * no-slash path and the legacy slashed path must answer.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Collection Endpoint Path Tests")
class RestCollectionPathTest {

    private static final Logger logger = LoggerFactory.getLogger(RestCollectionPathTest.class);

    private static final String USER_PASSWORD = "testPassword123";

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private RestUser user;

    @BeforeEach
    void setUp() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(this.restTemplate, USER_PASSWORD);
    }

    @Test
    @DisplayName("Labels list should answer on both the no-slash and the slashed path")
    void shouldListLabelsOnBothPaths() {
        assertCollectionIsListable("/labels");
    }

    @Test
    @DisplayName("Maps list should answer on both the no-slash and the slashed path")
    void shouldListMapsOnBothPaths() {
        assertCollectionIsListable("/maps");
    }

    @Test
    @DisplayName("Anonymous registration should be permitted on the no-slash path")
    void shouldRegisterOnNoSlashPath() {
        final ResponseEntity<String> response = register("/users");

        // The security filter chain matchers are exact paths: a missing no-slash permitAll entry
        // would make this fall through to the authenticated catch-all and answer 401.
        assertNotEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Anonymous registration must not require authentication on the no-slash path");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Body: " + response.getBody());
    }

    @Test
    @DisplayName("Anonymous registration should keep working on the legacy slashed path")
    void shouldRegisterOnSlashedPath() {
        final ResponseEntity<String> response = register("/users/");

        assertNotEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Anonymous registration must not require authentication on the slashed path");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Body: " + response.getBody());
    }

    private void assertCollectionIsListable(@NotNull final String path) {
        final TestRestTemplate authenticated = this.restTemplate.withBasicAuth(user.getEmail(), USER_PASSWORD);
        final HttpEntity<Void> request = new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON));

        for (final String candidate : new String[]{path, path + "/"}) {
            final ResponseEntity<String> response = authenticated.exchange(
                    BASE_REST_URL + candidate, HttpMethod.GET, request, String.class);
            logger.debug("GET {} answered {}", candidate, response.getStatusCode());
            assertEquals(HttpStatus.OK, response.getStatusCode(),
                    "GET " + candidate + " must answer 200. Body: " + response.getBody());
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }
    }

    @NotNull
    private ResponseEntity<String> register(@NotNull final String path) {
        final RestUserRegistration registration = RestUserRegistration.create(
                "path-test-" + System.nanoTime() + "@example.org", "registerPassword123",
                "Path", "Test", true);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<RestUserRegistration> request = new HttpEntity<>(registration, headers);

        return restTemplate.exchange(BASE_REST_URL + path, HttpMethod.POST, request, String.class);
    }
}
