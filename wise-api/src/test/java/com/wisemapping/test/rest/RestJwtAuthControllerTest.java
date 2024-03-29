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
import com.wisemapping.rest.JwtAuthController;
import com.wisemapping.rest.model.RestJwtUser;
import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {RestAppConfig.class, CommonConfig.class, JwtAuthController.class},
        properties = {"app.api.http-basic-enabled=true"}
)
@AutoConfigureMockMvc
public class RestJwtAuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void generateTokenValidUser() throws Exception {
        final RestJwtUser user = new RestJwtUser("test@wisemapping.org", "test");
        final String userJson = objectMapper.writeValueAsString(user);

        final MvcResult result = mockMvc.perform(
                        post("/api/restful/authenticate").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                .andExpect(status().isOk()).andReturn();


        assertTrue(jwtTokenUtil.validateJwtToken(result.getResponse().getContentAsString()));
    }

    @Test
    void generateTokenInvalidPassword() throws Exception {
        final RestJwtUser user = new RestJwtUser("test@wisemapping.org", "test1");
        final String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(
                        post("/api/restful/authenticate").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void generateTokenInvalidPasswordUser() throws Exception {
        final RestJwtUser user = new RestJwtUser("test-not-exist@wisemapping.org", "test");
        final String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(
                        post("/api/restful/authenticate").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                .andExpect(status().is4xxClientError());


    }
}
