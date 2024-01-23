package com.wisemapping.test.rest;


import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.rest.AdminController;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.*;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {RestAppConfig.class, CommonConfig.class, MindmapController.class, AdminController.class, UserController.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestMindmapControllerTest {

    private RestUser user;

    @Autowired
    private TestRestTemplate restTemplate;

    private RestAccountControllerTest restAccount;

    @BeforeEach
    void createUser() {

        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081/"));
        }
        this.restAccount = RestAccountControllerTest.create(restTemplate);
        this.user = restAccount.createNewUser();
    }

    @Test
    public void listMaps() {
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
    public void deleteMap() {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title1 = "Map to delete";
        final URI resourceUri = addNewMap(restTemplate, title1);

        // Now remove it ...
        restTemplate.delete(resourceUri.toString());

        // Check that has been removed ...
        try {
            findMap(requestHeaders, restTemplate, resourceUri);
            fail("Map could not be removed:" + resourceUri);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void changeMapTitle() {
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
    public void validateMapsCreation() {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        requestHeaders.set(HttpHeaders.ACCEPT_LANGUAGE, "en");
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a sample map ...
        final String title = "Map to Validate Creation";
        addNewMap(restTemplate, title);

        // Add map with same name ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<String> response = restTemplate.exchange("/api/restfull/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("You have already a map with the same name"));
    }


    @Test
    public void changeMapDescription() {
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
    public void updateMapXml() throws IOException {    // Configure media types ...
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

    private String updateMapDocument(final HttpHeaders requestHeaders, final TestRestTemplate template, final String resourceUrl, String content) throws RestClientException {
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newXmlContent = content != null ? content : "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<>(newXmlContent, requestHeaders);
        template.put(resourceUrl + "/document/xml", updateEntity);
        return newXmlContent;
    }


    @Test
    public void cloneMap() throws IOException {    // Configure media types ...
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
        final URI clonedMapUri = restTemplate.postForLocation(newMapUri, cloneEntity);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, restTemplate, clonedMapUri);
        assertEquals(response.getXml(), xml);
    }

    //
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateStarred(final @NotNull MediaType mediaType) {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String title1 = "Stared Map user 1";
//        URI mapUri = addNewMap(template, title1);
//
//        // Update starred ...
//        final String resourceUrl = HOST_PORT + mapUri.toString() + "/starred";
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//        final HttpHeaders textContentType = new HttpHeaders();
//        textContentType.setContentType(MediaType.TEXT_PLAIN);
//        final HttpEntity<String> updateEntity = new HttpEntity<>("true", textContentType);
//        template.put(resourceUrl, updateEntity);
//
//        // Has been updated ?.
//
//        final HttpEntity findLabelEntity = new HttpEntity(createHeaders(MediaType.TEXT_PLAIN));
//        final ResponseEntity<String> response = template.exchange(resourceUrl, HttpMethod.GET, findLabelEntity, String.class);
//
//        assertTrue(Boolean.parseBoolean(response.getBody()), "Starred has been updated");
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void verifyMapOwnership(final @NotNull MediaType mediaType) {    // Configure media types ...
//        final RestAdminITCase restAdminITCase = new RestAdminITCase();
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String title1 = "verifyMapOwnership Map user 1";
//        addNewMap(template, title1);
//
//        //create another user
//        RestUser secondUser = restAdminITCase.createNewUserAndGetUser(MediaType.APPLICATION_JSON);
//        final RestTemplate secondTemplate = createTemplate(secondUser.getEmail() + ":admin");
//
//        final String title2 = "verifyMapOwnership Map user 2";
//        addNewMap(secondTemplate, title2);
//
//        // Delete user ...
//        String authorisation = "admin@wisemapping.org" + ":" + "test";
//        RestTemplate superadminTemplate = createTemplate(authorisation);
//
//        superadminTemplate.delete(BASE_REST_URL + "/admin/users/" + secondUser.getId());
//
//        // Validate that the two maps are there ...
//        final RestMindmapList body = fetchMaps(requestHeaders, template);
//        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();
//
//        boolean found1 = false;
//        for (RestMindmapInfo mindmap : mindmaps) {
//            if (mindmap.getTitle().equals(title1)) {
//                found1 = true;
//                break;
//            }
//        }
//        assertTrue(found1, "Map could not be found");
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateMap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String title = "Update sample " + mediaType;
//        final URI resourceUri = addNewMap(template, title);
//
//        // Build map to update ...
//        final RestMindmap mapToUpdate = new RestMindmap();
//        mapToUpdate.setXml("<map>this is not valid</map>");
//        mapToUpdate.setProperties("{zoom:x}");
//
//        // Create lock ...
//        final HttpHeaders lockHeaders = createHeaders(mediaType);
//        lockHeaders.setContentType(MediaType.TEXT_PLAIN);
//
//        // Update map ...
//        final String resourceUrl = HOST_PORT + resourceUri.toString() + "/document";
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
//        template.put(resourceUrl, updateEntity);
//
//        // Check that the map has been updated ...
//        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
//        final ResponseEntity<RestMindmap> response = template.exchange(HOST_PORT + resourceUri, HttpMethod.GET, findMapEntity, RestMindmap.class);
//        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
//        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
//
//        // Unlock ...
//        HttpEntity<String> lockEntity = new HttpEntity<>("false", lockHeaders);
//        template.exchange(HOST_PORT + resourceUri + "/lock", HttpMethod.PUT, lockEntity, RestLockInfo.class);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void addCollabs(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "Map for addCollabs  - " + mediaType);
//
//        String newCollab = addNewCollaboration(requestHeaders, template, resourceUri);
//
//        // Has been added ?
//        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, template, resourceUri);
//
//        // Has been added ?
//        assertEquals(responseCollbs.getCount(), 2);
//
//        final Optional<RestCollaboration> addedCollab = responseCollbs.getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
//        assertTrue(addedCollab.isPresent());
//        assertEquals(addedCollab.get().getRole(), "editor");
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateCollabType(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "Map for updateCollabType  - " + mediaType);
//
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//
//        final String newCollab = "new-collab@example.com";
//        String role = "editor";
//
//        final RestCollaboration collab = addCollabToList(newCollab, role, collabs);
//
//        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//
//        // Has been added ?
//        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, template, resourceUri);
//        assertEquals(responseCollbs.getCount(), 2);
//
//        // Update the collaboration type ...
//        collab.setRole("viewer");
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//
//        // Has been added ?
//        final ResponseEntity<RestCollaborationList> afterResponse = fetchCollabs(requestHeaders, template, resourceUri);
//        final Optional<RestCollaboration> updatedCollab = afterResponse.getBody().getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
//        assertTrue(updatedCollab.isPresent());
//        assertEquals(updatedCollab.get().getRole(), "viewer");
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void deleteCollabs(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "Map for deleteCollabs  - " + mediaType);
//
//        String newCollab = addNewCollaboration(requestHeaders, template, resourceUri);
//
//        // Has been added ?
//        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, template, resourceUri);
//
//        // Has been added ?
//        assertEquals(responseCollbs.getCount(), 2);
//
//        // Now, remove it ...
//        template.delete(HOST_PORT + resourceUri + "/collabs?email=" + newCollab);
//
//        // Check that it has been removed ...
//        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, template, resourceUri);
//        assertEquals(afterDeleteResponse.getBody().getCollaborations().size(), 1);
//    }
//
//    private String addNewCollaboration(final HttpHeaders requestHeaders, final RestTemplate template, final URI resourceUri) throws RestClientException {
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//        final String newCollab = "new-collab@example.com";
//        String role = "editor";
//        addCollabToList(newCollab, role, collabs);
//        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//        return newCollab;
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void deleteCollabsWithInvalidEmail(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "deleteCollabsWithInvalidEmail");
//
//        // Remove with invalid email ...
//        try {
//
//            template.delete(HOST_PORT + resourceUri + "/collabs?email=invalidEmail");
//        } catch (HttpClientErrorException e) {
//            assertEquals(e.getRawStatusCode(), 400);
//            assertTrue(e.getMessage().contains("Invalid email exception:"));
//        }
//
//        // Check that it has been removed ...
//        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, template, resourceUri);
//        assertEquals(afterDeleteResponse.getBody().getCollaborations().size(), 1);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void deleteCollabsWithoutOwnerPermission(final @NotNull MediaType mediaType) {
//
//
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "deleteWithoutOwnerPermission");
//
//        final String newCollab = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
//        template = createTemplate(newCollab + ":admin");
//
//        // Remove with invalid email ...
//        try {
//
//            template.delete(HOST_PORT + resourceUri + "/collabs?email=" + newCollab);
//        } catch (HttpClientErrorException e) {
//            assertEquals(e.getRawStatusCode(), 400);
//            assertTrue(e.getMessage().contains("No enough permissions"));
//        }
//
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void deleteOwnerCollab(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "Map for deleteOwnerCollab");
//
//        // Now, remove owner collab ...
//        try {
//            template.delete(HOST_PORT + resourceUri + "/collabs?email=" + userEmail.replace(":admin", ""));
//        } catch (HttpClientErrorException e) {
//            assertEquals(e.getRawStatusCode(), 400);
//            assertTrue(e.getMessage().contains("Can not remove owner collab"));
//        }
//    }
//
//    @NotNull
//    private ResponseEntity<RestCollaborationList> fetchCollabs(HttpHeaders requestHeaders, RestTemplate template, URI resourceUri) {
//        final HttpEntity findCollabs = new HttpEntity(requestHeaders);
//        return template.exchange(HOST_PORT + resourceUri + "/collabs", HttpMethod.GET, findCollabs, RestCollaborationList.class);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, expectedExceptions = {HttpClientErrorException.class}, dataProvider = "ContentType-Provider-Function")
//    public void addCollabsInvalidOwner(final @NotNull MediaType mediaType) {
//
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...fetchAndGetCollabs(requestHeaders, template, resourceUri);
//        final URI resourceUri = addNewMap(template, "Map for Collaboration  - " + mediaType);
//
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//
//        // Validate that owner can not be added.
//        addCollabToList("newCollab@example", "owner", collabs);
//
//        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void removeLabelFromMindmap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a new label
//        final String titleLabel = "removeLabelFromMindmap";
//        final URI labelUri = RestLabelITCase.addNewLabel(requestHeaders, template, titleLabel, COLOR);
//
//        // Create a sample map ...
//        final String mapTitle = "removeLabelFromMindmap";
//        final URI mindmapUri = addNewMap(template, mapTitle);
//        final String mapId = mindmapUri.getPath().replace("/api/restfull/maps/", "");
//
//        // Assign label to map ...
//        String labelId = labelUri.getPath().replace("/api/restfull/labels/", "");
//        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
//        template.postForLocation(BASE_REST_URL + "/maps/" + mapId + "/labels", labelEntity);
//
//        // Remove label from map
//        template.delete(BASE_REST_URL + "/maps/" + mapId + "/labels/" + labelId);
//
//        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, template, mapId);
//        assertTrue(mindmapInfo.get().getLabels().size() == 0);
//
//    }
//
//    @NotNull
//    private Optional<RestMindmapInfo> fetchMap(HttpHeaders requestHeaders, RestTemplate template, @NotNull String mapId) {
//        // Check that the label has been removed ...
//        final List<RestMindmapInfo> mindmapsInfo = fetchMaps(requestHeaders, template).getMindmapsInfo();
//        Optional<RestMindmapInfo> mindmapInfo = mindmapsInfo
//                .stream()
//                .filter(m -> m.getId() == Integer.parseInt(mapId))
//                .findAny();
//        return mindmapInfo;
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void deleteMapAndCheckLabels(final @NotNull MediaType mediaType) {    // Configure media types ...
//        throw new SkipException("missing test: delete map should not affects others labels");
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void addLabelToMindmap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a new label
//        final String titleLabel = "Label 1  - " + mediaType;
//        final URI labelUri = RestLabelITCase.addNewLabel(requestHeaders, template, titleLabel, COLOR);
//
//        // Create a sample map ...
//        final String mapTitle = "Maps 1  - " + mediaType;
//        final URI mindmapUri = addNewMap(template, mapTitle);
//        final String mapId = mindmapUri.getPath().replace("/api/restfull/maps/", "");
//
//        // Assign label to map ...
//        String labelId = labelUri.getPath().replace("/api/restfull/labels/", "");
//        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
//        template.postForLocation(BASE_REST_URL + "/maps/" + mapId + "/labels", labelEntity);
//
//        // Check that the label has been assigned ...
//        Optional<RestMindmapInfo> mindmapInfo = fetchMap(requestHeaders, template, mapId);
//
//        assertTrue(mindmapInfo.get().getLabels().size() == 1);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateCollabs(final @NotNull MediaType mediaType) {
//
//        // Create a sample map ...
//        final RestTemplate template = createTemplate(userEmail);
//        final URI resourceUri = addNewMap(template, "Map for updateCollabs  - " + mediaType);
//
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//
//        String newCollab = "new-collab@example.com";
//        String role = "editor";
//
//        addCollabToList(newCollab, role, collabs);
//
//        HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//
//        collabs = fetchAndGetCollabs(requestHeaders, template, resourceUri);
//
//        //delete one collab
//        collabs.setCollaborations(collabs.getCollaborations().stream().filter(c -> c.getRole().equals("owner")).collect(Collectors.toList()));
//
//        //Add another collaborationMediaType
//        newCollab = "another-collab@example.com";
//        addCollabToList(newCollab, role, collabs);
//
//        //add owner to list
//        addCollabToList(userEmail.replace(":admin", ""), "owner", collabs);
//
//        updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.postForLocation(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//
//
//        RestCollaborationList responseCollbs = fetchAndGetCollabs(requestHeaders, template, resourceUri);
//
//        // Has been another-collaboration list updated ?
//        assertTrue(responseCollbs.getCollaborations().stream().anyMatch(x -> x.getEmail().equals("another-collab@example.com")));
//        assertEquals(responseCollbs.getCount(), 2);
//
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateProperties(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String title = "updateProperties map";
//        final URI resourceUri = addNewMap(template, title);
//
//        // Build map to update ...
//        final RestMindmap mapToUpdate = new RestMindmap();
//        mapToUpdate.setXml("<map>this is not valid</map>");
//        mapToUpdate.setProperties("{zoom:x}");
//        mapToUpdate.setTitle("new title for map");
//        mapToUpdate.setDescription("updated map description");
//
//        // Update map ...
//        final String resourceUrl = HOST_PORT + resourceUri.toString();
//        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<>(mapToUpdate, requestHeaders);
//        template.put(resourceUrl, updateEntity);
//
//        // Check that the map has been updated ...
//        HttpEntity<RestUser> findMapEntity = new HttpEntity<>(requestHeaders);
//        final ResponseEntity<RestMindmap> response = template.exchange(HOST_PORT + resourceUri, HttpMethod.GET, findMapEntity, RestMindmap.class);
//        assertEquals(response.getBody().getTitle(), mapToUpdate.getTitle());
//        assertEquals(response.getBody().getDescription(), mapToUpdate.getDescription());
//        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
//        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void batchDelete(final @NotNull MediaType mediaType) {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String title1 = "Batch delete map 1";
//        addNewMap(template, title1);
//
//        final String title2 = "Batch delete map 2";
//        addNewMap(template, title2);
//
//
//        String maps;
//        maps = fetchMaps(requestHeaders, template).getMindmapsInfo().stream().map(map -> {
//            return String.valueOf(map.getId());
//        }).collect(Collectors.joining(","));
//
//
//        template.delete(BASE_REST_URL + "/maps/batch?ids=" + maps);
//
//        // Validate that the two maps are there ...
//        final RestMindmapList body = fetchMaps(requestHeaders, template);
//        assertEquals(body.getMindmapsInfo().size(), 0);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updatePublishState(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final String mapTitle = "updatePublishState";
//        final URI mindmapUri = addNewMap(template, mapTitle);
//        final String mapId = mindmapUri.getPath().replace("/api/restfull/maps/", "");
//
//        // Change map status ...
//        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
//        //final String newPublicState = "true";
//        final HttpEntity<String> updateEntity = new HttpEntity<>(Boolean.TRUE.toString(), requestHeaders);
//        template.put(HOST_PORT + mindmapUri + "/publish", updateEntity);
//
////        //fetch public view
////        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
////        ResponseEntity<String> publicView = template.exchange(HOST_PORT  + mapId + "/public", HttpMethod.GET, findMapEntity, String.class);
////        assertNotNull(publicView.getBody());
////        assertEquals(publicView.getStatusCodeValue(), 200);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void fetchMapHistory(final @NotNull MediaType mediaType) {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "Map to change title  - " + mediaType);
//
//        updateMapDocument(requestHeaders, template, HOST_PORT + resourceUri.toString());
//
//        //fetch map history
//        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
//        final ResponseEntity<RestMindmapHistoryList> maps = template.exchange(HOST_PORT + resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);
//        assertEquals(maps.getBody().getCount(), 1);
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void updateRevertMindmap(final @NotNull MediaType mediaType) throws IOException {    // Configure media types ...
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        final RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "map to test revert changes");
//        updateMapDocument(requestHeaders, template, HOST_PORT + resourceUri.toString(), "<map><node text='this is an xml to test revert changes service'></map>");
//
//        updateMapDocument(requestHeaders, template, HOST_PORT + resourceUri.toString(), "<map><node text='this is an xml with modification to be reverted'></map>");
//
//        //fetch map history
//        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
//        final ResponseEntity<RestMindmapHistoryList> mapHistories = template.exchange(HOST_PORT + resourceUri + "/history/", HttpMethod.GET, findMapEntity, RestMindmapHistoryList.class);
//
//        //aply revert
//        final HttpEntity<String> cloneEntity = new HttpEntity<>(requestHeaders);
//        template.postForLocation(HOST_PORT + resourceUri + "/history/latest", cloneEntity);
//        final RestMindmap latestStoredMap = findMap(requestHeaders, template, resourceUri);
//        template.postForLocation(HOST_PORT + resourceUri + "/history/" + mapHistories.getBody().getChanges().get(1).getId(), cloneEntity);
//        final RestMindmap firstVersionMap = findMap(requestHeaders, template, resourceUri);
//
//        //verify revert
//        assertEquals(firstVersionMap.getXml(), "<map><node text='this is an xml to test revert changes service'></map>");
//        assertEquals(latestStoredMap.getXml(), "<map><node text='this is an xml with modification to be reverted'></map>");
//
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void addCollabWhitoutOwnerPermission(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "MaddCollabWhitoutOwnerPermission");
//
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//
//        final String newCollab = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
//        String role = "editor";
//
//        addCollabToList(newCollab, role, collabs);
//
//        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//
//        template = createTemplate(newCollab + ":admin");
//        //add collab again with the new user expecting the Exception
//        try {
//            template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//        } catch (HttpClientErrorException e) {
//            assertEquals(e.getRawStatusCode(), 400);
//            assertTrue(e.getMessage().contains("User must be owner to share mindmap"));
//        }
//    }
//
//    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
//    public void addCollabWhitOwnerRole(final @NotNull MediaType mediaType) {
//        final HttpHeaders requestHeaders = createHeaders(mediaType);
//        RestTemplate template = createTemplate(userEmail);
//
//        // Create a sample map ...
//        final URI resourceUri = addNewMap(template, "addCollabWhitOwnerRole");
//
//        // Add a new collaboration ...
//        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        final RestCollaborationList collabs = new RestCollaborationList();
//        collabs.setMessage("Adding new permission");
//
//        final String newCollab = "new-collaborator@mail.com";
//        String role = "owner";
//
//        addCollabToList(newCollab, role, collabs);
//
//        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
//        try {
//            template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
//        } catch (HttpClientErrorException e) {
//            assertEquals(e.getRawStatusCode(), 400);
//            assertTrue(e.getMessage().contains("Collab email can not be change"));
//        }
//    }
//
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
        final ResponseEntity<RestMindmapList> response = template.exchange("/api/restfull/maps/", HttpMethod.GET, findMapEntity, RestMindmapList.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), response.toString());

        return Objects.requireNonNull(response.getBody());
    }

    //
//    private RestCollaborationList fetchAndGetCollabs(final HttpHeaders requestHeaders, final RestTemplate template, final URI resourceUri) {
//        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
//        RestCollaborationList responseCollbs = response.getBody();
//        return responseCollbs;
//    }
//
//    private RestCollaboration addCollabToList(String newCollab, String role, RestCollaborationList collabs) {
//        RestCollaboration collab = new RestCollaboration();
//        collab.setEmail(newCollab);
//        collab.setRole(role);
//        collabs.addCollaboration(collab);
//        return collab;
//    }
//
    private RestMindmap findMap(@NotNull HttpHeaders requestHeaders, @NotNull TestRestTemplate template, URI resourceUri) {
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestMindmap> response = template.exchange("http://localhost:8081/" + resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(response.toString());
        }
        return response.getBody();
    }

    //
    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title, @Nullable String xml) {
        // Create a new map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_XML);
        HttpEntity<String> createUserEntity = new HttpEntity<>(xml, requestHeaders);
        return template.postForLocation("/api/restfull/maps?title=" + title, createUserEntity);
    }

    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) {
        return addNewMap(template, title, null);
    }

}
