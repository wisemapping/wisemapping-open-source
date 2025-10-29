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
        
        // Retry user creation in case admin user is not ready yet (up to 10 seconds)
        final int maxCreationRetries = 10;
        final long creationRetryDelayMillis = 1000; // 1 second between retries
        ResponseEntity<String> response = null;
        
        System.out.println("[DEBUG] Starting user creation for: " + email);
        for (int attempt = 1; attempt <= maxCreationRetries; attempt++) {
            try {
                System.out.println("[DEBUG] User creation attempt " + attempt + "/" + maxCreationRetries + " for: " + email);
                // Use postForEntity to get full response details for better error handling
                response = adminTemplate.postForEntity(
                    BASE_REST_URL + "/admin/users", 
                    createUserEntity, 
                    String.class
                );
                
                System.out.println("[DEBUG] Response status: " + response.getStatusCode() + " for: " + email);
                
                // If successful, break out of retry loop
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("[DEBUG] User creation successful on attempt " + attempt + " for: " + email);
                    break;
                }
                
                // If unauthorized and not the last attempt, wait and retry
                if (response.getStatusCode().value() == 401 && attempt < maxCreationRetries) {
                    System.out.println("[DEBUG] Got 401, waiting " + creationRetryDelayMillis + "ms before retry " + (attempt + 1) + " for: " + email);
                    Thread.sleep(creationRetryDelayMillis);
                    continue;
                }
                
                // For other errors on last attempt, throw immediately
                if (!response.getStatusCode().is2xxSuccessful() && attempt >= maxCreationRetries) {
                    System.out.println("[DEBUG] Final attempt failed with status: " + response.getStatusCode() + " for: " + email);
                    throw new IllegalStateException(
                        "Failed to create test user. Status: " + response.getStatusCode() + 
                        ", Body: " + response.getBody() + 
                        ", Email: " + email +
                        ", Attempt: " + attempt + "/" + maxCreationRetries
                    );
                }
                
                // For other non-2xx errors (not 401), wait and retry
                if (!response.getStatusCode().is2xxSuccessful() && attempt < maxCreationRetries) {
                    System.out.println("[DEBUG] Got error " + response.getStatusCode() + ", waiting " + creationRetryDelayMillis + "ms before retry " + (attempt + 1) + " for: " + email);
                    Thread.sleep(creationRetryDelayMillis);
                }
            } catch (InterruptedException e) {
                System.out.println("[DEBUG] Thread interrupted during user creation retry for: " + email);
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread interrupted while creating test user", e);
            } catch (Exception e) {
                System.out.println("[DEBUG] Exception during user creation attempt " + attempt + ": " + e.getClass().getSimpleName() + " - " + e.getMessage() + " for: " + email);
                // Handle exceptions (network errors, etc.) - retry if not last attempt
                if (attempt >= maxCreationRetries) {
                    System.out.println("[DEBUG] Max retries reached, throwing exception for: " + email);
                    throw new IllegalStateException(
                        "Failed to create test user after " + maxCreationRetries + " attempts. " +
                        "Last error: " + e.getMessage() + ", Email: " + email, e
                    );
                }
                try {
                    System.out.println("[DEBUG] Waiting " + creationRetryDelayMillis + "ms before retry after exception for: " + email);
                    Thread.sleep(creationRetryDelayMillis);
                } catch (InterruptedException ie) {
                    System.out.println("[DEBUG] Thread interrupted during wait after exception for: " + email);
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrupted while waiting to retry", ie);
                }
            }
        }
        
        // Final check after retries
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                "Failed to create test user after " + maxCreationRetries + " attempts. " +
                (response != null ? "Status: " + response.getStatusCode() + ", Body: " + response.getBody() : "No response") +
                ", Email: " + email
            );
        }
        
        // Extract location from response header (required, no fallback)
        final String locationHeader = response.getHeaders().getFirst("Location");
        if (locationHeader == null || locationHeader.isEmpty()) {
            throw new IllegalStateException(
                "Failed to create test user - Location header is required but was not returned. " +
                "Status: " + response.getStatusCode() + 
                ", Response: " + response.getBody() + 
                ", Email: " + email
            );
        }
        
        URI location;
        try {
            // Handle both absolute and relative location headers
            if (locationHeader.startsWith("http")) {
                location = new URI(locationHeader);
            } else {
                // Relative URL - construct absolute URL using the restTemplate's root URI
                String rootUri = restTemplate.getRootUri();
                if (rootUri == null || rootUri.isEmpty()) {
                    throw new IllegalStateException(
                        "Cannot resolve relative Location header: " + locationHeader + 
                        ". TestRestTemplate root URI is not set. Email: " + email
                    );
                }
                // Ensure location starts with / for proper concatenation
                String absoluteLocation = locationHeader.startsWith("/") 
                    ? rootUri + locationHeader 
                    : rootUri + "/" + locationHeader;
                location = new URI(absoluteLocation);
            }
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(
                "Invalid location header format: " + locationHeader + " for email: " + email, e
            );
        }
        
        // Retry logic to confirm account creation (handles eventual consistency/timing issues)
        // Retry for up to 10 seconds (10 retries × 1 second = 10 seconds total)
        final int maxRetries = 10;
        final long retryDelayMillis = 1000; // 1 second between retries
        
        System.out.println("[DEBUG] Starting user confirmation retries for: " + email + " at location: " + location);
        RestUser createdUser = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("[DEBUG] User confirmation attempt " + attempt + "/" + maxRetries + " for: " + email);
                // Fetch the created user to get the ID and confirm it exists
                final ResponseEntity<RestUser> result = adminTemplate.exchange(
                    location.toString(), 
                    HttpMethod.GET, 
                    new HttpEntity<>(requestHeaders), 
                    RestUser.class
                );
                
                System.out.println("[DEBUG] Confirmation response status: " + result.getStatusCode() + " for: " + email);
                
                if (result.getStatusCode().is2xxSuccessful()) {
                    createdUser = result.getBody();
                    System.out.println("[DEBUG] User retrieved - ID: " + (createdUser != null ? createdUser.getId() : "null") + 
                                      ", Email: " + (createdUser != null && createdUser.getEmail() != null ? createdUser.getEmail() : "null") + 
                                      " for: " + email);
                    // Verify user is fully created: has email and valid ID (ID > 0 means it was assigned)
                    if (createdUser != null && createdUser.getEmail() != null && createdUser.getId() > 0) {
                        System.out.println("[DEBUG] User fully confirmed on attempt " + attempt + " for: " + email);
                        // Store password separately since it's not returned from server
                        createdUser.setPassword(password);
                        return createdUser;
                    } else {
                        System.out.println("[DEBUG] User data incomplete, will retry for: " + email);
                    }
                }
                
                // User not ready yet (might be 404, or missing data) - wait and retry
                if (attempt < maxRetries) {
                    System.out.println("[DEBUG] Waiting " + retryDelayMillis + "ms before confirmation retry " + (attempt + 1) + " for: " + email);
                    Thread.sleep(retryDelayMillis);
                }
            } catch (InterruptedException e) {
                System.out.println("[DEBUG] Thread interrupted during confirmation retry for: " + email);
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread interrupted while creating test user", e);
            } catch (Exception e) {
                System.out.println("[DEBUG] Exception during confirmation attempt " + attempt + ": " + e.getClass().getSimpleName() + 
                                  " - " + e.getMessage() + " for: " + email);
                // If it's the last attempt, throw the exception
                if (attempt >= maxRetries) {
                    System.out.println("[DEBUG] Max confirmation retries reached, throwing exception for: " + email);
                    throw new IllegalStateException(
                        "Failed to confirm user creation after " + maxRetries + " attempts for email: " + email + 
                        ". Last error: " + e.getMessage(), 
                        e
                    );
                }
                // Otherwise, wait and retry
                try {
                    System.out.println("[DEBUG] Waiting " + retryDelayMillis + "ms after confirmation exception before retry for: " + email);
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    System.out.println("[DEBUG] Thread interrupted during confirmation wait for: " + email);
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrupted while waiting to retry user creation", ie);
                }
            }
        }
        
        // If we exhausted retries without getting a valid user
        throw new IllegalStateException(
            "Failed to confirm user creation after " + maxRetries + " retries for email: " + email + 
            ". User may not have been fully created or is missing required fields (ID: " + 
            (createdUser != null ? String.valueOf(createdUser.getId()) : "null") + 
            ", Email: " + (createdUser != null && createdUser.getEmail() != null ? createdUser.getEmail() : "null") + ")"
        );
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
