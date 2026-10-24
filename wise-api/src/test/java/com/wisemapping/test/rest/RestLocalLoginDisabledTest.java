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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * With the generic OpenID Connect provider configured, local (database) accounts can no longer
 * sign in with email and password, and nobody can register: OIDC is the way in.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.security.oauth2.client.registration.oidc.client-id=wisemapping",
                "spring.security.oauth2.client.registration.oidc.client-secret=secret",
                "spring.security.oauth2.client.registration.oidc.authorization-grant-type=authorization_code",
                "spring.security.oauth2.client.registration.oidc.scope=openid,profile,email",
                "spring.security.oauth2.client.registration.oidc.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
                "spring.security.oauth2.client.provider.oidc.authorization-uri=https://sso.example.com/authorize",
                "spring.security.oauth2.client.provider.oidc.token-uri=https://sso.example.com/token",
                "spring.security.oauth2.client.provider.oidc.jwk-set-uri=https://sso.example.com/jwks",
                "spring.security.oauth2.client.provider.oidc.user-info-uri=https://sso.example.com/userinfo",
                "spring.security.oauth2.client.provider.oidc.user-name-attribute=sub"
        })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Local login is disabled while OpenID Connect is configured")
class RestLocalLoginDisabledTest {

    // Seeded local account (data-hsqldb.sql)
    private static final String LOCAL_EMAIL = "test@wisemapping.org";
    private static final String LOCAL_PASSWORD = "password";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("The app config tells the frontend to hide the email/password form and sign-up")
    void configDisablesLocalLoginAndRegistration() throws Exception {
        mockMvc.perform(get("/api/restful/app/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oidcOauth2Enabled").value(true))
                .andExpect(jsonPath("$.localLoginEnabled").value(false))
                .andExpect(jsonPath("$.registrationEnabled").value(false));
    }

    @Test
    @DisplayName("Email/password login of a local account is refused with a Single Sign-On hint")
    void passwordLoginIsRefused() throws Exception {
        final String body = objectMapper.writeValueAsString(new RestJwtUser(LOCAL_EMAIL, LOCAL_PASSWORD));

        mockMvc.perform(post("/api/restful/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.globalErrors[0]", containsString("Single Sign-On")));
    }

    @Test
    @DisplayName("HTTP Basic cannot be used to bypass the disabled local login")
    void httpBasicIsRefused() throws Exception {
        mockMvc.perform(get("/api/restful/account").with(httpBasic(LOCAL_EMAIL, LOCAL_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Self-registration is refused")
    void registrationIsRefused() throws Exception {
        mockMvc.perform(post("/api/restful/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"local-" + System.nanoTime() + "@example.org\",\"firstname\":\"A\","
                                + "\"lastname\":\"B\",\"password\":\"Secret123!\",\"acceptedTerms\":true}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Password reset sends no link and points the user to the external provider")
    void passwordResetPointsToExternalProvider() throws Exception {
        mockMvc.perform(put("/api/restful/users/resetPassword").param("email", LOCAL_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("OAUTH2_USER"));
    }
}
