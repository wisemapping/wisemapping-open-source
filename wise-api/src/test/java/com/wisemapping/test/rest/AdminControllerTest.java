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
import com.wisemapping.rest.AdminController;
import com.wisemapping.rest.model.RestMap;
import com.wisemapping.rest.model.RestUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static com.wisemapping.test.rest.RestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {AppConfig.class, AdminController.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.api.http-basic-enabled=true"}
)
@AutoConfigureMockMvc
public class AdminControllerTest {
    private static final String ADMIN_USER = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "test";
    private static final String REGULAR_USER = "test@wisemapping.org";
    private static final String REGULAR_PASSWORD = "test";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetAllUsers_AdminAccess_Success() {
        // Test that admin can access the users endpoint
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/users", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetAllUsers_RegularUserAccess_Forbidden() {
        // Test that regular user cannot access admin endpoints
        ResponseEntity<String> response = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/users", String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllUsers_NoAuth_Unauthorized() {
        // Test that unauthenticated requests are rejected
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testGetAllMaps_AdminAccess_Success() {
        // Test that admin can access the maps endpoint
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/maps", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetAllMaps_RegularUserAccess_Forbidden() {
        // Test that regular user cannot access admin maps endpoint
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetUserById_AdminAccess_Success() {
        // Test that admin can get user by ID
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<RestUser> response = restTemplate.exchange(
                "/api/restful/admin/users/1",
                HttpMethod.GET,
                entity,
                RestUser.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetUserById_RegularUserAccess_Forbidden() {
        // Test that regular user cannot get user by ID
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetMapById_AdminAccess_Success() {
        // Test that admin can get map by ID
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        // This might return 404 if no map exists, but should not return 403
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetMapById_RegularUserAccess_Forbidden() {
        // Test that regular user cannot get map by ID
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/1",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testCreateUser_AdminAccess_Success() {
        // Test that admin can create users
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestUser newUser = new RestUser();
        newUser.setEmail("newuser@test.com");
        newUser.setFirstname("New");
        newUser.setLastname("User");
        newUser.setPassword("password123");

        HttpEntity<RestUser> entity = new HttpEntity<>(newUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testCreateUser_RegularUserAccess_Forbidden() {
        // Test that regular user cannot create users
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestUser newUser = new RestUser();
        newUser.setEmail("newuser@test.com");
        newUser.setFirstname("New");
        newUser.setLastname("User");
        newUser.setPassword("password123");

        HttpEntity<RestUser> entity = new HttpEntity<>(newUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_AdminAccess_Success() {
        // Test that admin can update users
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestUser updatedUser = new RestUser();
        updatedUser.setFirstname("Updated");
        updatedUser.setLastname("Name");

        HttpEntity<RestUser> entity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<RestUser> response = restTemplate.exchange(
                "/api/restful/admin/users/1",
                HttpMethod.PUT,
                entity,
                RestUser.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateUser_RegularUserAccess_Forbidden() {
        // Test that regular user cannot update users
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestUser updatedUser = new RestUser();
        updatedUser.setFirstname("Updated");
        updatedUser.setLastname("Name");

        HttpEntity<RestUser> entity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users/1",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeleteUser_AdminAccess_Success() {
        // Test that admin can delete users (create a user first, then delete)
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestUser newUser = new RestUser();
        newUser.setEmail("tobedeleted@test.com");
        newUser.setFirstname("To Be");
        newUser.setLastname("Deleted");
        newUser.setPassword("password123");

        HttpEntity<RestUser> entity = new HttpEntity<>(newUser, headers);

        // First create the user
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/restful/admin/users",
                HttpMethod.POST,
                entity,
                String.class
        );

        if (createResponse.getStatusCode() == HttpStatus.CREATED) {
            // Then try to delete (this might fail if user doesn't exist, but should not be forbidden)
            ResponseEntity<String> deleteResponse = restTemplate.exchange(
                    "/api/restful/admin/users/999", // Use a non-existent ID to avoid actually deleting
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );

            // Should not be forbidden (might be 404 or other error)
            assertNotEquals(HttpStatus.FORBIDDEN, deleteResponse.getStatusCode());
        }
    }

    @Test
    public void testDeleteUser_RegularUserAccess_Forbidden() {
        // Test that regular user cannot delete users
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users/1",
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateMap_AdminAccess_Success() {
        // Test that admin can update maps
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestMap updatedMap = new RestMap();
        updatedMap.setTitle("Updated Title");
        updatedMap.setDescription("Updated Description");

        HttpEntity<RestMap> entity = new HttpEntity<>(updatedMap, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/1",
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Should not be forbidden (might be 404 or other error)
        assertNotEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateMap_RegularUserAccess_Forbidden() {
        // Test that regular user cannot update maps
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestMap updatedMap = new RestMap();
        updatedMap.setTitle("Updated Title");
        updatedMap.setDescription("Updated Description");

        HttpEntity<RestMap> entity = new HttpEntity<>(updatedMap, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/1",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeleteMap_AdminAccess_Success() {
        // Test that admin can delete maps
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/999", // Use non-existent ID to avoid actual deletion
                HttpMethod.DELETE,
                entity,
                String.class
        );

        // Should not be forbidden (might be 404 or other error)
        assertNotEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeleteMap_RegularUserAccess_Forbidden() {
        // Test that regular user cannot delete maps
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/maps/1",
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testChangePassword_AdminAccess_Success() {
        // Test that admin can change user passwords
        HttpHeaders headers = createAuthHeaders(ADMIN_USER, ADMIN_PASSWORD);
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> entity = new HttpEntity<>("newpassword123", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users/1/password",
                HttpMethod.PUT,
                entity,
                String.class
        );

        // Should not be forbidden (might be 404 or other error)
        assertNotEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testChangePassword_RegularUserAccess_Forbidden() {
        // Test that regular user cannot change user passwords
        HttpHeaders headers = createAuthHeaders(REGULAR_USER, REGULAR_PASSWORD);
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> entity = new HttpEntity<>("newpassword123", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/restful/admin/users/1/password",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetMapXml_AdminAccess() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/maps/" + testMindmap.getId() + "/xml", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // XML content should contain mindmap data
        assertTrue(response.getBody().contains("<?xml"));
    }

    @Test
    void testGetMapXml_RegularUserAccessDenied() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/maps/" + testMindmap.getId() + "/xml", String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetMapXml_UnauthenticatedAccessDenied() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/restful/admin/maps/" + testMindmap.getId() + "/xml", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetMapXml_MapNotFound() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/maps/99999/xml", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
