package com.wisemapping.test.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.rest.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.wisemapping.test.rest.RestHelper.*;
import static org.testng.Assert.*;

@Test
public class RestMindmapITCase {

    private String userEmail = "admin@wisemapping.com";
    private static final String ICON = "glyphicon glyphicon-tag";

    @BeforeClass
    void createUser() {

        final RestAdminITCase restAdminITCase = new RestAdminITCase();
        userEmail = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
        userEmail += ":" + "admin";
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void listMaps(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title1 = "List Maps 1  - " + mediaType.toString();
        addNewMap(requestHeaders, template, title1);

        final String title2 = "List Maps 2 - " + mediaType.toString();
        addNewMap(requestHeaders, template, title2);

        // Check that the map has been created ...
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestMindmapList> response = template.exchange(BASE_REST_URL + "/maps/", HttpMethod.GET, findMapEntity, RestMindmapList.class);

        // Validate that the two maps are there ...
        final RestMindmapList body = response.getBody();
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

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void deleteMap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title1 = "Map to delete  - " + mediaType.toString();
        final URI resourceUri = addNewMap(requestHeaders, template, title1);

        // Now remove it ...
        template.delete(HOST_PORT + resourceUri.toString());

        // Check that has been removed ...
        try {
            findMap(requestHeaders, template, resourceUri);
            fail("Map could not be removed:" + resourceUri);
        } catch (Exception e) {
        }
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void changeMapTitle(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map to change title  - " + mediaType.toString());

        // Change map title ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newTitle = "New map to change title  - " + mediaType.toString();
        final HttpEntity<String> updateEntity = new HttpEntity<String>(newTitle, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/title", updateEntity);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, template, resourceUri);
        assertEquals(newTitle, map.getTitle());
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void validateMapsCreation(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title = "Map to Validate Creation  - " + mediaType.toString();
        final URI resourceUri = addNewMap(requestHeaders, template, title);

        // Try to create a map with the same title ..
        final RestMindmap restMindmap = new RestMindmap();
        restMindmap.setTitle(title);
        restMindmap.setDescription("My Map Desc");

        try {
            HttpEntity<RestMindmap> createUserEntity = new HttpEntity<RestMindmap>(restMindmap, requestHeaders);
            template.postForLocation(BASE_REST_URL + "/maps", createUserEntity);
        } catch (HttpClientErrorException cause) {
            final String responseBodyAsString = cause.getResponseBodyAsString();
            assert (responseBodyAsString.contains("You have already a map"));
            return;
        }
        fail("Wrong response");

    }


    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void changeMapDescription(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map to change Description  - " + mediaType.toString());

        // Change map title ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newDescription = "New map to change description  - " + mediaType.toString();
        final HttpEntity<String> updateEntity = new HttpEntity<String>(newDescription, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/description", updateEntity);

        // Load map again ..
        final RestMindmap map = findMap(requestHeaders, template, resourceUri);
        assertEquals(newDescription, map.getDescription());
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void updateMapXml(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title = "Update XML sample " + mediaType.toString();
        final URI resourceUri = addNewMap(requestHeaders, template, title);

        // Update map xml content ...
        final String resourceUrl = HOST_PORT + resourceUri.toString();
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        final String newXmlContent = "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<String>(newXmlContent, requestHeaders);
        template.put(resourceUrl + "/document/xml", updateEntity);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, template, resourceUri);
        assertEquals(response.getXml(), newXmlContent);
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void cloneMap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title = "Map to clone  sample " + mediaType.toString();
        final String xml = "<map><node text='this is a cloned map'></map>";
        final URI newMapUri = addNewMap(requestHeaders, template, title, xml);

        // Clone map ...
        final RestMindmapInfo restMindmap = new RestMindmapInfo();
        restMindmap.setTitle("Cloned map but with previous content." + mediaType.toString());
        restMindmap.setDescription("Cloned map desc");

        // Create a new map ...
        final HttpEntity<RestMindmapInfo> cloneEntity = new HttpEntity<RestMindmapInfo>(restMindmap, requestHeaders);
        final URI clonedMapUri = template.postForLocation(HOST_PORT + newMapUri, cloneEntity);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, template, clonedMapUri);
        assertEquals(response.getXml(), xml);
    }


    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void updateMap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        if(MediaType.APPLICATION_XML==mediaType){
            throw new SkipException("Some research need to check why it's falling.");
        }

        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final String title = "Update sample " + mediaType.toString();
        final URI resourceUri = addNewMap(requestHeaders, template, title);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");

        // Update map ...
        final String resourceUrl = HOST_PORT + resourceUri.toString() + "/document";
        requestHeaders.setContentType(MediaType.APPLICATION_XML);
        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<RestMindmap>(mapToUpdate, requestHeaders);
        template.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findMapEntity = new HttpEntity<RestUser>(requestHeaders);
        final ResponseEntity<RestMindmap> response = template.exchange(HOST_PORT + resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void addCollabs(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map for addCollabs  - " + mediaType.toString());

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collab@example.com";
        String role = "editor";

        final RestCollaboration collab = new RestCollaboration();
        collab.setEmail(newCollab);
        collab.setRole(role);
        collabs.addCollaboration(collab);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
        RestCollaborationList responseCollbs = response.getBody();

        // Has been added ?
        assertEquals(responseCollbs.getCount(), 2);

        final Optional<RestCollaboration> addedCollab = responseCollbs.getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(addedCollab.isPresent());
        assertEquals(addedCollab.get().getRole(), "editor");
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void updateCollabType(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map for updateCollabType  - " + mediaType.toString());

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collab@example.com";
        String role = "editor";

        final RestCollaboration collab = new RestCollaboration();
        collab.setEmail(newCollab);
        collab.setRole(role);
        collabs.addCollaboration(collab);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
        RestCollaborationList responseCollbs = response.getBody();
        assertEquals(responseCollbs.getCount(), 2);

       // Update the collaboration type ...
        collab.setRole("viewer");
        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> afterResponse = fetchCollabs(requestHeaders, template, resourceUri);
        final Optional<RestCollaboration> updatedCollab = afterResponse.getBody().getCollaborations().stream().filter(c -> c.getEmail().equals(newCollab)).findAny();
        assertTrue(updatedCollab.isPresent());
        assertEquals(updatedCollab.get().getRole(), "viewer");
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void deleteCollabs(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map for deleteCollabs  - " + mediaType.toString());

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        final String newCollab = "new-collab@example.com";
        String role = "editor";

        final RestCollaboration collab = new RestCollaboration();
        collab.setEmail(newCollab);
        collab.setRole(role);
        collabs.addCollaboration(collab);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);

        // Has been added ?
        final ResponseEntity<RestCollaborationList> response = fetchCollabs(requestHeaders, template, resourceUri);
        RestCollaborationList responseCollbs = response.getBody();

        // Has been added ?
        assertEquals(responseCollbs.getCount(), 2);

        // Now, remove it ...
        template.delete(HOST_PORT + resourceUri + "/collabs?email=" + newCollab);

        // Check that it has been removed ...
        final ResponseEntity<RestCollaborationList> afterDeleteResponse = fetchCollabs(requestHeaders, template, resourceUri);
        assertEquals(afterDeleteResponse.getBody().getCollaborations().size(), 1);
    }

    @NotNull
    private ResponseEntity<RestCollaborationList> fetchCollabs(HttpHeaders requestHeaders, RestTemplate template, URI resourceUri) {
        final HttpEntity findCollabs = new HttpEntity(requestHeaders);
        return template.exchange(HOST_PORT + resourceUri + "/collabs", HttpMethod.GET, findCollabs, RestCollaborationList.class);
    }

    @Test(dataProviderClass = RestHelper.class, expectedExceptions = {HttpClientErrorException.class}, dataProvider = "ContentType-Provider-Function")
    public void addCollabsInvalidOwner(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {

        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a sample map ...
        final URI resourceUri = addNewMap(requestHeaders, template, "Map for Collaboration  - " + mediaType.toString());

        // Add a new collaboration ...
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        collabs.setMessage("Adding new permission");

        // Validate that owner can not be added.
        final RestCollaboration collab = new RestCollaboration();
        final String newCollab = "new-collab@example.com";
        collab.setEmail(newCollab);
        collab.setRole("owner");
        collabs.addCollaboration(collab);

        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        template.put(HOST_PORT + resourceUri + "/collabs/", updateEntity);
    }


    @Test(dataProviderClass = RestHelper.class, dataProvider = "ContentType-Provider-Function")
    public void addLabelToMindmap(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate(userEmail);

        // Create a new label
        final String titleLabel = "Label 1  - " + mediaType.toString();
        final URI labelUri = RestLabelITCase.addNewLabel(requestHeaders, template, titleLabel, COLOR, ICON);

        // Create a sample map ...
        final String mapTitle = "Maps 1  - " + mediaType.toString();
        final URI mindmapUri = addNewMap(requestHeaders, template, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/service/maps/", "");

        final RestLabel restLabel = new RestLabel();
        restLabel.setColor(COLOR);
        String labelId = labelUri.getPath().replace("/service/labels/", "");
        restLabel.setId(Integer.parseInt(labelId));
        restLabel.setTitle(titleLabel);

        HttpEntity<RestLabel> labelEntity = new HttpEntity<>(restLabel, requestHeaders);
        template.postForLocation(BASE_REST_URL + "/labels/maps?ids=" + mapId, labelEntity);

        // Load map again ..
        final RestMindmap withLabel = findMap(requestHeaders, template, mindmapUri);

//        assertTrue(withLabel.getDelegated().getLabels().size() == 1);
    }

    private RestMindmap findMap(HttpHeaders requestHeaders, RestTemplate template, URI resourceUri) {
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestMindmap> response = template.exchange(HOST_PORT + resourceUri.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        return response.getBody();
    }

    private URI addNewMap(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title, @Nullable String xml) throws IOException, WiseMappingException {
        final RestMindmap restMindmap = new RestMindmap();
        restMindmap.setTitle(title);
        restMindmap.setDescription("My Map Desc");

        if (xml != null) {
            restMindmap.setXml(xml);
        }

        // Create a new map ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<>(restMindmap, requestHeaders);
        return template.postForLocation(BASE_REST_URL + "/maps", createUserEntity);
    }

    private URI addNewMap(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title) throws IOException, WiseMappingException {
        return addNewMap(requestHeaders, template, title, null);
    }

}
