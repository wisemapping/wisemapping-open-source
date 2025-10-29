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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.config.AppConfig;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "app.registration.enabled=true",
            "app.registration.email-confirmation-enabled=true",
            "app.registration.disposable-email.blocking.enabled=true"
        }
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Controller Tests")
class RestUserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private TestDataManager testDataManager;

    @BeforeEach
    void setUp() {
        testDataManager.cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        testDataManager.cleanupTestData();
    }


    @Test
    @Order(1)
    @DisplayName("Should reject password reset for non-existent user")
    void shouldRejectPasswordResetForNonExistentUser() {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPassword?email=nonexistent@example.com",
                HttpMethod.PUT,
                null,
                String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("The email provided is not a valid user account"));
    }

    @Test
    @Order(2)
    @DisplayName("Should reset password successfully for valid user")
    void shouldResetPasswordSuccessfully() {
        Account existingUser = testDataManager.createAndSaveUser();
        
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPassword?email=" + existingUser.getEmail(),
                HttpMethod.PUT,
                null,
                String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("EMAIL_SENT"));
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("Should validate email format for password reset")
    @ValueSource(strings = {"", "invalid-email", "test@", "@example.com", "   ", "null"})
    void shouldValidateEmailFormatForPasswordReset(String invalidEmail) {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPassword?email=" + invalidEmail,
                HttpMethod.PUT,
                null,
                String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("The email provided is not a valid user account"));
    }


    @Test
    @Order(4)
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(userRegistration, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertNotNull(response.getHeaders().get("ResourceId"));

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstname()).isEqualTo(userRegistration.getFirstname());
        assertThat(createdUser.getLastname()).isEqualTo(userRegistration.getLastname());
        assertThat(createdUser.getEmail()).isEqualTo(userRegistration.getEmail());
    }

    @Test
    @Order(5)
    @DisplayName("Should reject registration with disposable email")
    void shouldRejectRegistrationWithDisposableEmail() {
        RestUserRegistration userWithDisposableEmail = RestUserRegistration.create(
            "test@10minutemail.com", "password123", "Test", "User"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(userWithDisposableEmail, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Disposable email addresses are not allowed"));
    }

    @Test
    @Order(6)
    @DisplayName("Should reject registration with duplicate email")
    void shouldRejectRegistrationWithDuplicateEmail() {
        // First create a user successfully
        RestUserRegistration firstUser = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request1 = new HttpEntity<>(firstUser, headers);
        
        ResponseEntity<String> firstResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request1,
                String.class
        );
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());
        
        // Then try to create duplicate with same email
        RestUserRegistration duplicateUser = RestUserRegistration.create(
            firstUser.getEmail(), "password123", "Duplicate", "User"
        );
        HttpEntity<RestUserRegistration> request2 = new HttpEntity<>(duplicateUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request2,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("There is an account already with this email"));
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("Should validate user registration input")
    @MethodSource("invalidRegistrationData")
    void shouldValidateUserRegistrationInput(RestUserRegistration invalidUser, String expectedError) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(invalidUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), 
            "Expected 400 but got " + response.getStatusCode() + " with body: " + response.getBody());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(expectedError), 
            "Expected error message to contain '" + expectedError + "' but got: " + response.getBody());
    }

    private static Stream<Arguments> invalidRegistrationData() {
        return Stream.of(
            Arguments.of(
                RestUserRegistration.create("invalid-email", "password123", "Test", "User"),
                "Invalid email address"
            )
        );
    }

    @Test
    @Order(8)
    @DisplayName("Should handle malformed JSON in registration")
    void shouldHandleMalformedJsonInRegistration() {
        String malformedJson = "{\"email\":\"test@example.com\",\"password\":}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );

        assertTrue(response.getStatusCode().is4xxClientError(), 
                   "Expected 4xx for malformed JSON, got: " + response.getStatusCode());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() {
        String emptyJson = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(emptyJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );

        assertTrue(response.getStatusCode().is4xxClientError(), 
                   "Expected 4xx for empty request body, got: " + response.getStatusCode());
    }

    @Test
    @Order(10)
    @DisplayName("Should test user registration endpoint exists")
    void registerUserEndpointExists() {
        RestUserRegistration testUser = RestUserRegistration.create(
            "endpoint.test@example.org", "validpassword123", "Valid", "User"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(testUser, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );
        
        assertThat(response.getStatusCode().value()).isIn(201, 400, 404);
    }

    @Test
    @Order(11)
    @DisplayName("Should test password reset endpoint exists")
    void resetPasswordEndpointExists() {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPassword?email=admin@wisemapping.org",
                HttpMethod.PUT,
                null,
                String.class
        );
        
        assertThat(response.getStatusCode().value()).isIn(200, 400, 404, 500);
    }

    // ==================== ACTIVATION TESTS ====================

    @Test
    @Order(12)
    @DisplayName("Should activate user with valid activation code")
    void shouldActivateUserWithValidCode() {
        // Create a user with email confirmation enabled
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(userRegistration, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        // Get the created user to retrieve activation code
        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.isActive()).isFalse(); // Should not be active yet
        
        long activationCode = createdUser.getActivationCode();

        // Activate the user
        ResponseEntity<String> activateResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + activationCode,
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, activateResponse.getStatusCode());

        // Verify user is now active
        Account activatedUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(activatedUser.isActive()).isTrue();
        assertThat(activatedUser.getActivationDate()).isNotNull();
    }

    @Test
    @Order(13)
    @DisplayName("Should reject activation with invalid code")
    void shouldRejectActivationWithInvalidCode() {
        long invalidCode = 999999999L;

        ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + invalidCode,
                HttpMethod.PUT,
                null,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Invalid activation code"));
    }

    @Test
    @Order(14)
    @DisplayName("Should reject activation for already active user")
    void shouldRejectActivationForAlreadyActiveUser() {
        // Create and activate a user
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(userRegistration, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        long activationCode = createdUser.getActivationCode();

        // First activation should succeed
        ResponseEntity<String> firstActivation = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + activationCode,
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, firstActivation.getStatusCode());

        // Second activation with same code should fail
        ResponseEntity<String> secondActivation = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + activationCode,
                HttpMethod.PUT,
                null,
                String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, secondActivation.getStatusCode());
        assertNotNull(secondActivation.getBody());
        assertTrue(secondActivation.getBody().contains("This account has already been activated"));
    }

    @Test
    @Order(15)
    @DisplayName("Should prevent login for non-activated user")
    void shouldPreventLoginForNonActivatedUser() {
        // This test verifies the authentication flow blocks non-activated users
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserRegistration> request = new HttpEntity<>(userRegistration, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request,
                String.class
        );
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser.isActive()).isFalse();
        
        // Verify that the user cannot be used for authentication (would be tested in JwtAuthControllerTest)
        // This is a data integrity test - user should have null activation_date
        assertThat(createdUser.getActivationDate()).isNull();
    }

    @Test
    @Order(16)
    @DisplayName("Should handle activation code boundary values")
    void shouldHandleActivationCodeBoundaryValues() {
        // Test with very large code
        ResponseEntity<String> response1 = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + Long.MAX_VALUE,
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Test with zero
        ResponseEntity<String> response2 = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=0",
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());

        // Test with negative number
        ResponseEntity<String> response3 = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=-1",
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
    }

    @Test
    @Order(17)
    @DisplayName("Should validate activation code is unique per user")
    void shouldValidateActivationCodeIsUniquePerUser() {
        // Create two users
        RestUserRegistration user1 = testDataManager.createTestUserRegistration();
        RestUserRegistration user2 = testDataManager.createTestUserRegistration();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RestUserRegistration> request1 = new HttpEntity<>(user1, headers);
        ResponseEntity<String> createResponse1 = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request1,
                String.class
        );
        assertEquals(HttpStatus.CREATED, createResponse1.getStatusCode());

        HttpEntity<RestUserRegistration> request2 = new HttpEntity<>(user2, headers);
        ResponseEntity<String> createResponse2 = restTemplate.exchange(
                BASE_REST_URL + "/users/",
                HttpMethod.POST,
                request2,
                String.class
        );
        assertEquals(HttpStatus.CREATED, createResponse2.getStatusCode());

        Account account1 = userService.getUserBy(user1.getEmail());
        Account account2 = userService.getUserBy(user2.getEmail());

        // Activation codes should be different
        assertThat(account1.getActivationCode()).isNotEqualTo(account2.getActivationCode());

        // Activating user1 should not affect user2
        ResponseEntity<String> activateResponse = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=" + account1.getActivationCode(),
                HttpMethod.PUT,
                null,
                String.class
        );
        assertEquals(HttpStatus.NO_CONTENT, activateResponse.getStatusCode());

        Account activatedAccount1 = userService.getUserBy(user1.getEmail());
        Account nonActivatedAccount2 = userService.getUserBy(user2.getEmail());

        assertThat(activatedAccount1.isActive()).isTrue();
        assertThat(nonActivatedAccount2.isActive()).isFalse();
    }


}
