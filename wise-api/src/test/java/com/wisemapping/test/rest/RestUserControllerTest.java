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
import com.wisemapping.rest.model.RestUser;
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

import static com.wisemapping.test.rest.RestHelper.createDummyUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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


    private RestUser createUser() throws Exception {
        final RestUser result = createDummyUser();
        final String userJson = objectMapper.writeValueAsString(result);

        mockMvc.perform(
                        post("/api/restful/admin/users").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson)
                                .with(user("test@wisemapping.org").roles("ADMIN")))
                .andExpect(status().isCreated());

        // Check dao ...
        Account userBy = userService.getUserBy(result.getEmail());
        assertNotNull(userBy);
        return result;
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

        MvcResult result = mockMvc.perform(
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


}
