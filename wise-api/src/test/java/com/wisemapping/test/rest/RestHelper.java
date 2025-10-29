package com.wisemapping.test.rest;

import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RestHelper {
    public static final String BASE_REST_URL = "/api/restful";
    private static final String ADMIN_USER = "admin@wisemapping.org";
    private static final String ADMIN_PASSWORD = "testAdmin123";

    static HttpHeaders createHeaders(@NotNull MediaType mediaType) {
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(mediaType);

        final HttpHeaders result = new HttpHeaders();
        result.setAccept(acceptableMediaTypes);
        result.setContentType(mediaType);
        return result;
    }

    static RestUser createDummyUser() {
        final RestUser restUser = new RestUser();
        final String username = "foo-to-delete" + System.nanoTime();
        final String email = username + "@example.org";
        restUser.setEmail(email);
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("fooPassword123");
        return restUser;
    }

    /**
     * Creates a user via the admin REST API and returns the created user with its ID populated.
     * Uses the admin credentials to create the user. Includes retry logic to confirm account creation.
     * 
     * @param restTemplate the TestRestTemplate to use for API calls
     * @param email the email for the new user (should be unique)
     * @param firstname the first name
     * @param lastname the last name
     * @param password the password
     * @return the created RestUser with ID populated
     * @throws IllegalStateException if user creation fails
     */
    public static RestUser createUserViaApi(@NotNull TestRestTemplate restTemplate,
                                            @NotNull String email,
                                            @NotNull String firstname,
                                            @NotNull String lastname,
                                            @NotNull String password) {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate adminTemplate = restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD);
        
        final RestUser newUser = new RestUser();
        newUser.setEmail(email);
        newUser.setFirstname(firstname);
        newUser.setLastname(lastname);
        newUser.setPassword(password);
        
        final HttpEntity<RestUser> createUserEntity = new HttpEntity<>(newUser, requestHeaders);
        final URI location = adminTemplate.postForLocation(BASE_REST_URL + "/admin/users", createUserEntity);
        
        if (location == null) {
            throw new IllegalStateException("Failed to create test user - no location header returned for email: " + email);
        }
        
        // Retry logic to confirm account creation (handles eventual consistency/timing issues)
        final int maxRetries = 5;
        final long retryDelayMillis = 100; // 100ms between retries
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Fetch the created user to get the ID
                final ResponseEntity<RestUser> result = adminTemplate.exchange(
                    location.toString(), 
                    HttpMethod.GET, 
                    new HttpEntity<>(requestHeaders), 
                    RestUser.class
                );
                
                if (result.getStatusCode().is2xxSuccessful()) {
                    final RestUser createdUser = result.getBody();
                    if (createdUser != null && createdUser.getEmail() != null) {
                        // Store password separately since it's not returned from server
                        createdUser.setPassword(password);
                        return createdUser;
                    }
                }
                
                // If not successful and not the last attempt, wait and retry
                if (attempt < maxRetries) {
                    Thread.sleep(retryDelayMillis);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread interrupted while creating test user", e);
            } catch (Exception e) {
                // If it's the last attempt, throw the exception
                if (attempt == maxRetries) {
                    throw new IllegalStateException(
                        "Failed to confirm user creation after " + maxRetries + " attempts for email: " + email, 
                        e
                    );
                }
                // Otherwise, wait and retry
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrupted while waiting to retry user creation", ie);
                }
            }
        }
        
        throw new IllegalStateException("Failed to create test user with email: " + email + " after " + maxRetries + " retries");
    }
    
    /**
     * Creates a test user with a unique email address.
     * Convenience method that generates a unique email automatically.
     * 
     * @param restTemplate the TestRestTemplate to use for API calls
     * @param password the password for the user (defaults to "testPassword123" if null)
     * @return the created RestUser with ID populated
     * @throws IllegalStateException if user creation fails
     */
    public static RestUser createTestUser(@NotNull TestRestTemplate restTemplate, String password) {
        final String email = "test-" + System.nanoTime() + "@example.org";
        final String userPassword = password != null ? password : "testPassword123";
        return createUserViaApi(restTemplate, email, "Test", "User", userPassword);
    }
    
    /**
     * Creates a test user with a unique email address and default password.
     * 
     * @param restTemplate the TestRestTemplate to use for API calls
     * @return the created RestUser with ID populated
     * @throws IllegalStateException if user creation fails
     */
    public static RestUser createTestUser(@NotNull TestRestTemplate restTemplate) {
        return createTestUser(restTemplate, null);
    }
}
