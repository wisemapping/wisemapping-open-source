package com.wisemapping.test.rest;


import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Test
public class RestAdminITCase {

    @NonNls
    private static final String HOST_PORT = "http://localhost:8080/";
    private static final String BASE_REST_URL = HOST_PORT + "service";

    @Test(dataProvider = "ContentType-Provider-Function")
    public void changePassword(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Fill user data ...
        final RestUser restUser = new RestUser();
        final String email = "foo-to-change" + System.nanoTime() + "@example.org";
        restUser.setEmail(email);
        restUser.setUsername("foo");
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("foo password");

        // User has been created ...
        final URI location = createUser(requestHeaders, templateRest, restUser);

        // Check that the user has been created ...
        ResponseEntity<RestUser> result = findUser(requestHeaders, templateRest, location);

        // Change password ...
        requestHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> createUserEntity = new HttpEntity<String>("some-new-password", requestHeaders);
        System.out.println("Changed password to:" + email);
        templateRest.put(BASE_REST_URL + "/admin/users/{id}/password", createUserEntity, result.getBody().getId());
    }


    @Test(dataProvider = "ContentType-Provider-Function")
    public void deleteUser(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Fill user data ...
        final RestUser restUser = new RestUser();
        final String email = "foo-to-delete" + System.nanoTime() + "@example.org";
        restUser.setEmail(email);
        restUser.setUsername("foo");
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("foo password");

        // User has been created ...
        final URI location = createUser(requestHeaders, templateRest, restUser);

        // Check that the user has been created ...
        ResponseEntity<RestUser> result = findUser(requestHeaders, templateRest, location);

        // Delete user ...
        templateRest.delete(BASE_REST_URL + "/admin/users/{id}", result.getBody().getId());

        // Is the user there ?
        // Check that the user has been created ...
        try {
            findUser(requestHeaders, templateRest, location);
            fail("User could not be deleted !");
        } catch (Exception e) {
        }
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void createNewUser(final @NotNull MediaType mediaType) {

        // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Fill user data ...
        final RestUser restUser = new RestUser();

        final String email = "foo" + System.nanoTime() + "@example.org";
        restUser.setEmail(email);
        restUser.setUsername("foo");
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("foo password");

        // Create a new user ...
        final URI location = createUser(requestHeaders, templateRest, restUser);

        // Check that the user has been created ...
        ResponseEntity<RestUser> result = findUser(requestHeaders, templateRest, location);
        assertEquals(result.getBody(), restUser, "Returned object object seems not be the same.");


        // Find by email and check ...
        result = findUserByEmail(requestHeaders, templateRest, email);
        assertEquals(result.getBody(), restUser, "Returned object object seems not be the same.");

    }

    private ResponseEntity<RestUser> findUser(HttpHeaders requestHeaders, RestTemplate templateRest, URI location) {
        HttpEntity<RestUser> findUserEntity = new HttpEntity<RestUser>(requestHeaders);
        final String url = "http://localhost:8080" + location;
        return templateRest.exchange(url, HttpMethod.GET, findUserEntity, RestUser.class);
    }

    private ResponseEntity<RestUser> findUserByEmail(HttpHeaders requestHeaders, RestTemplate templateRest, final String email) {
        HttpEntity<RestUser> findUserEntity = new HttpEntity<RestUser>(requestHeaders);

        // Add extension only to avoid the fact that the last part is extracted ...
        final String url = "http://localhost:8080/service/admin/users/email/{email}.json";
        return templateRest.exchange(url, HttpMethod.GET, findUserEntity, RestUser.class, email);
    }

    private URI createUser(HttpHeaders requestHeaders, RestTemplate templateRest, RestUser restUser) {
        HttpEntity<RestUser> createUserEntity = new HttpEntity<RestUser>(restUser, requestHeaders);
        return templateRest.postForLocation(BASE_REST_URL + "/admin/users", createUserEntity);
    }

    private HttpHeaders createHeaders(MediaType mediaType) {
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(mediaType);
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(acceptableMediaTypes);
        requestHeaders.setContentType(mediaType);
        return requestHeaders;
    }

    private RestTemplate createTemplate() {
        SimpleClientHttpRequestFactory s = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);

                //Basic Authentication for Police API
                String authorisation = "admin@wisemapping.org" + ":" + "admin";
                byte[] encodedAuthorisation = Base64.encode(authorisation.getBytes());
                connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuthorisation));
            }

        };
        return new RestTemplate(s);
    }

    @DataProvider(name = "ContentType-Provider-Function")
    public Object[][] contentTypes() {
        return new Object[][]{{MediaType.APPLICATION_XML}, {MediaType.APPLICATION_JSON}};
    }
}
