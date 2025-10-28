package com.wisemapping.test.rest;

import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
     * Uses the admin credentials to create the user.
     * 
     * @param restTemplate the TestRestTemplate to use for API calls
     * @param email the email for the new user (should be unique)
     * @param firstname the first name
     * @param lastname the last name
     * @param password the password
     * @return the created RestUser with ID populated, or null if creation failed
     */
    public static RestUser createUserViaApi(@NotNull org.springframework.boot.test.web.client.TestRestTemplate restTemplate, 
                                            @NotNull String email, 
                                            @NotNull String firstname, 
                                            @NotNull String lastname, 
                                            @NotNull String password) {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final org.springframework.boot.test.web.client.TestRestTemplate adminTemplate = 
            restTemplate.withBasicAuth(ADMIN_USER, ADMIN_PASSWORD);
        
        final RestUser newUser = new RestUser();
        newUser.setEmail(email);
        newUser.setFirstname(firstname);
        newUser.setLastname(lastname);
        newUser.setPassword(password);
        
        final HttpEntity<RestUser> createUserEntity = new HttpEntity<>(newUser, requestHeaders);
        
        try {
            final org.springframework.http.ResponseEntity<String> response = 
                adminTemplate.postForEntity(BASE_REST_URL + "/admin/users", createUserEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Get the Location header - format is: /api/restful/admin/users/{id}
                final String locationHeader = response.getHeaders().getFirst("Location");
                if (locationHeader != null) {
                    // Extract user ID from location header
                    final String userIdStr = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                    try {
                        final int userId = Integer.parseInt(userIdStr);
                        
                        // Fetch the created user by ID - returns AdminRestUser, convert to RestUser
                        final org.springframework.http.ResponseEntity<com.wisemapping.rest.model.AdminRestUser> result = 
                            adminTemplate.exchange(BASE_REST_URL + "/admin/users/" + userId, 
                                                 org.springframework.http.HttpMethod.GET, 
                                                 new HttpEntity<>(requestHeaders), 
                                                 com.wisemapping.rest.model.AdminRestUser.class);
                        final com.wisemapping.rest.model.AdminRestUser adminUser = result.getBody();
                        if (adminUser != null) {
                            // Fetch the underlying Account and create RestUser from it
                            // We need to get the Account by ID to create a RestUser
                            final com.wisemapping.model.Account account = 
                                new com.wisemapping.model.Account();
                            account.setId(adminUser.getId());
                            account.setEmail(adminUser.getEmail());
                            account.setFirstname(adminUser.getFirstname());
                            account.setLastname(adminUser.getLastname());
                            account.setLocale(adminUser.getLocale());
                            if (adminUser.isActive()) {
                                account.setActivationDate(java.util.Calendar.getInstance());
                            }
                            account.setSuspended(adminUser.isSuspended());
                            account.setAllowSendEmail(adminUser.isAllowSendEmail());
                            
                            final RestUser createdUser = new RestUser(account);
                            createdUser.setPassword(password);
                            return createdUser;
                        }
                    } catch (NumberFormatException e) {
                        // Invalid user ID format
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            // Log exception for debugging but return null
            e.printStackTrace();
        }
        return null;
    }
}
