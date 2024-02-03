/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.test.rest;


import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.rest.AdminController;
import com.wisemapping.rest.MindmapController;
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;

import static com.wisemapping.test.rest.RestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = {RestAppConfig.class, CommonConfig.class, MindmapController.class, AdminController.class, UserController.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestAccountControllerTest {
    private static final String ADMIN_USER = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "test";

    @Autowired
    private TestRestTemplate restTemplate;

    static public RestAccountControllerTest create(@NotNull TestRestTemplate restTemplate) {
        final RestAccountControllerTest result = new RestAccountControllerTest();
        result.restTemplate = restTemplate;
        return result;
    }

    @Test
    public void deleteUser() {    // Configure media types ...

        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate adminRestTemplate = this.restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD);

        final RestUser dummyUser = createDummyUser();
        createUser(requestHeaders, adminRestTemplate, dummyUser);

        // Delete user ...
        final TestRestTemplate dummyTemplate = this.restTemplate.withBasicAuth(dummyUser.getEmail(), "fooPassword");
        dummyTemplate.delete(BASE_REST_URL + "/account");

        // Is the user there ?
        // Check that the user has been created ...
//        try {
//            findUser(requestHeaders, adminTemplate, location);
//            fail("User could not be deleted !");
//        } catch (Exception e) {
//        }
    }

    @Test
    public RestUser createNewUser() {
        // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate templateRest = this.restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD);

        // Fill user data ...
        final RestUser restUser = createDummyUser();

        // Create a new user ...
        final URI location = createUser(requestHeaders, templateRest, restUser);

        // Check that the user has been created ...
        ResponseEntity<RestUser> result = findUser(requestHeaders, templateRest, location);
        assertEquals(result.getBody().getEmail(), restUser.getEmail(), "Returned object object seems not be the same.");

        // Find by email and check ...
        result = findUserByEmail(requestHeaders, templateRest, restUser.getEmail());
        assertEquals(result.getBody().getEmail(), restUser.getEmail(), "Returned object object seems not be the same.");

        // Assign generated id ...
        restUser.setId(result.getBody().getId());
        return restUser;

    }

    private ResponseEntity<RestUser> findUser(HttpHeaders requestHeaders, TestRestTemplate templateRest, URI location) {
        HttpEntity<RestUser> findUserEntity = new HttpEntity<>(requestHeaders);
        return templateRest.exchange(location.toString(), HttpMethod.GET, findUserEntity, RestUser.class);
    }

    private ResponseEntity<RestUser> findUserByEmail(HttpHeaders requestHeaders, TestRestTemplate templateRest, final String email) {
        HttpEntity<RestUser> findUserEntity = new HttpEntity<>(requestHeaders);

        // Add extension only to avoid the fact that the last part is extracted ...
        final String url = BASE_REST_URL + "/admin/users/email/{email}";
        return templateRest.exchange(url, HttpMethod.GET, findUserEntity, RestUser.class, email);
    }

    private URI createUser(@NotNull HttpHeaders requestHeaders, TestRestTemplate templateRest, RestUser restUser) {
        final HttpEntity<RestUser> createUserEntity = new HttpEntity<>(restUser, requestHeaders);
        return templateRest.postForLocation(BASE_REST_URL + "/admin/users", createUserEntity);
    }

    private RestUser createDummyUser() {
        final RestUser restUser = new RestUser();
        final String username = "foo-to-delete" + System.nanoTime();
        final String email = username + "@example.org";
        restUser.setEmail(email);
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("fooPassword");
        return restUser;
    }
}
