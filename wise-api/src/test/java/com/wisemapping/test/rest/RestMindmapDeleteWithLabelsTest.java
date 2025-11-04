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
 * Integration test to reproduce the "detached entity passed to persist" error
 * when deleting a mindmap via REST API that has labels associated with it.
 * 
 * This test reproduces the production scenario where:
 * 1. A mindmap is created via REST API
 * 2. A label is created and associated with the mindmap via REST API
 * 3. The mindmap is deleted via REST API DELETE endpoint
 * 4. This causes "detached entity passed to persist: com.wisemapping.model.Mindmap" error
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestMindmapDeleteWithLabelsTest {

    private RestUser user;
    private String userPassword = "testPassword123";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void createUser() {
        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory("http://localhost:8081/"));
        }

        // Create a new test user using the helper method
        this.user = createTestUser(restTemplate, userPassword);
    }

    @Test
    public void testDeleteMindmapWithLabels_ReproducesDetachedEntityError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Step 1: Create a new mindmap via REST API
        final String mapTitle = "Map to Delete with Labels";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");
        System.out.println("Created mindmap with ID: " + mapId);

        // Step 2: Create a new label via REST API
        final String labelTitle = "Test Label for Delete";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, labelTitle, "#FF0000");
        final String labelId = labelUri.getPath().replace("/api/restful/labels/", "");
        System.out.println("Created label with ID: " + labelId);

        // Step 3: Associate label with mindmap via REST API
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        final ResponseEntity<String> labelAssociationResponse = authenticatedTemplate.exchange(
            "/api/restful/maps/" + mapId + "/labels", 
            HttpMethod.POST, 
            labelEntity, 
            String.class);
        
        assertTrue(labelAssociationResponse.getStatusCode().is2xxSuccessful(), 
            "Label association should succeed");
        System.out.println("Label associated with mindmap");

        // Step 4: Verify the label is associated
        // Use fetchMaps to get the list and find our mindmap
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent(), "Mindmap should exist");
        assertEquals(1, mindmapInfo.get().getLabels().size(), "Mindmap should have 1 label");
        System.out.println("Verified mindmap has label");

        // Step 5: Try to delete the mindmap via REST API
        // This should reproduce the "detached entity passed to persist" error
        System.out.println("Attempting to delete mindmap via DELETE /api/restful/maps/" + mapId);
        
        try {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            // Check the response status
            if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("DELETE request succeeded with status: " + deleteResponse.getStatusCode());
                
                // Verify mindmap was deleted
                final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
                final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
                    .filter(m -> m.getId() == Integer.parseInt(mapId))
                    .findAny();
                if (deletedMindmapInfo.isEmpty()) {
                    System.out.println("Mindmap was successfully deleted");
                } else {
                    System.out.println("WARNING: Mindmap still exists after DELETE");
                }
            } else {
                System.out.println("DELETE request failed with status: " + deleteResponse.getStatusCode());
                System.out.println("Response body: " + deleteResponse.getBody());
                fail("DELETE request should succeed, but got status: " + deleteResponse.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // Check if this is the error we're trying to reproduce
            final String responseBody = e.getResponseBodyAsString();
            System.out.println("=========================================");
            System.out.println("HTTP SERVER ERROR OCCURRED!");
            System.out.println("=========================================");
            System.out.println("Status: " + e.getStatusCode());
            System.out.println("Response body: " + responseBody);
            System.out.println("=========================================");
            
            if (responseBody != null && responseBody.contains("detached entity passed to persist")) {
                System.out.println("SUCCESSFULLY REPRODUCED THE ISSUE!");
                System.out.println("This confirms the bug exists and needs to be fixed");
                // Re-throw to fail the test and show the error
                throw new AssertionError("Detached entity error reproduced - this is the bug we're fixing", e);
            } else {
                // Different error - re-throw it
                System.out.println("Different error occurred - not the detached entity error");
                throw e;
            }
        } catch (Exception e) {
            // Catch any other exception
            System.out.println("=========================================");
            System.out.println("UNEXPECTED ERROR OCCURRED!");
            System.out.println("=========================================");
            System.out.println("Error class: " + e.getClass().getName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=========================================");
            throw e;
        }
    }

    @Test
    public void testDeleteMindmapWithLabels_ShouldNotThrowError() throws URISyntaxException {
        // This test verifies that the fix works correctly
        // It's the same as above but expects success
        
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new mindmap
        final String mapTitle = "Map to Delete with Labels (Fixed)";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create a new label
        final String labelTitle = "Test Label for Delete (Fixed)";
        final URI labelUri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, labelTitle, "#FF0000");
        final String labelId = labelUri.getPath().replace("/api/restful/labels/", "");

        // Associate label with mindmap
        HttpEntity<String> labelEntity = new HttpEntity<>(labelId, requestHeaders);
        authenticatedTemplate.exchange(
            "/api/restful/maps/" + mapId + "/labels", 
            HttpMethod.POST, 
            labelEntity, 
            String.class);

        // Verify the label is associated
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertEquals(1, mindmapInfo.get().getLabels().size());

        // Delete the mindmap - this should NOT throw an error after the fix
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
            
            // Verify mindmap was deleted
            final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
            final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
                .filter(m -> m.getId() == Integer.parseInt(mapId))
                .findAny();
            assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
        }, "Deleting mindmap with labels should not throw an error");
    }

    @Test
    public void testDeleteMindmapWithMultipleLabelsAndCollaborators_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new mindmap
        final String mapTitle = "Map with Labels and Collaborators";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create multiple labels
        final URI label1Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 1", "#FF0000");
        final URI label2Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 2", "#00FF00");
        final URI label3Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label 3", "#0000FF");
        
        final String label1Id = label1Uri.getPath().replace("/api/restful/labels/", "");
        final String label2Id = label2Uri.getPath().replace("/api/restful/labels/", "");
        final String label3Id = label3Uri.getPath().replace("/api/restful/labels/", "");

        // Associate all labels with mindmap
        HttpEntity<String> labelEntity1 = new HttpEntity<>(label1Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity1, String.class);
        
        HttpEntity<String> labelEntity2 = new HttpEntity<>(label2Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity2, String.class);
        
        HttpEntity<String> labelEntity3 = new HttpEntity<>(label3Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity3, String.class);

        // Add collaborators
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final com.wisemapping.rest.model.RestCollaborationList collabs = new com.wisemapping.rest.model.RestCollaborationList();
        collabs.setMessage("Adding collaborators");
        addCollabToList("collaborator1@example.com", "editor", collabs);
        addCollabToList("collaborator2@example.com", "viewer", collabs);
        
        final HttpEntity<com.wisemapping.rest.model.RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        authenticatedTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Verify mindmap has labels and collaborators
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertTrue(mindmapInfo.get().getLabels().size() >= 3, "Mindmap should have at least 3 labels");

        // Delete the mindmap - this should NOT throw an error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
            
            // Verify mindmap was deleted
            final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
            final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
                .filter(m -> m.getId() == Integer.parseInt(mapId))
                .findAny();
            assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
        }, "Deleting mindmap with multiple labels and collaborators should not throw an error");
    }

    @Test
    public void testDeleteMindmapWithLabelsCollaboratorsAndHistory_ShouldNotThrowError() throws URISyntaxException {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate authenticatedTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new mindmap
        final String mapTitle = "Map with Everything";
        final URI mindmapUri = addNewMap(authenticatedTemplate, mapTitle);
        final String mapId = mindmapUri.getPath().replace("/api/restful/maps/", "");

        // Create and associate labels
        final URI label1Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label A", "#FF0000");
        final URI label2Uri = RestLabelControllerTest.addNewLabel(requestHeaders, authenticatedTemplate, "Label B", "#00FF00");
        final String label1Id = label1Uri.getPath().replace("/api/restful/labels/", "");
        final String label2Id = label2Uri.getPath().replace("/api/restful/labels/", "");

        HttpEntity<String> labelEntity1 = new HttpEntity<>(label1Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity1, String.class);
        
        HttpEntity<String> labelEntity2 = new HttpEntity<>(label2Id, requestHeaders);
        authenticatedTemplate.exchange("/api/restful/maps/" + mapId + "/labels", HttpMethod.POST, labelEntity2, String.class);

        // Add collaborators
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        final com.wisemapping.rest.model.RestCollaborationList collabs = new com.wisemapping.rest.model.RestCollaborationList();
        addCollabToList("collab1@example.com", "editor", collabs);
        final HttpEntity<com.wisemapping.rest.model.RestCollaborationList> updateEntity = new HttpEntity<>(collabs, requestHeaders);
        authenticatedTemplate.put(mindmapUri + "/collabs/", updateEntity);

        // Update mindmap to create history (by updating the title)
        try {
            final com.wisemapping.rest.model.RestMindmap updateMap = new com.wisemapping.rest.model.RestMindmap();
            updateMap.setTitle("Updated Title for History");
            final HttpEntity<com.wisemapping.rest.model.RestMindmap> updateMapEntity = new HttpEntity<>(updateMap, requestHeaders);
            authenticatedTemplate.put(mindmapUri.toString(), updateMapEntity);
        } catch (Exception e) {
            // Ignore history update errors for this test
        }

        // Verify mindmap has labels and collaborators
        final RestMindmapList mindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
        final Optional<RestMindmapInfo> mindmapInfo = mindmapList.getMindmapsInfo().stream()
            .filter(m -> m.getId() == Integer.parseInt(mapId))
            .findAny();
        assertTrue(mindmapInfo.isPresent());
        assertTrue(mindmapInfo.get().getLabels().size() >= 2, "Mindmap should have at least 2 labels");

        // Delete the mindmap - this should NOT throw an error
        assertDoesNotThrow(() -> {
            final ResponseEntity<String> deleteResponse = authenticatedTemplate.exchange(
                mindmapUri.toString(), 
                HttpMethod.DELETE, 
                null, 
                String.class);
            
            assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), 
                "DELETE should succeed");
            
            // Verify mindmap was deleted
            final RestMindmapList deletedMindmapList = fetchMaps(requestHeaders, authenticatedTemplate);
            final Optional<RestMindmapInfo> deletedMindmapInfo = deletedMindmapList.getMindmapsInfo().stream()
                .filter(m -> m.getId() == Integer.parseInt(mapId))
                .findAny();
            assertFalse(deletedMindmapInfo.isPresent(), "Mindmap should be deleted");
        }, "Deleting mindmap with labels, collaborators, and history should not throw an error");
    }

    private com.wisemapping.rest.model.RestCollaboration addCollabToList(String email, String role, com.wisemapping.rest.model.RestCollaborationList collabs) {
        com.wisemapping.rest.model.RestCollaboration collab = new com.wisemapping.rest.model.RestCollaboration();
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

