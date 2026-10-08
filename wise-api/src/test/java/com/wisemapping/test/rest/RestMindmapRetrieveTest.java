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

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@code GET /api/restful/maps/{id}}
 * ({@link com.wisemapping.rest.MindmapController#retrieve}), the single-map
 * {@code RestMindmap} JSON fetch. The sibling {@code /metadata} and
 * {@code /document/xml} projections were already covered; this one was not.
 *
 * <p>Assertions are made against the raw JSON rather than a deserialized
 * {@code RestMindmap}, because several of that DTO's setters are no-ops and a
 * round-trip through it would silently drop the fields under test.
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestMindmapRetrieveTest {

    private static final String PASSWORD = "testPassword123";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TestRestTemplate restTemplate;
    private RestUser user;

    @LocalServerPort
    private int port;

    @BeforeEach
    void createUser() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(restTemplate, PASSWORD);
    }

    @Test
    @DisplayName("The owner retrieves a map as JSON carrying its id, title, creator, owner and xml")
    void ownerRetrievesTheFullMindmapRepresentation() throws Exception {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String title = "Retrieve map " + System.nanoTime();
        final URI mapUri = addNewMap(ownerTemplate, title);

        final ResponseEntity<String> response = get(ownerTemplate, mapUri);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Retrieving an owned map must succeed");

        final JsonNode body = objectMapper.readTree(Objects.requireNonNull(response.getBody()));
        assertEquals(title, body.path("title").asText(), "The title must round-trip");
        assertTrue(body.path("id").asInt() > 0, "The map id must be populated");
        assertEquals(user.getEmail(), body.path("creator").asText(), "creator must be the account that created it");
        assertEquals(user.getEmail(), body.path("owner").asText(), "owner must be the account that created it");
        assertFalse(body.path("public").asBoolean(), "A freshly created map must not be public");
        assertFalse(body.path("starred").asBoolean(), "A freshly created map must not be starred");
        assertTrue(body.path("xml").asText().contains("<map"),
                "The representation must embed the mindmap xml, got: " + body.path("xml").asText());
    }

    @Test
    @DisplayName("A map shared as viewer is retrievable by the collaborator with the owner still reported as creator")
    void collaboratorRetrievesASharedMap() throws Exception {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String title = "Retrieve shared map " + System.nanoTime();
        final URI mapUri = addNewMap(ownerTemplate, title);

        final RestUser collaborator = createTestUser(restTemplate, PASSWORD);
        final String payload = "{\"message\":\"shared for retrieve\",\"collaborations\":[{\"email\":\""
                + collaborator.getEmail() + "\",\"role\":\"viewer\"}]}";
        final ResponseEntity<String> shareResponse = ownerTemplate.exchange(mapUri + "/collabs/", HttpMethod.PUT,
                new HttpEntity<>(payload, createHeaders(MediaType.APPLICATION_JSON)), String.class);
        assertTrue(shareResponse.getStatusCode().is2xxSuccessful(),
                "Sharing the map should succeed: " + shareResponse.getStatusCode() + " - " + shareResponse.getBody());

        final ResponseEntity<String> response = get(authenticated(collaborator), mapUri);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "A viewer must be able to retrieve the map");

        final JsonNode body = objectMapper.readTree(Objects.requireNonNull(response.getBody()));
        assertEquals(title, body.path("title").asText(), "The viewer must see the same title");
        assertEquals(user.getEmail(), body.path("creator").asText(), "The creator must remain the owner");
    }

    @Test
    @DisplayName("Retrieving a map the caller has no collaboration on is refused")
    void retrievingSomebodyElsesMapIsRefused() throws Exception {
        final URI mapUri = addNewMap(authenticated(user), "Retrieve private map " + System.nanoTime());

        final RestUser stranger = createTestUser(restTemplate, PASSWORD);
        final ResponseEntity<String> response = get(authenticated(stranger), mapUri);

        assertTrue(response.getStatusCode().is4xxClientError(),
                "A stranger must not be able to retrieve the map, got: " + response.getStatusCode());
    }

    @Test
    @DisplayName("Retrieving a map requires authentication, even once the map has been published")
    void retrievingAMapAnonymouslyIsRefused() throws Exception {
        final TestRestTemplate ownerTemplate = authenticated(user);
        final URI mapUri = addNewMap(ownerTemplate, "Retrieve anon map " + System.nanoTime());

        ownerTemplate.exchange(mapUri + "/publish", HttpMethod.PUT,
                new HttpEntity<>("{\"isPublic\":true}", createHeaders(MediaType.APPLICATION_JSON)), String.class);

        final ResponseEntity<String> response = get(restTemplate, mapUri);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Unlike /metadata, the RestMindmap projection is not anonymous-readable");
    }

    @NotNull
    private ResponseEntity<String> get(@NotNull TestRestTemplate template, @NotNull URI mapUri) {
        return template.exchange(mapUri.toString(), HttpMethod.GET,
                new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON)), String.class);
    }

    @NotNull
    private TestRestTemplate authenticated(@NotNull RestUser account) {
        return this.restTemplate.withBasicAuth(account.getEmail(), account.getPassword());
    }

    @NotNull
    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) throws Exception {
        final HttpHeaders xmlHeaders = createHeaders(MediaType.APPLICATION_XML);
        final ResponseEntity<String> response = template.exchange("/api/restful/maps?title=" + title,
                HttpMethod.POST, new HttpEntity<String>(null, xmlHeaders), String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Map creation should succeed: " + response);

        final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(locations, "Map creation must return a Location header");
        return new URI(locations.get(0));
    }
}
