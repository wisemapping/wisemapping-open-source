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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("App Controller Tests")
class RestAppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("Should return application configuration successfully")
    void shouldReturnApplicationConfiguration() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/restful/app/config"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwtExpirationMin").value(10080))
                .andExpect(jsonPath("$.jwtExpirationMin").isNumber())
                .andExpect(jsonPath("$").isMap())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("jwtExpirationMin");
        assertThat(responseContent).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Should return proper content type")
    void shouldReturnProperContentType() throws Exception {
        mockMvc.perform(get("/api/restful/app/config"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    @Order(3)
    @DisplayName("Should validate response structure")
    void shouldValidateResponseStructure() throws Exception {
        mockMvc.perform(get("/api/restful/app/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtExpirationMin").exists())
                .andExpect(jsonPath("$.jwtExpirationMin").isNumber())
                .andExpect(jsonPath("$.jwtExpirationMin").value(10080));
    }
}
