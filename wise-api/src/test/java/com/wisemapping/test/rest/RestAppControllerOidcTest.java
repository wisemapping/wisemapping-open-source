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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Generic OpenID Connect provider (registration id "oidc"). Explicit provider endpoints are
 * used instead of an issuer-uri so that the context starts without network discovery.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.site.api-base-url=https://wm.example.com",
                "app.oauth.oidc.label=Company SSO",
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
@DisplayName("App Controller Tests - generic OIDC provider")
class RestAppControllerOidcTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should expose the generic OIDC provider when it is configured")
    void shouldExposeOidcProvider() throws Exception {
        mockMvc.perform(get("/api/restful/app/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oidcOauth2Enabled").value(true))
                .andExpect(jsonPath("$.oidcOauth2Url").value("https://wm.example.com/oauth2/authorization/oidc"))
                .andExpect(jsonPath("$.oidcOauth2Label").value("Company SSO"));
    }

    @Test
    @DisplayName("Should redirect the OIDC authorization endpoint to the identity provider")
    void shouldRedirectToIdentityProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/oidc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.startsWith("https://sso.example.com/authorize?")));
    }
}
