package com.wisemapping.test.rest;


import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.rest.AdminController;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {RestAppConfig.class, CommonConfig.class, MindmapController.class, AdminController.class, UserController.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.api.http-basic-enabled=true"})
public class RestMindmapControllerTest {

    private RestUser user;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void createUser() {

        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081/"));
        }

        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        this.user = restAccount.createNewUser();
    }

    @Test
    public void listMaps() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "List Maps 1";
        addNewMap(restTemplate, title1);

        final String title2 = "List Maps 2";
        addNewMap(restTemplate, title2);

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, restTemplate);
        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();

        boolean found1 = false;
        boolean found2 = false;
        for (RestMindmapInfo mindmap : mindmaps) {
            if (mindmap.getTitle().equals(title1)) {
                found1 = true;
            }
            if (mindmap.getTitle().equals(title2)) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2, "Map could not be found");
    }

    @Test
    public void deleteMap() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Map to delete";
        final URI resourceUri = addNewMap(restTemplate, title1);

        // Now remove it ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri.toString(), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), "Status code:" + exchange.getStatusCode());

        // Check that has been removed ...
        try {
            findMap(requestHeaders, restTemplate, resourceUri);
            fail("Map could not be removed:" + resourceUri);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void changeMapTitle() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change title");
        final String newTitle = changeMapTitle(requestHeaders, MediaType.APPLICATION_JSON, restTemplate, resourceUri);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(newTitle, map.getTitle());
    }

    @Test
    public void validateMapsCreation() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        requestHeaders.set(HttpHeaders.ACCEPT_LANGUAGE, "en");
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Map to Validate Creation";
        addNewMap(restTemplate, title);

        // Add map with same name ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<String> response = restTemplate.exchange("/api/restful/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("You have already a map with the same name"));
    }


    @Test
    public void changeMapDescription() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change Description ");

        // Change map title ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newDescription = "New map to change description ";
        final HttpEntity<String> updateEntity = new HttpEntity<>(newDescription, requestHeaders);
        restTemplate.put(resourceUri + "/description", updateEntity);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(newDescription, map.getDescription());
    }


    @Test
    public void updateMapXml() throws IOException, URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Update XML sample";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Update map xml content ...
        final String resourceUrl = resourceUri.toString();
        String newXmlContent = updateMapDocument(requestHeaders, restTemplate, resourceUrl, null);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, restTemplate, resourceUri);
        assertEquals(response.getXml(), newXmlContent);
    }

    private String updateMapDocument(final HttpHeaders requestHeaders, final TestRestTemplate template, final String resourceUrl, final String content) throws RestClientException {
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);

        final String newXmlContent = content != null ? content : "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<>(newXmlContent, requestHeaders);
        template.put(resourceUrl + "/document/xml", updateEntity);
        return newXmlContent;
    }


    @Test
    public void cloneMap() throws IOException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Map to clone  sample ";
        final String xml = "<map><node text='this is a cloned map'></map>";
        final URI newMapUri = addNewMap(restTemplate, title, xml);

        // Clone map ...
        final RestMindmapInfo restMindmap = new RestMindmapInfo();
        restMindmap.setTitle("Cloned map but with previous content.");
        restMindmap.setDescription("Cloned map desc");

        // Create a new map ...
        final HttpEntity<RestMindmapInfo> cloneEntity = new HttpEntity<>(restMindmap, requestHeaders);
        final ResponseEntity<Void> exchange = restTemplate.exchange(newMapUri.toString(), HttpMethod.POST, cloneEntity, Void.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        URI clonedMapUri = exchange.getHeaders().getLocation();


        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, restTemplate, clonedMapUri);
        assertEquals(response.getXml(), xml);
    }


    @Test
    public void updateStarred() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Stared Map user 1";
        URI mapUri = addNewMap(restTemplate, title1);

        // Update starred ...
        final String resourceUrl = mapUri.toString() + "/starred";
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        final HttpHeaders textContentType = new HttpHeaders();
        textContentType.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> updateEntity = new HttpEntity<>("true", textContentType);
        restTemplate.put(resourceUrl, updateEntity);

        // Has been updated ?.

        final HttpEntity<String> findLabelEntity = new HttpEntity<>(createHeaders(MediaType.TEXT_PLAIN));
        final ResponseEntity<String> response = restTemplate.exchange(resourceUrl, HttpMethod.GET, findLabelEntity, String.class);

        assertTrue(Boolean.parseBoolean(response.getBody()), "Starred has been updated");
    }


    @Test
    public void verifyMapOwnership() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate firstUser = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "verifyMapOwnership Map user 1";
        addNewMap(firstUser, title1);

        //create another user
        final RestUser secondUser = RestAccountControllerTest.create(this.restTemplate).createNewUser();
        final TestRestTemplate secondTemplate = this.restTemplate.withBasicAuth(secondUser.getEmail(), secondUser.getPassword());

        final String title2 = "verifyMapOwnership Map user 2";
        addNewMap(secondTemplate, title2);

        final TestRestTemplate superadminTemplate = this.restTemplate.withBasicAuth("admin@wisemapping.org", "test");
        final ResponseEntity<String> exchange = superadminTemplate.exchange("/api/restful/admin/users/" + secondUser.getId(), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), "Status Code:" + exchange.getStatusCode() + "- " + exchange.getBody());

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, firstUser);
        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();

        final Optional<RestMindmapInfo> any = mindmaps.stream().filter(m -> m.getTitle().equals(title1)).findAny();
        assertTrue(any.isPresent(), "Map could not be found");
    }

    @Test
    public void updateMap() throws IOException, WiseMappingException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Update sample ";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");

        // Create lock ...
        final HttpHeaders lockHeaders = createHeaders(MediaType.APPLICATION_JSON);
        lockHeaders.setContentType(MediaType.TEXT_PLAIN);

        // Update map ...
        final String resourceUrl = resourceUri.toString() + "/document";
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
        restTemplate.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = restTemplate.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        assertEquals(Objects.requireNonNull(response.getBody()).getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());

        // Unlock ...
        HttpEntity<String> lockEntity = new HttpEntity<>("false", lockHeaders);
        restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, lockEntity, RestLockInfo.class);
    }


    @Test
    public void addCollabs() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for addCollabs  - ");

        String newCollab = addNewCollaboration(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        assertEquals(responseCollbs.getCount(), 2);

        final Optional<RestCollaboration> addedCollab = responseCollbs.getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(addedCollab.isPresent());
        assertEquals(addedCollab.get().getRole(), "editor");
    }

    @Test
    public void updateCollabType() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for updateCollabType");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collab@example.com";
        String role = "editor";

        final RestCollaboration collab = addCollabToList(newCollab, role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(responseCollbs.getCount(), 2);

        // Update the collaboration type ...
        collab.setRole("viewer");
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> afterResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        final Optional<RestCollaboration> updatedCollab = Objects.requireNonNull(afterResponse.getBody()).getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(updatedCollab.isPresent());
        assertEquals(updatedCollab.get().getRole(), "viewer");
    }


    @Test
    public void deleteCollabs() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for deleteCollabs  - ");

        String newCollab = addNewCollaboration(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been added ?
        assertEquals(2, responseCollbs.getCount());

        // Now, remove it ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=" + newCollab, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        // Check that it has been removed ...
        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(1, Objects.requireNonNull(afterDeleteResponse.getBody()).getCollaborations().size());
    }

    private String addNewCollaboration(final HttpHeaders requestHeaders, final TestRestTemplate template, final URI resourceUri) throws RestClientException {
        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");
        final String newCollab = "new-collab@example.com";
        String role = "editor";
        addCollabToList(newCollab, role, collabs);
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(resourceUri + "/collabs/", updateEntity);
        return newCollab;
    }


    @Test
    public void deleteCollabsWithInvalidEmail() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "deleteCollabsWithInvalidEmail");

        // Remove with invalid email ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=invalidEmail", HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Invalid email exception:"));

        // Check that it has been removed ...
        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, restTemplate, resourceUri);
        assertEquals(Objects.requireNonNull(afterDeleteResponse.getBody()).getCollaborations().size(), 1);
    }

    @Test
    public void deleteCollabsWithoutOwnerPermission() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final URI resourceUri = addNewMap(restTemplate, "deleteWithoutOwnerPermission");

        // Create a new user ...
        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        this.user = restAccount.createNewUser();

        // Create template ...
        final RestAccountControllerTest accountController = RestAccountControllerTest.create(restTemplate);
        final RestUser anotherUser = accountController.createNewUser();
        final TestRestTemplate anotherTemplate = this.restTemplate.withBasicAuth(anotherUser.getEmail(), anotherUser.getPassword());

        // Try to delete but I'm not the owner ...
        final ResponseEntity<String> exchange = anotherTemplate.exchange(resourceUri + "/collabs?email=" + anotherUser.getEmail(), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("You do not have enough right access to see this map. This map has been changed to private or deleted."));

    }

    @Test
    public void deleteOwnerCollab() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map for deleteOwnerCollab");

        // Now, remove owner collab ...
        final ResponseEntity<String> exchange = restTemplate.exchange(resourceUri + "/collabs?email=" + user.getEmail().replace(":admin", ""), HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is4xxClientError());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Can not remove owner collab"));
    }

    @NotNull
    private ResponseEntity<RestCollaborationList> fetchCollabs(HttpHeaders requestHeaders, TestRestTemplate template, URI resourceUri) {
        final HttpEntity<RestCollaborationList> findCollabs = new HttpEntity<>(requestHeaders);
        return template.exchange(resourceUri + "/collabs", HttpMethod.GET, findCollabs, RestCollaborationList.class);
    }

    @Test
    public void addCollabsInvalidOwner() throws URISyntaxException {

        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...fetchAndGetCollabs(requestHeaders, template, resourceUri);
        final URI resourceUri = addNewMap(restTemplate, "Map for Collaboration");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        // Validate that owner can not be added.
        addCollabToList("newCollab@example", "owner", collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);
    }

    @Test
    public void removeLabelFromMindmap() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new label
        final String titleLabel = "removeLabelFromMindmap";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, restTemplate, titleLabel, "red");

        // Create a sample map ...
        final String mapTitle = "removeLabelFromMindmap";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Assign label to map ...
        String labelId = labelUri.getPath().replace("/api/restful/labels/", "");
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        restTemplate.postForLocation("/api/restful/maps/" + mapId + "/labels", labelEntity);

        // Remove label from map
        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful//maps/" + mapId + "/labels/" + labelId, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());


        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, restTemplate, mapId);
        assertEquals(0, mindmapInfo.get().getLabels().size());
    }


    @NotNull
    private Optional<RestMindmapInfo> fetchMap(HttpHeaders requestHeaders, TestRestTemplate template, @NotNull String mapId) {
        // Check that the label has been removed ...
        final List<RestMindmapInfo> mindmapsInfo = fetchMaps(requestHeaders, template).getMindmapsInfo();
        return mindmapsInfo
                .stream()
                .filter(m -> m.getId() == Integer.parseInt(mapId))
                .findAny();
    }


    @Test
    @Disabled("missing test: delete map should not affects others labels")
    public void deleteMapAndCheckLabels() {    // Configure media types ...
    }

    @Test
    public void addLabelToMindmap() throws URISyntaxException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new label
        final String titleLabel = "Label 1  - ";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, restTemplate, titleLabel, "COLOR");

        // Create a sample map ...
        final String mapTitle = "Maps 1  - ";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Assign label to map ...
        String labelId = labelUri.getPath().replace("/api/restful/labels/", "");
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        restTemplate.postForLocation("/api/restful/maps/" + mapId + "/labels", labelEntity);

        // Check that the label has been assigned ...
        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, restTemplate, mapId);
        assertEquals(1, mindmapInfo.get().getLabels().size());
    }

    @Test
    public void fetchMapMetadata() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "Maps 1 !";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        final ResponseEntity<RestMindmapMetadata> exchange = restTemplate.exchange(mindmapUri + "/metadata", HttpMethod.GET, null, RestMindmapMetadata.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertEquals(mapTitle, exchange.getBody().getTitle());

    }

    @Test
    public void updateCollabs() throws URISyntaxException {

        // Create a sample map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());
        final URI resourceUri = addNewMap(restTemplate, "Map for updateCollabs");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        String newCollab = "new-collab@example.com";
        String role = "editor";

        addCollabToList(newCollab, role, collabs);

        HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        collabs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        //delete one collab
        collabs.setCollaborations(collabs.getCollaborations().stream().filter(c -> c.getRole().equals("owner")).collect(Collectors.toList()));

        //Add another collaborationMediaType
        newCollab = "another-collab@example.com";
        addCollabToList(newCollab, role, collabs);

        //add owner to list
        addCollabToList(user.getEmail().replace(":admin", ""), "owner", collabs);

        updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.postForLocation(resourceUri + "/collabs/", updateEntity);

        final RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, restTemplate, resourceUri);

        // Has been another-collaboration list updated ?
        assertTrue(responseCollbs.getCollaborations().stream().anyMatch(x -> x.getEmail().equals("another-collab@example.com")));
        assertEquals(responseCollbs.getCount(), 2);
    }


    @Test
    public void updateProperties() throws IOException, WiseMappingException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "updateProperties map";
        final URI resourceUri = addNewMap(restTemplate, title);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");
        mapToUpdate.setTitle("new title for map");
        mapToUpdate.setDescription("updated map description");

        // Update map ...
        final String resourceUrl = resourceUri.toString();
        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
        restTemplate.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = restTemplate.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        assertEquals(response.getBody().getTitle(), mapToUpdate.getTitle());
        assertEquals(response.getBody().getDescription(), mapToUpdate.getDescription());
        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
    }


    @Test
    public void batchDelete() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Batch delete map 1";
        addNewMap(restTemplate, title1);

        final String title2 = "Batch delete map 2";
        addNewMap(restTemplate, title2);

        final String maps = fetchMaps(requestHeaders, restTemplate).getMindmapsInfo().stream().map(map -> String.valueOf(map.getId())).collect(Collectors.joining(","));

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/batch?ids=" + maps, HttpMethod.DELETE, null, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), "Status code:" + exchange.getStatusCode() + " - " + exchange.getBody());

        // Validate that the two maps are there ...
        final RestMindmapList body = fetchMaps(requestHeaders, restTemplate);
        assertEquals(0, body.getMindmapsInfo().size());
    }


    @Test
    public void updatePublishStateFailDueToSpam() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishState";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Change map status ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> updateEntity = new HttpEntity<>(Boolean.TRUE.toString(), requestHeaders);

        // Maps was created and try to publish in short period of time, this is considered a spam behavior.
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        assertTrue(exchange.getStatusCode().isError());
    }

    @Test
    public void updatePublishStateWithJson() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJson";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test publishing with JSON content type
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("public", true);

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to publish - this should work with JSON content type
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // The request should be accepted (not 415 error)
        assertTrue(exchange.getStatusCode().is2xxSuccessful() || exchange.getStatusCode().is4xxClientError(), 
                   "Expected 2xx or 4xx status, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
        
        // If it's 4xx, it should be due to spam detection, not content type issues
        if (exchange.getStatusCode().is4xxClientError()) {
            assertTrue(Objects.requireNonNull(exchange.getBody()).contains("spam") || 
                      Objects.requireNonNull(exchange.getBody()).contains("SpamContentException"),
                      "Expected spam-related error, got: " + exchange.getBody());
        }
    }

    @Test
    public void updatePublishStateWithJsonMakePrivate() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJsonMakePrivate";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test making private with JSON content type
        final Map<String, Boolean> publishRequest = new HashMap<>();
        publishRequest.put("public", false);

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to make private - this should always work
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // Making private should always succeed
        assertTrue(exchange.getStatusCode().is2xxSuccessful(), 
                   "Expected 2xx status for making private, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
    }

    @Test
    public void updatePublishStateWithJsonInvalidRequest() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String mapTitle = "updatePublishStateWithJsonInvalidRequest";
        final URI mindmapUri = addNewMap(restTemplate, mapTitle);

        // Test with invalid JSON request (missing "public" field)
        final Map<String, Boolean> publishRequest = new HashMap<>();
        // Intentionally not adding "public" field

        final HttpEntity<Map<String, Boolean>> updateEntity = new HttpEntity<>(publishRequest, requestHeaders);

        // Try to publish with invalid request - this should fail
        final ResponseEntity<String> exchange = restTemplate.exchange(mindmapUri + "/publish", HttpMethod.PUT, updateEntity, String.class);
        
        // Should return 4xx error due to missing "public" field
        assertTrue(exchange.getStatusCode().is4xxClientError(), 
                   "Expected 4xx status for invalid request, got: " + exchange.getStatusCode() + " - " + exchange.getBody());
        assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Map properties can not be null"),
                   "Expected error about missing properties, got: " + exchange.getBody());
    }

    @Test
    public void fetchMapHistory() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "Map to change title");
        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), null);

        //fetch map history
        final HttpEntity<RestMindmapHistoryList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapHistoryList> maps = restTemplate.exchange(resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);
        assertTrue(maps.getStatusCode().is2xxSuccessful(), maps.toString());

        assertEquals(1, Objects.requireNonNull(maps.getBody()).getCount());
    }


    @Test
    public void updateRevertMindmap() throws IOException, URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "map to test revert changes");
        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), "<map><node text='this is an xml to test revert changes service'></map>");

        updateMapDocument(requestHeaders, restTemplate, resourceUri.toString(), "<map><node text='this is an xml with modification to be reverted'></map>");

        //fetch map history
        final HttpEntity<RestMindmapHistoryList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapHistoryList> mapHistories = restTemplate.exchange(resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);

        //aply revert
        final HttpEntity<String> cloneEntity = new HttpEntity<>(requestHeaders);
        restTemplate.postForLocation(resourceUri + "/history/latest", cloneEntity);
        final RestMindmap latestStoredMap = findMap(requestHeaders, restTemplate, resourceUri);
        restTemplate.postForLocation(resourceUri + "/history/" + mapHistories.getBody().getChanges().get(1).getId(), cloneEntity);
        final RestMindmap firstVersionMap = findMap(requestHeaders, restTemplate, resourceUri);

        //verify revert
        assertEquals(firstVersionMap.getXml(), "<map><node text='this is an xml to test revert changes service'></map>");
        assertEquals(latestStoredMap.getXml(), "<map><node text='this is an xml with modification to be reverted'></map>");
    }


    @Test
    public void addCollabWhitoutOwnerPermission() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "MaddCollabWhitoutOwnerPermission");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        final RestUser newCollab = restAccount.createNewUser();
        String role = "editor";

        addCollabToList(newCollab.getEmail(), role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        restTemplate.put(resourceUri + "/collabs/", updateEntity);

        final TestRestTemplate newCollabTemplate = this.restTemplate.withBasicAuth(newCollab.getEmail(), newCollab.getPassword());
        final ResponseEntity<Void> exchange = newCollabTemplate.exchange(resourceUri + "/collabs/", HttpMethod.PUT, updateEntity, Void.class);
        assertTrue(exchange.getStatusCode().is4xxClientError(), exchange.toString());
    }

    @Test
    public void addCollabWhitOwnerRole() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final URI resourceUri = addNewMap(restTemplate, "addCollabWhitOwnerRole");

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collaborator@mail.com";
        String role = "owner";

        addCollabToList(newCollab, role, collabs);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        final ResponseEntity<RestCollaborationList> collabsList = restTemplate.exchange(resourceUri + "/collabs/", HttpMethod.PUT, updateEntity, RestCollaborationList.class);
        assertTrue(collabsList.getStatusCode().is4xxClientError());
    }

    @Test
    public void retrieveDocumentXml() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final String xmlContent = "<map><node text='test document retrieval'></map>";
        final URI resourceUri = addNewMap(restTemplate, "Test Document Retrieval", xmlContent);
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.GET, requestEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
        assertNotNull(exchange.getBody());
        assertTrue(exchange.getBody().contains("test document retrieval"));
    }

    @Test
    public void lockMindmap() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Lock");

        final HttpHeaders lockHeaders = new HttpHeaders();
        lockHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> lockEntity = new HttpEntity<>("true", lockHeaders);
        final ResponseEntity<RestLockInfo> exchange = restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, lockEntity, RestLockInfo.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        final HttpEntity<String> unlockEntity = new HttpEntity<>("false", lockHeaders);
        final ResponseEntity<RestLockInfo> unlockResponse = restTemplate.exchange(resourceUri + "/lock", HttpMethod.PUT, unlockEntity, RestLockInfo.class);
        assertTrue(unlockResponse.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void updateMapXmlWithTextPlain() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Update XML");
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final String xmlContent = "<map><topic central=\"true\" text=\"Updated via XML\"></topic></map>";
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> updateEntity = new HttpEntity<>(xmlContent, requestHeaders);

        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/document/xml", HttpMethod.PUT, updateEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void retrieveHistoryDocumentXml() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map for History Test");
        final String mapId = resourceUri.getPath().replace("/api/restful/maps/", "");

        final ResponseEntity<RestMindmapHistoryList> historyResponse = restTemplate.exchange("/api/restful/maps/" + mapId + "/history/", HttpMethod.GET, null, RestMindmapHistoryList.class);
        assertTrue(historyResponse.getStatusCode().is2xxSuccessful());

        if (historyResponse.getBody() != null && !historyResponse.getBody().getChanges().isEmpty()) {
            final int hid = historyResponse.getBody().getChanges().get(0).getId();
            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.TEXT_PLAIN);
            final HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);

            final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/maps/" + mapId + "/" + hid + "/document/xml", HttpMethod.GET, requestEntity, String.class);
            assertTrue(exchange.getStatusCode().is2xxSuccessful());
            assertNotNull(exchange.getBody());
        }
    }

    @Test
    public void getStarred() throws URISyntaxException {
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final URI resourceUri = addNewMap(restTemplate, "Map to Star");

        final HttpHeaders starHeaders = new HttpHeaders();
        starHeaders.setContentType(MediaType.TEXT_PLAIN);
        final HttpEntity<String> starEntity = new HttpEntity<>("true", starHeaders);
        restTemplate.exchange(resourceUri + "/starred", HttpMethod.PUT, starEntity, String.class);

        final ResponseEntity<String> getStarredResponse = restTemplate.exchange(resourceUri + "/starred", HttpMethod.GET, null, String.class);
        assertTrue(getStarredResponse.getStatusCode().is2xxSuccessful());
        assertEquals("true", getStarredResponse.getBody());
    }

    private String changeMapTitle(final HttpHeaders requestHeaders, final MediaType mediaType, final TestRestTemplate template, final URI resourceUri) throws RestClientException {
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String result = "New map to change title  - " + mediaType;
        final HttpEntity<String> updateEntity = new HttpEntity<>(result, requestHeaders);
        template.put(resourceUri + "/title", updateEntity);
        return result;
    }

    @NotNull
    private RestMindmapList fetchMaps(final HttpHeaders requestHeaders, final TestRestTemplate template) throws RestClientException {
        final HttpEntity<RestMindmapList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapList> response = template.exchange("/api/restful/maps/", HttpMethod.GET, findMapEntity, RestMindmapList.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), response.toString());

        return Objects.requireNonNull(response.getBody());
    }


    private RestCollaborationList fetchAndGetCollabs(final HttpHeaders requestHeaders, final TestRestTemplate template, final URI resourceUri) {
        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
        return response.getBody();
    }

    private RestCollaboration addCollabToList(String newCollab, String role, RestCollaborationList collabs) {
        RestCollaboration collab = new RestCollaboration();
        collab.setEmail(newCollab);
        collab.setRole(role);
        collabs.addCollaboration(collab);
        return collab;
    }

    private RestMindmap findMap(@NotNull HttpHeaders requestHeaders, @NotNull TestRestTemplate template, URI resourceUri) {
        final HttpEntity<RestMindmap> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmap> response = template.exchange(resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(response.toString());
        }
        return response.getBody();
    }

    //
    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title, @Nullable String xml) throws URISyntaxException {
        // Create a new map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_XML);
        final HttpEntity<String> createUserEntity = new HttpEntity<>(xml, requestHeaders);

        final ResponseEntity<String> exchange = template.exchange("/api/restful/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        final List<String> locations = exchange.getHeaders().get(HttpHeaders.LOCATION);
        return new URI(locations.stream().findFirst().get());
    }

    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) throws URISyntaxException {
        return addNewMap(template, title, null);
    }

}
