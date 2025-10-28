/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may obtain a copy of the license at
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
import com.wisemapping.rest.AdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {AppConfig.class, AdminController.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class AdminSystemControllerTest {
    private static final String ADMIN_USER = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "testAdmin123";
    private static final String REGULAR_USER = "test@wisemapping.org";
    private static final String REGULAR_PASSWORD = "password";

    @Autowired
    private TestRestTemplate restTemplate;

    // ===== System Information Endpoint Tests =====

    @Test
    void testGetSystemInfo_AdminAccess() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify the response contains expected system information fields
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("application"), "Response should contain application info");
        assertTrue(responseBody.contains("database"), "Response should contain database info");
        assertTrue(responseBody.contains("jvm"), "Response should contain JVM info");
        assertTrue(responseBody.contains("statistics"), "Response should contain statistics");
    }

    @Test
    void testGetSystemInfo_RegularUserAccessDenied() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);

        // Should be either 401 (unauthorized) or 403 (forbidden)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                  response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetSystemInfo_UnauthenticatedAccessDenied() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/restful/admin/system/info", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetSystemHealth_AdminAccess() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify the response contains expected health information fields
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("database"), "Response should contain database health");
        assertTrue(responseBody.contains("memory"), "Response should contain memory health");
    }

    @Test
    void testGetSystemHealth_RegularUserAccessDenied() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);

        // Should be either 401 (unauthorized) or 403 (forbidden)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                  response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetSystemHealth_UnauthenticatedAccessDenied() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/restful/admin/system/health", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ===== System Information Content Validation Tests =====

    @Test
    void testGetSystemInfo_ContentValidation() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        
        // Validate application section
        assertTrue(responseBody.contains("\"application\""), "Should contain application section");
        assertTrue(responseBody.contains("\"name\""), "Should contain application name");
        assertTrue(responseBody.contains("\"port\""), "Should contain application port");
        
        // Validate database section
        assertTrue(responseBody.contains("\"database\""), "Should contain database section");
        assertTrue(responseBody.contains("\"driver\""), "Should contain database driver");
        assertTrue(responseBody.contains("\"url\""), "Should contain database URL");
        assertTrue(responseBody.contains("\"username\""), "Should contain database username");
        assertTrue(responseBody.contains("\"hibernateDdlAuto\""), "Should contain Hibernate DDL setting");
        
        // Validate JVM section
        assertTrue(responseBody.contains("\"jvm\""), "Should contain JVM section");
        assertTrue(responseBody.contains("\"javaVersion\""), "Should contain Java version");
        assertTrue(responseBody.contains("\"javaVendor\""), "Should contain Java vendor");
        assertTrue(responseBody.contains("\"uptime\""), "Should contain uptime");
        assertTrue(responseBody.contains("\"startTime\""), "Should contain start time");
        assertTrue(responseBody.contains("\"maxMemory\""), "Should contain max memory");
        assertTrue(responseBody.contains("\"usedMemory\""), "Should contain used memory");
        assertTrue(responseBody.contains("\"totalMemory\""), "Should contain total memory");
        assertTrue(responseBody.contains("\"availableProcessors\""), "Should contain available processors");
        assertTrue(responseBody.contains("\"systemLoadAverage\""), "Should contain system load average");
        
        // Validate statistics section
        assertTrue(responseBody.contains("\"statistics\""), "Should contain statistics section");
    }

    @Test
    void testGetSystemHealth_ContentValidation() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        
        // Validate health response structure
        assertTrue(responseBody.contains("\"database\""), "Should contain database health status");
        assertTrue(responseBody.contains("\"memory\""), "Should contain memory health status");
        
        // Validate that database status is either "UP" or "DOWN"
        assertTrue(responseBody.contains("\"UP\"") || responseBody.contains("\"DOWN\""), 
                   "Database status should be UP or DOWN");
        
        // Validate that memory status is either "UP" or "WARNING"
        assertTrue(responseBody.contains("\"UP\"") || responseBody.contains("\"WARNING\""), 
                   "Memory status should be UP or WARNING");
    }

    @Test
    void testGetSystemInfo_ResponseFormat() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify content type is JSON
        assertTrue(response.getHeaders().getContentType().toString().contains("application/json"),
                   "Response should be JSON format");
        
        // Verify response is valid JSON (basic check)
        String responseBody = response.getBody();
        assertTrue(responseBody.startsWith("{"), "Response should start with '{'");
        assertTrue(responseBody.endsWith("}"), "Response should end with '}'");
    }

    @Test
    void testGetSystemHealth_ResponseFormat() {
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify content type is JSON
        assertTrue(response.getHeaders().getContentType().toString().contains("application/json"),
                   "Response should be JSON format");
        
        // Verify response is valid JSON (basic check)
        String responseBody = response.getBody();
        assertTrue(responseBody.startsWith("{"), "Response should start with '{'");
        assertTrue(responseBody.endsWith("}"), "Response should end with '}'");
    }

    // ===== Security and Authorization Tests =====

    @Test
    void testSystemEndpoints_RequireAuthentication() {
        // Test that both system endpoints require authentication
        ResponseEntity<String> infoResponse = restTemplate.getForEntity("/api/restful/admin/system/info", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, infoResponse.getStatusCode());

        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/api/restful/admin/system/health", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, healthResponse.getStatusCode());
    }

    @Test
    void testSystemEndpoints_RequireAdminRole() {
        // Test that both system endpoints require admin role
        ResponseEntity<String> infoResponse = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);
        // Should be either 401 (unauthorized) or 403 (forbidden)
        assertTrue(infoResponse.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                  infoResponse.getStatusCode() == HttpStatus.FORBIDDEN);

        ResponseEntity<String> healthResponse = restTemplate.withBasicAuth(REGULAR_USER, REGULAR_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);
        // Should be either 401 (unauthorized) or 403 (forbidden)
        assertTrue(healthResponse.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                  healthResponse.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void testSystemEndpoints_AdminCanAccess() {
        // Test that admin can access both system endpoints
        ResponseEntity<String> infoResponse = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/info", String.class);
        assertEquals(HttpStatus.OK, infoResponse.getStatusCode());

        ResponseEntity<String> healthResponse = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD)
                .getForEntity("/api/restful/admin/system/health", String.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
    }
}
