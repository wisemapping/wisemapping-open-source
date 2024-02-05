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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.model.User;
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static com.wisemapping.test.rest.RestHelper.createDummyUser;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {RestAppConfig.class, CommonConfig.class, UserController.class})
@AutoConfigureMockMvc
public class RestUserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;


    private RestUser createUser() throws Exception {
        final RestUser result = createDummyUser();
        final String userJson = objectMapper.writeValueAsString(result);

        mockMvc.perform(
                        post("/api/restfull/admin/users").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson)
                                .with(user("test@wisemapping.org").roles("ADMIN")))
                .andExpect(status().isCreated());

        // Check dao ...
        User userBy = userService.getUserBy(result.getEmail());
        assertNotNull(userBy);
        return result;
    }

    @Test
    void resetPasswordInvalidUser() throws Exception {
        this.mockMvc.perform
                        (put("/api/restfull/users/resetPassword?email=doesnotexist@example.com"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("The email provided is not a valid user account.")));
    }

    @Test
    void resetPasswordValidUser() throws Exception {
        final RestUser user = createUser();
        this.mockMvc.perform
                        (put("/api/restfull/users/resetPassword?email=" + user.getEmail()))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @Disabled
    void registerNewUser() throws Exception {
        final RestUserRegistration user = RestUserRegistration.create("some@example.com", "somepass", "Test", "registation");
        final String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(
                        post("/api/restfull/users/").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                .andExpect(status().isCreated());

        // Check dao ...
        User userBy = userService.getUserBy(user.getEmail());
        assertNotNull(userBy);
    }


}
