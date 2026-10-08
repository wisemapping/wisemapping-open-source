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
import com.wisemapping.rest.model.RestCollaboration;
import com.wisemapping.rest.model.RestCollaborationList;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the {@code ?q=} parameter of {@code GET /api/restful/maps/}, which is
 * backed by {@link com.wisemapping.rest.MindmapFilter}. Every supported filter
 * value is exercised through a real HTTP call with fixtures that make the
 * filter discriminate (so a filter that returned everything would fail).
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestMindmapFilterTest {

    private static final String PASSWORD = "testPassword123";

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
    @DisplayName("q=my_maps returns only maps the user created, excluding maps shared with the user")
    void myMapsFilterReturnsOnlyOwnedMaps() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String ownTitle = "Filter own map " + System.nanoTime();
        addNewMap(ownerTemplate, ownTitle);

        final String sharedTitle = shareMapFromAnotherUserWithTestUser();

        final List<String> titles = titlesOf(fetchMaps(ownerTemplate, "my_maps"));
        assertTrue(titles.contains(ownTitle), "my_maps must include a map created by the user. Got: " + titles);
        assertFalse(titles.contains(sharedTitle),
                "my_maps must not include a map created by somebody else. Got: " + titles);
    }

    @Test
    @DisplayName("q=shared_with_me returns only maps created by somebody else, excluding the user's own maps")
    void sharedWithMeFilterReturnsOnlyMapsOwnedByOthers() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String ownTitle = "Filter not shared map " + System.nanoTime();
        addNewMap(ownerTemplate, ownTitle);

        final String sharedTitle = shareMapFromAnotherUserWithTestUser();

        final List<String> titles = titlesOf(fetchMaps(ownerTemplate, "shared_with_me"));
        assertTrue(titles.contains(sharedTitle),
                "shared_with_me must include a map another user shared with the test user. Got: " + titles);
        assertFalse(titles.contains(ownTitle),
                "shared_with_me must not include the user's own maps. Got: " + titles);
    }

    @Test
    @DisplayName("q=starred returns only maps the user has starred")
    void starredFilterReturnsOnlyStarredMaps() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String starredTitle = "Filter starred map " + System.nanoTime();
        final URI starredUri = addNewMap(ownerTemplate, starredTitle);

        final String plainTitle = "Filter unstarred map " + System.nanoTime();
        addNewMap(ownerTemplate, plainTitle);

        final HttpHeaders textHeaders = new HttpHeaders();
        textHeaders.setContentType(MediaType.TEXT_PLAIN);
        final ResponseEntity<String> starResponse = ownerTemplate.exchange(starredUri + "/starred",
                HttpMethod.PUT, new HttpEntity<>("true", textHeaders), String.class);
        assertTrue(starResponse.getStatusCode().is2xxSuccessful(), "Starring the map should succeed");

        final List<String> titles = titlesOf(fetchMaps(ownerTemplate, "starred"));
        assertTrue(titles.contains(starredTitle), "starred must include the starred map. Got: " + titles);
        assertFalse(titles.contains(plainTitle), "starred must not include an unstarred map. Got: " + titles);
    }

    @Test
    @DisplayName("q=public returns only maps that have been published")
    void publicFilterReturnsOnlyPublishedMaps() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String publicTitle = "Filter public map " + System.nanoTime();
        final URI publicUri = addNewMap(ownerTemplate, publicTitle);

        final String privateTitle = "Filter private map " + System.nanoTime();
        addNewMap(ownerTemplate, privateTitle);

        final HttpHeaders jsonHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final ResponseEntity<String> publishResponse = ownerTemplate.exchange(publicUri + "/publish",
                HttpMethod.PUT, new HttpEntity<>("{\"isPublic\":true}", jsonHeaders), String.class);
        assertTrue(publishResponse.getStatusCode().is2xxSuccessful(),
                "Publishing the map should succeed, got: " + publishResponse.getStatusCode()
                        + " - " + publishResponse.getBody());

        final List<String> titles = titlesOf(fetchMaps(ownerTemplate, "public"));
        assertTrue(titles.contains(publicTitle), "public must include the published map. Got: " + titles);
        assertFalse(titles.contains(privateTitle), "public must not include a private map. Got: " + titles);
    }

    @Test
    @DisplayName("An unrecognised q value is treated as a label name and returns only maps carrying that label")
    void unknownFilterValueIsInterpretedAsALabelName() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String labelName = "filter-label-" + System.nanoTime();
        final int labelId = createLabel(ownerTemplate, labelName);

        final String labelledTitle = "Filter labelled map " + System.nanoTime();
        final URI labelledUri = addNewMap(ownerTemplate, labelledTitle);

        final String unlabelledTitle = "Filter unlabelled map " + System.nanoTime();
        addNewMap(ownerTemplate, unlabelledTitle);

        final HttpHeaders jsonHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final ResponseEntity<String> linkResponse = ownerTemplate.exchange(labelledUri + "/labels",
                HttpMethod.POST, new HttpEntity<>(String.valueOf(labelId), jsonHeaders), String.class);
        assertTrue(linkResponse.getStatusCode().is2xxSuccessful(),
                "Linking the label to the map should succeed, got: " + linkResponse.getStatusCode());

        final List<String> titles = titlesOf(fetchMaps(ownerTemplate, labelName));
        assertTrue(titles.contains(labelledTitle),
                "The label filter must include the map carrying the label. Got: " + titles);
        assertFalse(titles.contains(unlabelledTitle),
                "The label filter must not include a map without the label. Got: " + titles);
    }

    @Test
    @DisplayName("A misspelled filter such as q=my_mpas is silently treated as a label filter and returns no maps")
    void misspelledFilterValueSilentlyReturnsAnEmptyListInsteadOfFailing() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);
        addNewMap(ownerTemplate, "Filter typo map " + System.nanoTime());

        // Sanity check: the map really is reachable through the default filter.
        assertFalse(titlesOf(fetchMaps(ownerTemplate, null)).isEmpty(), "The user should own at least one map");

        // "my_mpas" is a typo of "my_maps". It is NOT rejected: MindmapFilter.parse
        // falls back to a LabelFilter, and because no label is named "my_mpas" the
        // endpoint answers 200 with an empty list rather than a validation error.
        final RestMindmapList response = fetchMaps(ownerTemplate, "my_mpas");
        assertTrue(titlesOf(response).isEmpty(),
                "A misspelled filter is a label filter that matches nothing. Got: " + titlesOf(response));
    }

    @Test
    @DisplayName("q=all is equivalent to omitting q and returns both owned and shared maps")
    void allFilterReturnsEveryAccessibleMap() throws URISyntaxException {
        final TestRestTemplate ownerTemplate = authenticated(user);

        final String ownTitle = "Filter all own map " + System.nanoTime();
        addNewMap(ownerTemplate, ownTitle);
        final String sharedTitle = shareMapFromAnotherUserWithTestUser();

        final List<String> explicitAll = titlesOf(fetchMaps(ownerTemplate, "all"));
        assertTrue(explicitAll.contains(ownTitle), "q=all must include owned maps. Got: " + explicitAll);
        assertTrue(explicitAll.contains(sharedTitle), "q=all must include shared maps. Got: " + explicitAll);

        final List<String> noFilter = titlesOf(fetchMaps(ownerTemplate, null));
        assertTrue(noFilter.containsAll(explicitAll), "Omitting q must behave like q=all. Got: " + noFilter);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    @NotNull
    private TestRestTemplate authenticated(@NotNull RestUser account) {
        return this.restTemplate.withBasicAuth(account.getEmail(), account.getPassword());
    }

    @NotNull
    private RestMindmapList fetchMaps(@NotNull TestRestTemplate template, @Nullable String query) {
        final HttpEntity<Void> entity = new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON));
        final String url = query == null ? "/api/restful/maps/" : "/api/restful/maps/?q=" + query;
        final ResponseEntity<RestMindmapList> response = template.exchange(url, HttpMethod.GET, entity,
                RestMindmapList.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Map listing should succeed: " + response);
        return Objects.requireNonNull(response.getBody());
    }

    @NotNull
    private List<String> titlesOf(@NotNull RestMindmapList list) {
        final List<RestMindmapInfo> infos = list.getMindmapsInfo();
        return infos == null ? List.of() : infos.stream().map(RestMindmapInfo::getTitle).toList();
    }

    /**
     * Creates a second account, has it create a map and share it with the test
     * user as editor. Returns the title of the shared map.
     */
    @NotNull
    private String shareMapFromAnotherUserWithTestUser() throws URISyntaxException {
        final RestUser otherUser = createTestUser(restTemplate, PASSWORD);
        final TestRestTemplate otherTemplate = authenticated(otherUser);

        final String sharedTitle = "Filter shared map " + System.nanoTime();
        final URI sharedUri = addNewMap(otherTemplate, sharedTitle);

        final HttpHeaders jsonHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Sharing for the shared_with_me filter");
        final RestCollaboration collaboration = new RestCollaboration();
        collaboration.setEmail(user.getEmail());
        collaboration.setRole("editor");
        collabs.addCollaboration(collaboration);

        final ResponseEntity<String> response = otherTemplate.exchange(sharedUri + "/collabs/", HttpMethod.PUT,
                new HttpEntity<>(collabs, jsonHeaders), String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(),
                "Sharing the map should succeed, got: " + response.getStatusCode() + " - " + response.getBody());
        return sharedTitle;
    }

    private int createLabel(@NotNull TestRestTemplate template, @NotNull String title) {
        final HttpHeaders jsonHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final RestLabel label = new RestLabel();
        label.setTitle(title);
        label.setColor("#000000");

        final ResponseEntity<String> response = template.exchange("/api/restful/labels", HttpMethod.POST,
                new HttpEntity<>(label, jsonHeaders), String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Label creation should succeed: " + response);

        final URI location = response.getHeaders().getLocation();
        assertNotNull(location, "Label creation must return a Location header");
        final String path = location.getPath();
        return Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
    }

    @NotNull
    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) throws URISyntaxException {
        final HttpHeaders xmlHeaders = createHeaders(MediaType.APPLICATION_XML);
        final ResponseEntity<String> response = template.exchange("/api/restful/maps?title=" + title,
                HttpMethod.POST, new HttpEntity<String>(null, xmlHeaders), String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Map creation should succeed: " + response);

        final List<String> locations = response.getHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(locations, "Map creation must return a Location header");
        return new URI(locations.get(0));
    }
}
