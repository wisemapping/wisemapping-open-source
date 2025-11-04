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
import com.wisemapping.rest.model.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify mindmap deletion behavior through REST API.
 * 
 * Tests various scenarios including:
 * - Deleting mindmaps with labels, collaborations, and history
 * - Verifying that collaborators cannot delete mindmaps (only remove themselves)
 * - Verifying that owners can delete mindmaps
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestMindmapDeletionTest {

    private RestUser ownerUser;
    private RestUser collaboratorUser;
    private String userPassword = "testPassword123";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void createUsers() {
        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory("http://localhost:8081/"));
        }

        // Create owner user
        this.ownerUser = createTestUser(restTemplate, userPassword);
        
        // Create collaborator user
        this.collaboratorUser = createTestUser(restTemplate, "collabPassword123");
    }

    @Test
    public void testDeleteMindmapWithLabels_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());

        // Create mindmap
        final String mapTitle = "Map with Labels";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create and associate label
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Test Label", "#FF0000");
        final String labelId = labelUri.getPath().replace("/api/restful/labels/", "");

        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity, String.class);

        // Verify label is associated
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertEquals(1, mindmapInfo.get().getLabels().size());

        // Delete mindmap - should not throw error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
        }, "Deleting mindmap with labels should not throw an error");

        // Verify mindmap was deleted
        final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
    }

    @Test
    public void testDeleteMindmapWithMultipleLabels_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());

        // Create mindmap
        final String mapTitle = "Map with Multiple Labels";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create and associate multiple labels
        final URI label1Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 1", "#FF0000");
        final URI label2Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 2", "#00FF00");
        final URI label3Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 3", "#0000FF");
        
        final String label1Id = label1Uri.getPath().replace("/api/restful/labels/", "");
        final String label2Id = label2Uri.getPath().replace("/api/restful/labels/", "");
        final String label3Id = label3Uri.getPath().replace("/api/restful/labels/", "");

        HttpEntity<String> labelEntity1 = new HttpEntity<>(label1Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity1, String.class);
        
        HttpEntity<String> labelEntity2 = new HttpEntity<>(label2Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity2, String.class);
        
        HttpEntity<String> labelEntity3 = new HttpEntity<>(label3Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity3, String.class);

        // Verify labels are associated
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertTrue(mindmapInfo.get().getLabels().size() >= 3, "Mindmap should have at least 3 labels");

        // Delete mindmap - should not throw error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
        }, "Deleting mindmap with multiple labels should not throw an error");

        // Verify mindmap was deleted
        final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
    }

    @Test
    public void testDeleteMindmapWithCollaborations_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());

        // Create mindmap
        final String mapTitle = "Map with Collaborations";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Add collaborators
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        addCollabToList(collaboratorUser.getEmail(), "editor", collabs);
        
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        authenticatedTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Verify mindmap has collaborations
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());

        // Delete mindmap - should not throw error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
        }, "Deleting mindmap with collaborations should not throw an error");

        // Verify mindmap was deleted
        final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
    }

    @Test
    public void testDeleteMindmapWithLabelsAndCollaborations_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());

        // Create mindmap
        final String mapTitle = "Map with Labels and Collaborations";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create and associate label
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Test Label", "#FF0000");
        final String labelId = labelUri.getPath().replace("/api/restful/labels/", "");

        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity, String.class);

        // Add collaborators
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        addCollabToList(collaboratorUser.getEmail(), "viewer", collabs);
        
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        authenticatedTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Verify mindmap has labels and collaborations
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertTrue(mindmapInfo.get().getLabels().size() >= 1, "Mindmap should have labels");

        // Delete mindmap - should not throw error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
        }, "Deleting mindmap with labels and collaborations should not throw an error");

        // Verify mindmap was deleted
        final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
    }

    @Test
    public void testDeleteMindmapAsCollaborator_ShouldRemoveCollaborationNotDeleteMindmap() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate ownerTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());
        final TestRestTemplate collaboratorTemplate = this.restTemplate.withBasicAuth(collaboratorUser.getEmail(), collaboratorUser.getPassword());

        // Owner creates mindmap
        final String mapTitle = "Map for Collaborator Test";
        final URI mindmapUri = addNewMap(ownerTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Owner adds collaborator
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        addCollabToList(collaboratorUser.getEmail(), "editor", collabs);
        
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        ownerTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Verify collaborator can see the mindmap
        final RestMindmapList collaboratorMindmapList = fetchMaps(requestHeaders, collaboratorTemplate);
        final Optional<RestMindmapInfo> collaboratorMindmapInfo = collaboratorMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(collaboratorMindmapInfo.isPresent(), "Collaborator should be able to see the mindmap");

        // Collaborator tries to "delete" the mindmap
        // This should only remove the collaboration, NOT delete the mindmap
        final ResponseEntity<String> collaboratorDeleteResponse = collaboratorTemplate.exchange(
            mindmapUri.toString(), 
            HttpMethod.DELETE, 
            null, 
            String.class);
        
        assertTrue(collaboratorDeleteResponse.getStatusCode().is2xxSuccessful(), 
            "Collaborator delete request should succeed (removes collaboration)");

        // Verify mindmap still exists (owner can still see it)
        final RestMindmapList ownerMindmapList = fetchMaps(requestHeaders, ownerTemplate);
        final Optional<RestMindmapInfo> ownerMindmapInfo = ownerMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(ownerMindmapInfo.isPresent(), "Mindmap should still exist after collaborator delete");

        // Verify collaborator can no longer see the mindmap (removed from collaborations)
        final RestMindmapList collaboratorMindmapListAfter = fetchMaps(requestHeaders, collaboratorTemplate);
        final Optional<RestMindmapInfo> collaboratorMindmapInfoAfter = collaboratorMindmapListAfter.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(collaboratorMindmapInfoAfter.isPresent(), "Collaborator should no longer see the mindmap");
    }

    @Test
    public void testDeleteMindmapAsCollaboratorWithLabels_ShouldRemoveCollaborationNotDeleteMindmap() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate ownerTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());
        final TestRestTemplate collaboratorTemplate = this.restTemplate.withBasicAuth(collaboratorUser.getEmail(), collaboratorUser.getPassword());

        // Owner creates mindmap
        final String mapTitle = "Map with Labels for Collaborator Test";
        final URI mindmapUri = addNewMap(ownerTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Owner creates and associates label
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, ownerTemplate, "Test Label", "#FF0000");
        final String labelId = labelUri.getPath().replace("/api/restful/labels/", "");

        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        ownerTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity, String.class);

        // Owner adds collaborator
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final RestCollaborationList collabs = new RestCollaborationList();
        addCollabToList(collaboratorUser.getEmail(), "viewer", collabs);
        
        final HttpEntity<RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        ownerTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Verify collaborator can see the mindmap
        final RestMindmapList collaboratorMindmapList = fetchMaps(requestHeaders, collaboratorTemplate);
        final Optional<RestMindmapInfo> collaboratorMindmapInfo = collaboratorMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(collaboratorMindmapInfo.isPresent(), "Collaborator should be able to see the mindmap");

        // Collaborator tries to "delete" the mindmap
        // This should only remove the collaboration, NOT delete the mindmap
        final ResponseEntity<String> collaboratorDeleteResponse = collaboratorTemplate.exchange(
            mindmapUri.toString(), 
            HttpMethod.DELETE, 
            null, 
            String.class);
        
        assertTrue(collaboratorDeleteResponse.getStatusCode().is2xxSuccessful(), 
            "Collaborator delete request should succeed (removes collaboration)");

        // Verify mindmap still exists (owner can still see it)
        final RestMindmapList ownerMindmapList = fetchMaps(requestHeaders, ownerTemplate);
        final Optional<RestMindmapInfo> ownerMindmapInfo = ownerMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(ownerMindmapInfo.isPresent(), "Mindmap should still exist after collaborator delete");
        
        // Verify labels are still there
        assertTrue(ownerMindmapInfo.get().getLabels().size() >= 1, "Mindmap should still have labels");

        // Verify collaborator can no longer see the mindmap (removed from collaborations)
        final RestMindmapList collaboratorMindmapListAfter = fetchMaps(requestHeaders, collaboratorTemplate);
        final Optional<RestMindmapInfo> collaboratorMindmapInfoAfter = collaboratorMindmapListAfter.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(collaboratorMindmapInfoAfter.isPresent(), "Collaborator should no longer see the mindmap");
    }

    @Test
    public void testDeleteMindmapAsOwner_ShouldDeleteMindmap() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(ownerUser.getEmail(), ownerUser.getPassword());

        // Owner creates mindmap
        final String mapTitle = "Map to Delete as Owner";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Owner deletes the mindmap
        final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
            mindmapUri.toString(), 
            HttpMethod.DELETE, 
            null, 
            String.class);
        
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
            "Owner delete should succeed");

        // Verify mindmap was deleted
        final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted when owner deletes it");
    }

    private RestCollaboration addCollabToList(String email, String role, RestCollaborationList collabs) {
        RestCollaboration collab = new RestCollaboration();
        collab.setEmail(email);
        collab.setRole(role);
        collabs.addCollaboration(collab);
        return collab;
    }

    private URI addNewMap(@NotNull TestRestTemplate template, @NotNull String title) throws URISyntaxException {
        // Create a new map ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_XML);
        final HttpEntity<String> createUserEntity = new HttpEntity<>("", requestHeaders);

        final ResponseEntity<String> exchange = template.exchange("/api/restful/maps?title=" + title, HttpMethod.POST, createUserEntity, String.class);
        assertTrue(exchange.getStatusCode().is2xxSuccessful());

        final java.util.List<String> locations = exchange.getHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(locations, "Location header should be present");
        assertFalse(locations.isEmpty(), "Location header should not be empty");
        return new URI(locations.stream().findFirst().get());
    }

    private RestMindmapList fetchMaps(@NotNull HttpHeaders requestHeaders, @NotNull TestRestTemplate template) {
        final HttpEntity<RestMindmapList> findMapEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestMindmapList> response = template.exchange(
            "/api/restful/maps/", 
            HttpMethod.GET, 
            findMapEntity, 
            RestMindmapList.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), response.toString());
        return response.getBody();
    }
}

