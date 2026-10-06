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
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static com.wisemapping.test.rest.RestHelper.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@code PUT /api/restful/oauth2/confirmaccountsync}
 * ({@link com.wisemapping.rest.OAuth2Controller#confirmAccountSync}).
 *
 * <p>Only the rejection paths can be driven end-to-end without a real identity
 * provider: a sync code is only ever written by the OAuth2 success handler, so
 * a plain DATABASE account never has one and every confirmation attempt for it
 * must fail.
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestOAuth2ControllerTest {

    private static final String CONFIRM_SYNC_URL = "/api/restful/oauth2/confirmaccountsync";

    private TestRestTemplate restTemplate;
    private RestUser user;

    @LocalServerPort
    private int port;

    @BeforeEach
    void createUser() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
        this.user = createTestUser(restTemplate, "testPassword123");
    }

    @Test
    @DisplayName("confirmaccountsync rejects a code for an account that has no pending sync code")
    void confirmSyncRejectsCodeForAccountWithoutPendingSync() {
        final ResponseEntity<String> response = confirmSync(user.getEmail(), "not-the-right-code", null);

        assertTrue(response.getStatusCode().is4xxClientError(),
                "A confirmation without a pending sync code must be rejected, got: "
                        + response.getStatusCode() + " - " + response.getBody());
    }

    @Test
    @DisplayName("confirmaccountsync rejects an unknown email without leaking whether the account exists")
    void confirmSyncRejectsUnknownEmail() {
        final String unknownEmail = "no-such-account-" + System.nanoTime() + "@example.org";

        final ResponseEntity<String> response = confirmSync(unknownEmail, "any-code", "google");

        assertTrue(response.getStatusCode().is4xxClientError(),
                "A confirmation for an unknown account must be rejected, got: " + response.getStatusCode());
    }

    @Test
    @DisplayName("confirmaccountsync rejects a code even when a provider hint is supplied")
    void confirmSyncRejectsInvalidCodeWithFacebookProviderHint() {
        final ResponseEntity<String> response = confirmSync(user.getEmail(), "another-wrong-code", "facebook");

        assertTrue(response.getStatusCode().is4xxClientError(),
                "The provider hint must not bypass the code check, got: " + response.getStatusCode());
    }

    @Test
    @DisplayName("confirmaccountsync requires authentication")
    void confirmSyncRejectsAnonymousCallers() {
        final HttpHeaders headers = createHeaders(MediaType.APPLICATION_JSON);
        final ResponseEntity<String> response = restTemplate.exchange(
                CONFIRM_SYNC_URL + "?email=" + encode(user.getEmail()) + "&code=whatever",
                HttpMethod.PUT, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "An unauthenticated confirmation must be rejected with 401");
    }

    @NotNull
    private ResponseEntity<String> confirmSync(@NotNull String email, @NotNull String code,
                                               @Nullable String provider) {
        final HttpHeaders headers = createHeaders(MediaType.APPLICATION_JSON);
        final StringBuilder url = new StringBuilder(CONFIRM_SYNC_URL)
                .append("?email=").append(encode(email))
                .append("&code=").append(encode(code));
        if (provider != null) {
            url.append("&provider=").append(encode(provider));
        }

        return restTemplate.withBasicAuth(user.getEmail(), user.getPassword())
                .exchange(url.toString(), HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }

    @NotNull
    private static String encode(@NotNull String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
