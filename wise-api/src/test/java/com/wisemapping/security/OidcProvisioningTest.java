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

package com.wisemapping.security;

import com.wisemapping.config.AppConfig;
import com.wisemapping.model.Account;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login through the generic OpenID Connect provider (registration id "oidc").
 *
 * <p>Self-registration is disabled on purpose: an instance that delegates identity to
 * its own IdP closes the public sign-up form, and the first OIDC login must still
 * provision the account.
 */
@SpringBootTest(
        classes = {AppConfig.class},
        properties = {
                "app.registration.enabled=false",
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
@DisplayName("Generic OIDC provisioning")
class OidcProvisioningTest {

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("First OIDC login provisions an account even when self-registration is disabled")
    void firstLoginProvisionsAccountWithRegistrationDisabled() throws Exception {
        final String email = "oidc-new-" + System.nanoTime() + "@example.org";

        final MockHttpServletResponse response = login(email, "sub-" + System.nanoTime());

        assertTrue(response.getRedirectedUrl().contains("/c/oauth-callback"),
                "unexpected redirect: " + response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().contains("provider=oidc"),
                "the frontend needs the provider to confirm an account link: " + response.getRedirectedUrl());
        final Account account = userService.getUserBy(email);
        assertNotNull(account, "the OIDC login must provision the account");
        assertEquals(AuthenticationType.OIDC, account.getAuthenticationType());
        assertEquals("Jane", account.getFirstname());
        assertEquals("Doe", account.getLastname());
        assertEquals("", account.getPassword(), "an OIDC account must not get a usable local password");
    }

    @Test
    @DisplayName("Self-registration stays closed while OIDC provisioning works")
    void selfRegistrationIsRejected() throws Exception {
        mockMvc.perform(post("/api/restful/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"form-" + System.nanoTime() + "@example.org\",\"firstname\":\"A\","
                                + "\"lastname\":\"B\",\"password\":\"Secret123!\",\"acceptedTerms\":true}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("An existing local account is linked to OIDC after sync confirmation")
    void existingDatabaseAccountIsLinked() throws Exception {
        final String email = "oidc-existing-" + System.nanoTime() + "@example.org";
        final Account local = new Account();
        local.setEmail(email);
        local.setFirstname("Jane");
        local.setLastname("Doe");
        local.setPassword("LocalPassword123");
        local.setAuthenticationType(AuthenticationType.DATABASE);
        authenticate(token(email, "sub-existing"));
        userService.createUser(local, false, false);

        final MockHttpServletResponse response = login(email, "sub-existing");

        final Account pending = userService.getUserBy(email);
        assertEquals(AuthenticationType.DATABASE, pending.getAuthenticationType(),
                "linking requires an explicit confirmation");
        assertNotNull(pending.getSyncCode());
        assertTrue(response.getRedirectedUrl().contains("syncCode="));

        // The frontend confirms the link with the JWT issued by the success handler.
        final UserDetails principal = userDetailsService.loadUserByUsername(email);
        final var jwtAuthentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        jwtAuthentication.setDetails(new Object());
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(jwtAuthentication);
        SecurityContextHolder.setContext(context);

        final Account linked = userService.confirmAccountSync(email, pending.getSyncCode(), "oidc");

        assertEquals(AuthenticationType.OIDC, linked.getAuthenticationType());
        assertNull(linked.getSyncCode());
    }

    private MockHttpServletResponse login(String email, String subject) throws Exception {
        final OAuth2AuthenticationToken authentication = token(email, subject);
        // Spring Security stores the authentication in the SecurityContext before it
        // invokes the success handler (AbstractAuthenticationProcessingFilter).
        authenticate(authentication);

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/oidc");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(request, response, authentication);
        return response;
    }

    private static void authenticate(OAuth2AuthenticationToken authentication) {
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private static OAuth2AuthenticationToken token(String email, String subject) {
        final OidcIdToken idToken = new OidcIdToken("id-token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("sub", subject, "email", email, "given_name", "Jane", "family_name", "Doe",
                        "iss", "https://sso.example.com"));
        final var authorities = List.of(new SimpleGrantedAuthority("OIDC_USER"));
        final DefaultOidcUser user = new DefaultOidcUser(authorities, idToken, "sub");
        return new OAuth2AuthenticationToken(user, authorities, "oidc");
    }
}
