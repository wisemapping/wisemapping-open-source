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
import com.wisemapping.rest.model.RestJwtUser;
import com.wisemapping.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestJwtAuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void generateTokenValidUser() throws Exception {
        final RestJwtUser user = new RestJwtUser("test@wisemapping.org", "password");
        final String userJson = objectMapper.writeValueAsString(user);

        final MvcResult result = mockMvc.perform(
                        post("/api/restful/authenticate").
                                contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                .andExpect(status().isOk()).andReturn();


        assertThat(jwtTokenUtil.validateJwtToken(result.getResponse().getContentAsString())).isTrue();
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
