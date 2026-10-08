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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.config.AppConfig;
import com.wisemapping.config.GlobalExceptionHandler;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks down the HTTP error contract for the request-binding exceptions Spring
 * MVC raises on its own.
 * <p>
 * Before {@link GlobalExceptionHandler} grew explicit handlers for them, the
 * catch-all {@code @ExceptionHandler(Exception.class)} was the closest match for
 * every one of these, so each row below answered <b>500</b> instead of its
 * proper 4xx status (and polluted the ERROR log). Every assertion here checks
 * both the status code and that the body is still a {@code RestErrors}
 * document, because the frontend parses that exact shape -- which is why
 * {@code GlobalExceptionHandler} must never be converted to extend
 * {@code ResponseEntityExceptionHandler} (that would emit RFC-7807
 * {@code ProblemDetail} bodies instead).
 */
@SpringBootTest(
        classes = {AppConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("REST Error Contract Tests")
class RestErrorContractTest {

    private static final String ADMIN_USER = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "testAdmin123";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestTemplate() {
        this.restTemplate = new TestRestTemplate("http://localhost:" + port + "/");
    }

    @Test
    @DisplayName("Missing required request parameter should return 400, not 500")
    void missingRequestParameterShouldReturnBadRequest() {
        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPassword",
                HttpMethod.PUT,
                null,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);
        assertTrue(body.get("globalErrors").get(0).asText().contains("email"),
                "The missing parameter name should be reported: " + response.getBody());
    }

    @Test
    @DisplayName("Unparseable request parameter value should return 400, not 500")
    void parameterTypeMismatchShouldReturnBadRequest() {
        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/activation?code=notanumber",
                HttpMethod.PUT,
                null,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);
        assertTrue(body.get("globalErrors").get(0).asText().contains("code"),
                "The offending parameter name should be reported: " + response.getBody());
    }

    @Test
    @DisplayName("Wrong HTTP method should return 405, not 500")
    void unsupportedHttpMethodShouldReturnMethodNotAllowed() {
        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/app/config",
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);
        assertTrue(body.get("globalErrors").get(0).asText().contains("DELETE"),
                "The rejected HTTP method should be reported: " + response.getBody());
    }

    @Test
    @DisplayName("Bean validation failure on the request body should return 400 with field errors, not 500")
    void invalidRequestBodyShouldReturnBadRequestWithFieldErrors() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/users/resetPasswordToken",
                HttpMethod.POST,
                new HttpEntity<>("{\"token\":\"\",\"password\":\"x\"}", headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);

        final JsonNode fieldErrors = body.get("fieldErrors");
        assertNotNull(fieldErrors, "fieldErrors must be present: " + response.getBody());
        assertTrue(fieldErrors.isObject(), "fieldErrors must be a map: " + response.getBody());
        assertTrue(fieldErrors.has("token"),
                "The blank token must be reported as a field error: " + response.getBody());
        assertTrue(fieldErrors.has("password"),
                "The too-short password must be reported as a field error: " + response.getBody());
        assertTrue(fieldErrors.get("token").asText().length() > 0,
                "Field errors must carry a resolved message: " + response.getBody());
    }

    @Test
    @DisplayName("Unsupported request content type should return 415, not 500")
    void unsupportedContentTypeShouldReturnUnsupportedMediaType() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> response = adminTemplate().exchange(
                BASE_REST_URL + "/account/firstname",
                HttpMethod.PUT,
                new HttpEntity<>("some name", headers),
                String.class);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode(), response.toString());
        assertRestErrorsBody(response);
    }

    @Test
    @DisplayName("Unknown endpoint should return 404, not 500")
    void unknownEndpointShouldReturnNotFound() {
        final ResponseEntity<String> response = adminTemplate().exchange(
                BASE_REST_URL + "/doesnotexist",
                HttpMethod.GET,
                null,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), response.toString());
        assertRestErrorsBody(response);
    }

    @Test
    @DisplayName("Batch delete without the ids parameter should return 400, not 500")
    void missingBatchIdsParameterShouldReturnBadRequest() {
        final ResponseEntity<String> response = adminTemplate().exchange(
                BASE_REST_URL + "/maps/batch",
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);
        assertTrue(body.get("globalErrors").get(0).asText().contains("ids"),
                "The missing parameter name should be reported: " + response.getBody());
    }

    @Test
    @DisplayName("Unacceptable response format should return 406, not 500")
    void unacceptableResponseFormatShouldReturnNotAcceptable() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_PDF));

        final ResponseEntity<String> response = restTemplate.exchange(
                BASE_REST_URL + "/app/config",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class);

        // Body shape is not asserted here: the client declared it accepts only
        // application/pdf, so the RestErrors document itself cannot be
        // negotiated back. Only the status code is part of the contract.
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode(), response.toString());
    }

    /**
     * {@code PASSWORD_TOO_SHORT} was declared as the {@code MSG_KEY} of
     * {@link com.wisemapping.exceptions.PasswordTooShortException} but defined in
     * no bundle. Because {@code spring.messages.use-code-as-default-message} is
     * not set, resolving it threw {@code NoSuchMessageException} from inside
     * {@code handleClientErrors}, so the request fell through to the catch-all
     * {@code Exception} handler and answered 500 instead of 400. Same class of
     * defect as the missing handlers above, reached through a missing resource.
     */
    @Test
    @DisplayName("Too short password should return 400, not 500")
    void tooShortPasswordShouldReturnBadRequest() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        final ResponseEntity<String> response = adminTemplate().exchange(
                BASE_REST_URL + "/account/password",
                HttpMethod.PUT,
                new HttpEntity<>("short", headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), response.toString());
        final JsonNode body = assertRestErrorsBody(response);
        assertTrue(body.get("globalErrors").size() > 0,
                "The resolved PASSWORD_TOO_SHORT message must be reported: " + response.getBody());
        assertTrue(body.get("globalErrors").get(0).asText().contains("8"),
                "The minimum password length must be reported: " + response.getBody());
    }

    /**
     * Guards the two handlers that cannot be driven over HTTP from this test:
     * {@link HandlerMethodValidationException} (no controller currently declares
     * constraint annotations straight on a handler parameter) and
     * {@link NoHandlerFoundException} (the static resource handler mapped at
     * {@code /**} raises {@link NoResourceFoundException} first). Without this
     * assertion they could be deleted or re-statused unnoticed.
     */
    @Test
    @DisplayName("Every Spring MVC binding exception should be mapped to its own 4xx status")
    void everyBindingExceptionShouldDeclareItsStatus() {
        final Map<Class<? extends Throwable>, HttpStatus> expected = new LinkedHashMap<>();
        expected.put(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST);
        expected.put(HandlerMethodValidationException.class, HttpStatus.BAD_REQUEST);
        expected.put(MissingServletRequestParameterException.class, HttpStatus.BAD_REQUEST);
        expected.put(MethodArgumentTypeMismatchException.class, HttpStatus.BAD_REQUEST);
        expected.put(HttpRequestMethodNotSupportedException.class, HttpStatus.METHOD_NOT_ALLOWED);
        expected.put(HttpMediaTypeNotSupportedException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        expected.put(HttpMediaTypeNotAcceptableException.class, HttpStatus.NOT_ACCEPTABLE);
        expected.put(NoHandlerFoundException.class, HttpStatus.NOT_FOUND);
        expected.put(NoResourceFoundException.class, HttpStatus.NOT_FOUND);

        final Map<Class<?>, HttpStatus> declared = new LinkedHashMap<>();
        for (Method method : GlobalExceptionHandler.class.getDeclaredMethods()) {
            final ExceptionHandler handler = method.getAnnotation(ExceptionHandler.class);
            final ResponseStatus status = method.getAnnotation(ResponseStatus.class);
            if (handler == null || status == null) {
                continue;
            }
            for (Class<? extends Throwable> exceptionType : handler.value()) {
                declared.put(exceptionType, status.value());
            }
        }

        for (Map.Entry<Class<? extends Throwable>, HttpStatus> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), declared.get(entry.getKey()),
                    "GlobalExceptionHandler must map " + entry.getKey().getSimpleName()
                            + " to " + entry.getValue());
        }
    }

    @NotNull
    private TestRestTemplate adminTemplate() {
        return this.restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD);
    }

    /**
     * Asserts the payload is still a {@code RestErrors} document rather than an
     * RFC-7807 {@code ProblemDetail}.
     */
    @NotNull
    private JsonNode assertRestErrorsBody(@NotNull final ResponseEntity<String> response) {
        final String payload = response.getBody();
        assertNotNull(payload, "An error response must carry a RestErrors body");

        final JsonNode body;
        try {
            body = OBJECT_MAPPER.readTree(payload);
        } catch (Exception e) {
            throw new AssertionError("Error response is not valid JSON: " + payload, e);
        }

        assertTrue(body.isObject(), "RestErrors must serialize as an object: " + payload);
        // RestErrors exposes exactly two properties on the wire: globalErrors and
        // fieldErrors. globalSeverity is deliberately not asserted -- the @JsonIgnore
        // on the backing field suppresses it for every RestErrors response, new and old.
        assertTrue(body.has("globalErrors"), "RestErrors.globalErrors is missing: " + payload);
        assertTrue(body.get("globalErrors").isArray(), "RestErrors.globalErrors must be an array: " + payload);
        assertTrue(body.has("fieldErrors"), "RestErrors.fieldErrors is missing: " + payload);
        // ProblemDetail (RFC-7807) would have surfaced these instead.
        assertTrue(!body.has("type") && !body.has("detail"),
                "Error body must not be an RFC-7807 ProblemDetail: " + payload);
        return body;
    }
}
