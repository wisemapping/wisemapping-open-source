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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for {@code DELETE /api/restful/maps/batch}. A malformed identifier list is a
 * client error and must not be reported as a revoked-permission failure, and genuine server faults
 * must no longer be masked by a blanket catch.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Batch Delete Tests")
class RestBatchDeleteTest {

    private static final Logger logger = LoggerFactory.getLogger(RestBatchDeleteTest.class);

    private static final String USER_PASSWORD = "testPassword123";

    /**
     * The message rendered for {@code AccessDeniedSecurityException}. A malformed id must never
     * produce it.
     */
    private static final String PERMISSION_REVOKED_FRAGMENT = "access permissions to this map has been revoked";

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;
    private RestUser user;

    @BeforeEach
    void setUp() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(new TestRestTemplate("http://localhost:" + port + "/"), USER_PASSWORD);
        this.restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), USER_PASSWORD);
    }

    @Test
    @DisplayName("Non numeric map id should be a client error, not a permission error")
    void shouldRejectNonNumericMapId() {
        final ResponseEntity<String> response = batchDelete("abc");
        logger.debug("Batch delete with a non numeric id answered {}", response.getStatusCode());

        assertMalformedIdRejected(response);
    }

    @Test
    @DisplayName("Mixed valid and non numeric map ids should be a client error, not a permission error")
    void shouldRejectMixedMapIds() throws URISyntaxException {
        final int mapId = createMap("Batch Delete Mixed " + System.nanoTime());

        final ResponseEntity<String> response = batchDelete(mapId + ",abc");

        assertMalformedIdRejected(response);

        // Nothing should have been deleted: validation runs before any removal.
        final ResponseEntity<String> fetch = restTemplate.exchange(
                BASE_REST_URL + "/maps/" + mapId, HttpMethod.GET,
                new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON)), String.class);
        assertEquals(HttpStatus.OK, fetch.getStatusCode(),
                "The map must survive a rejected batch delete. Body: " + fetch.getBody());
    }

    @Test
    @DisplayName("Empty map id list should be a client error, not a permission error")
    void shouldRejectEmptyMapIdList() {
        assertMalformedIdRejected(batchDelete(""));
        assertMalformedIdRejected(batchDelete("1,,2"));
    }

    @Test
    @DisplayName("Valid map ids should still be deleted")
    void shouldDeleteValidMapIds() throws URISyntaxException {
        final int first = createMap("Batch Delete Valid A " + System.nanoTime());
        final int second = createMap("Batch Delete Valid B " + System.nanoTime());

        final ResponseEntity<String> response = batchDelete(first + "," + second);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Body: " + response.getBody());

        for (final int mapId : List.of(first, second)) {
            final ResponseEntity<String> fetch = restTemplate.exchange(
                    BASE_REST_URL + "/maps/" + mapId, HttpMethod.GET,
                    new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON)), String.class);
            assertFalse(fetch.getStatusCode().is2xxSuccessful(), "Map " + mapId + " should have been deleted");
        }
    }

    private void assertMalformedIdRejected(@NotNull final ResponseEntity<String> response) {
        // The precise status code for malformed input is owned by the global exception handler;
        // what matters here is that it is a client error and never a permission complaint.
        assertTrue(response.getStatusCode().is4xxClientError(),
                "A malformed id must answer a 4xx. Got " + response.getStatusCode() + ". Body: " + response.getBody());
        assertFalse(response.getStatusCode().is5xxServerError(),
                "A malformed id must not answer a 5xx. Body: " + response.getBody());
        assertNotEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value(),
                "A malformed id must not be reported as a permission failure. Body: " + response.getBody());

        final String body = response.getBody();
        if (body != null) {
            assertFalse(body.contains(PERMISSION_REVOKED_FRAGMENT),
                    "A malformed id must not render the revoked-permission message. Body: " + body);
        }
    }

    @NotNull
    private ResponseEntity<String> batchDelete(@Nullable final String ids) {
        final HttpEntity<Void> request = new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON));
        return restTemplate.exchange(BASE_REST_URL + "/maps/batch?ids=" + (ids == null ? "" : ids),
                HttpMethod.DELETE, request, String.class);
    }

    private int createMap(@NotNull final String title) throws URISyntaxException {
        final HttpHeaders headers = createHeaders(MediaType.APPLICATION_XML);
        final HttpEntity<String> request = new HttpEntity<>(null, headers);

        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/maps?title=" + title, HttpMethod.POST, request, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Could not create map. Body: " + response.getBody());

        final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(locations, "Location header missing on map creation");
        final URI location = new URI(locations.get(0));
        final String path = location.getPath();
        return Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
    }
}
