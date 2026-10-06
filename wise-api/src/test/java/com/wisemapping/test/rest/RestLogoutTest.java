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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
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

import javax.crypto.SecretKey;
import java.util.Date;

import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Covers {@code POST /api/restful/logout}
 * ({@link com.wisemapping.rest.JwtAuthController#logout}). The endpoint is
 * permitAll and deliberately idempotent: every shape of (or absence of)
 * {@code Authorization} header must answer 200 so a client can always clear
 * its session.
 */
@SpringBootTest(classes = {AppConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RestLogoutTest {

    private static final String LOGOUT_URL = "/api/restful/logout";
    private static final String SEED_USER = "test@wisemapping.org";
    private static final String SEED_PASSWORD = "password";

    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
    }

    @Test
    @DisplayName("Logout with a freshly issued bearer token returns 200")
    void logoutWithValidBearerTokenSucceeds() {
        final String token = authenticate();
        assertEquals(HttpStatus.OK, logout("Bearer " + token).getStatusCode(),
                "Logout with a valid bearer token must return 200");
    }

    @Test
    @DisplayName("Logout with a correctly signed but expired bearer token still returns 200")
    void logoutWithExpiredBearerTokenSucceeds() {
        assertEquals(HttpStatus.OK, logout("Bearer " + expiredTokenFor(SEED_USER)).getStatusCode(),
                "An expired token must not prevent logout");
    }

    @Test
    @DisplayName("Logout with a garbage bearer token still returns 200")
    void logoutWithGarbageBearerTokenSucceeds() {
        assertEquals(HttpStatus.OK, logout("Bearer not-a-jwt-at-all").getStatusCode(),
                "A malformed token must not prevent logout");
    }

    @Test
    @DisplayName("Logout with a valid token for an account that no longer exists returns 200")
    void logoutWithTokenForUnknownAccountSucceeds() {
        final String token = Jwts.builder()
                .subject("nobody-" + System.nanoTime() + "@example.org")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(signingKey())
                .compact();

        assertEquals(HttpStatus.OK, logout("Bearer " + token).getStatusCode(),
                "An unknown subject must not prevent logout");
    }

    @Test
    @DisplayName("Logout with no Authorization header at all returns 200")
    void logoutWithoutAuthorizationHeaderSucceeds() {
        assertEquals(HttpStatus.OK, logout(null).getStatusCode(),
                "Logout must be reachable without any credentials");
    }

    @Test
    @DisplayName("Logout with a valid HTTP Basic Authorization header returns 200 without a bearer token")
    void logoutWithNonBearerAuthorizationHeaderSucceeds() {
        // Not a Bearer scheme, so JwtAuthController skips token inspection
        // entirely. Note this needs *valid* Basic credentials: the Basic auth
        // filter runs before the permitAll rule and rejects bad credentials
        // with 401 before the controller is ever reached.
        final ResponseEntity<Void> response = restTemplate.withBasicAuth(SEED_USER, SEED_PASSWORD)
                .exchange(LOGOUT_URL, HttpMethod.POST, new HttpEntity<>(new HttpHeaders()), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "A non-Bearer scheme must be ignored rather than rejected");
    }

    @NotNull
    private ResponseEntity<Void> logout(@Nullable String authorizationHeader) {
        final HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return restTemplate.exchange(LOGOUT_URL, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }

    @NotNull
    private String expiredTokenFor(@NotNull String email) {
        final long oneHourAgo = System.currentTimeMillis() - 3_600_000L;
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(oneHourAgo))
                .expiration(new Date(oneHourAgo + 1_000L))
                .signWith(signingKey())
                .compact();
    }

    @NotNull
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @NotNull
    private String authenticate() {
        final HttpHeaders headers = createHeaders(MediaType.APPLICATION_JSON);
        final String payload = "{\"email\":\"" + SEED_USER + "\",\"password\":\"" + SEED_PASSWORD + "\"}";
        final ResponseEntity<String> response = restTemplate.exchange("/api/restful/authenticate",
                HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Authentication of the seed user should succeed");
        final String token = response.getBody();
        assertNotNull(token, "Authentication must return a token");
        return token;
    }
}
