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

import com.wisemapping.config.AppConfig;
import com.wisemapping.rest.OAuth2Controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {AppConfig.class, OAuth2Controller.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.api.http-basic-enabled=true"})
public class RestOAuth2ControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void processGoogleCallbackEndpointExists() {
        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/oauth2/googlecallback?code=invalid_code", HttpMethod.POST, null, String.class);
        assertNotEquals(HttpStatus.NOT_FOUND, exchange.getStatusCode());
    }

    @Test
    public void confirmAccountSyncEndpointExists() {
        final ResponseEntity<String> exchange = restTemplate.exchange("/api/restful/oauth2/confirmaccountsync?email=test@example.com&code=test_code", HttpMethod.PUT, null, String.class);
        assertNotEquals(HttpStatus.NOT_FOUND, exchange.getStatusCode());
    }
}