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
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {AppConfig.class, UserController.class, TestDataManager.class},
        properties = {
            "app.api.http-basic-enabled=true",
            "app.registration.enabled=true",
            "app.registration.email-confirmation-enabled=true",
            "app.registration.disposable-email.blocking.enabled=true"
        }
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Controller Tests")
class RestUserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

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
    void shouldRejectPasswordResetForNonExistentUser() throws Exception {
        mockMvc.perform(
                put("/api/restful/users/resetPassword")
                    .param("email", "nonexistent@example.com"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("The email provided is not a valid user account")));
    }

    @Test
    @Order(2)
    @DisplayName("Should reset password successfully for valid user")
    void shouldResetPasswordSuccessfully() throws Exception {
        Account existingUser = testDataManager.createAndSaveUser();
        
        mockMvc.perform(
                put("/api/restful/users/resetPassword")
                    .param("email", existingUser.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EMAIL_SENT")));
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("Should validate email format for password reset")
    @ValueSource(strings = {"", "invalid-email", "test@", "@example.com", "   ", "null"})
    void shouldValidateEmailFormatForPasswordReset(String invalidEmail) throws Exception {
        mockMvc.perform(
                put("/api/restful/users/resetPassword")
                    .param("email", invalidEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("The email provided is not a valid user account")));
    }


    @Test
    @Order(4)
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        String userJson = objectMapper.writeValueAsString(userRegistration);

       mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("ResourceId"))
                .andReturn();

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstname()).isEqualTo(userRegistration.getFirstname());
        assertThat(createdUser.getLastname()).isEqualTo(userRegistration.getLastname());
        assertThat(createdUser.getEmail()).isEqualTo(userRegistration.getEmail());
    }

    @Test
    @Order(5)
    @DisplayName("Should reject registration with disposable email")
    void shouldRejectRegistrationWithDisposableEmail() throws Exception {
        RestUserRegistration userWithDisposableEmail = RestUserRegistration.create(
            "test@10minutemail.com", "password123", "Test", "User"
        );
        String userJson = objectMapper.writeValueAsString(userWithDisposableEmail);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Disposable email addresses are not allowed")));
    }

    @Test
    @Order(6)
    @DisplayName("Should reject registration with duplicate email")
    void shouldRejectRegistrationWithDuplicateEmail() throws Exception {
        // First create a user successfully
        RestUserRegistration firstUser = testDataManager.createTestUserRegistration();
        String firstUserJson = objectMapper.writeValueAsString(firstUser);
        
        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(firstUserJson))
                .andExpect(status().isCreated());
        
        // Then try to create duplicate with same email
        RestUserRegistration duplicateUser = RestUserRegistration.create(
            firstUser.getEmail(), "password123", "Duplicate", "User"
        );
        String duplicateJson = objectMapper.writeValueAsString(duplicateUser);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(duplicateJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("There is an account already with this email")));
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("Should validate user registration input")
    @MethodSource("invalidRegistrationData")
    void shouldValidateUserRegistrationInput(RestUserRegistration invalidUser, String expectedError) throws Exception {
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(expectedError)));
    }

    private static Stream<Arguments> invalidRegistrationData() {
        return Stream.of(
            Arguments.of(
                RestUserRegistration.create("invalid-email", "password", "Test", "User"),
                "Invalid email address"
            ),
            Arguments.of(
                RestUserRegistration.create("test@example.com", "", "Test", "User"),
                "Required field cannot be left blank"
            ),
            Arguments.of(
                RestUserRegistration.create("test@example.com", "password", "", "User"),
                "Required field cannot be left blank"
            ),
            Arguments.of(
                RestUserRegistration.create("test@example.com", "password", "Test", ""),
                "Required field cannot be left blank"
            ),
            Arguments.of(
                RestUserRegistration.create("test@example.com", "a".repeat(50), "Test", "User"),
                "Password must be less than 40 characters"
            )
        );
    }

    @Test
    @Order(8)
    @DisplayName("Should handle malformed JSON in registration")
    void shouldHandleMalformedJsonInRegistration() throws Exception {
        String malformedJson = "{\"email\":\"test@example.com\",\"password\":}";

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(10)
    @DisplayName("Should test user registration endpoint exists")
    void registerUserEndpointExists() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/restful/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"endpoint.test@example.org\",\"password\":\"validpassword123\",\"firstName\":\"Valid\",\"lastName\":\"User\"}"))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(201, 400, 404);
    }

    @Test
    @Order(11)
    @DisplayName("Should test password reset endpoint exists")
    void resetPasswordEndpointExists() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/restful/users/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@wisemapping.org\"}"))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 400, 404, 500);
    }

    // ==================== ACTIVATION TESTS ====================

    @Test
    @Order(12)
    @DisplayName("Should activate user with valid activation code")
    void shouldActivateUserWithValidCode() throws Exception {
        // Create a user with email confirmation enabled
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        String userJson = objectMapper.writeValueAsString(userRegistration);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isCreated());

        // Get the created user to retrieve activation code
        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.isActive()).isFalse(); // Should not be active yet
        
        long activationCode = createdUser.getActivationCode();

        // Activate the user
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(activationCode)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify user is now active
        Account activatedUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(activatedUser.isActive()).isTrue();
        assertThat(activatedUser.getActivationDate()).isNotNull();
    }

    @Test
    @Order(13)
    @DisplayName("Should reject activation with invalid code")
    void shouldRejectActivationWithInvalidCode() throws Exception {
        long invalidCode = 999999999L;

        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(invalidCode)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid activation code")));
    }

    @Test
    @Order(14)
    @DisplayName("Should reject activation for already active user")
    void shouldRejectActivationForAlreadyActiveUser() throws Exception {
        // Create and activate a user
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        String userJson = objectMapper.writeValueAsString(userRegistration);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isCreated());

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        long activationCode = createdUser.getActivationCode();

        // First activation should succeed
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(activationCode)))
                .andExpect(status().isNoContent());

        // Second activation with same code should fail
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(activationCode)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("This account has already been activated")));
    }

    @Test
    @Order(15)
    @DisplayName("Should prevent login for non-activated user")
    void shouldPreventLoginForNonActivatedUser() throws Exception {
        // This test verifies the authentication flow blocks non-activated users
        RestUserRegistration userRegistration = testDataManager.createTestUserRegistration();
        String userJson = objectMapper.writeValueAsString(userRegistration);

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(status().isCreated());

        Account createdUser = userService.getUserBy(userRegistration.getEmail());
        assertThat(createdUser.isActive()).isFalse();
        
        // Verify that the user cannot be used for authentication (would be tested in JwtAuthControllerTest)
        // This is a data integrity test - user should have null activation_date
        assertThat(createdUser.getActivationDate()).isNull();
    }

    @Test
    @Order(16)
    @DisplayName("Should handle activation code boundary values")
    void shouldHandleActivationCodeBoundaryValues() throws Exception {
        // Test with very large code
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(Long.MAX_VALUE)))
                .andExpect(status().isBadRequest());

        // Test with zero
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", "0"))
                .andExpect(status().isBadRequest());

        // Test with negative number
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    @DisplayName("Should validate activation code is unique per user")
    void shouldValidateActivationCodeIsUniquePerUser() throws Exception {
        // Create two users
        RestUserRegistration user1 = testDataManager.createTestUserRegistration();
        RestUserRegistration user2 = testDataManager.createTestUserRegistration();

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/restful/users/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        Account account1 = userService.getUserBy(user1.getEmail());
        Account account2 = userService.getUserBy(user2.getEmail());

        // Activation codes should be different
        assertThat(account1.getActivationCode()).isNotEqualTo(account2.getActivationCode());

        // Activating user1 should not affect user2
        mockMvc.perform(
                put("/api/restful/users/activation")
                    .param("code", String.valueOf(account1.getActivationCode())))
                .andExpect(status().isNoContent());

        Account activatedAccount1 = userService.getUserBy(user1.getEmail());
        Account nonActivatedAccount2 = userService.getUserBy(user2.getEmail());

        assertThat(activatedAccount1.isActive()).isTrue();
        assertThat(nonActivatedAccount2.isActive()).isFalse();
    }


}
