package com.wisemapping.test.rest;


import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


@Test
public class RestMindmapTCase {

    private String userEmail = "admin@wisemapping.com";
    private static final String HOST_PORT = "http://localhost:8080";
    private static final String BASE_REST_URL = HOST_PORT + "/service";

    @BeforeClass
    void createUser() {

        final RestAdminITCase restAdminITCase = new RestAdminITCase();
        userEmail = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void createMap(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final RestMindmap restMindmap = new RestMindmap();
        final String title = "My Map " + mediaType.toString();
        restMindmap.setTitle(title);
        restMindmap.setDescription("My Map Desc");

        // Create a new map ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<RestMindmap>(restMindmap, requestHeaders);
        final URI resourceLocation = templateRest.postForLocation(BASE_REST_URL + "/maps", createUserEntity);

        // Check that the map has been created ...
        HttpEntity<RestUser> findUserEntity = new HttpEntity<RestUser>(requestHeaders);
        final ResponseEntity<RestMindmap> response = templateRest.exchange(HOST_PORT + resourceLocation.toString(), HttpMethod.GET, findUserEntity, RestMindmap.class);
        assertEquals(response.getBody().getTitle(), title);
    }


    @Test(dataProvider = "ContentType-Provider-Function")
    public void updateMapXml(final @NotNull MediaType mediaType) throws IOException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final RestMindmap restMindmap = new RestMindmap();
        final String title = "Update XML sample " + mediaType.toString();
        restMindmap.setTitle(title);
        restMindmap.setDescription("My Map Desc");

        // Create a new map ...
        HttpEntity<RestMindmap> createUserEntity = new HttpEntity<RestMindmap>(restMindmap, requestHeaders);
        final URI resourceLocation = templateRest.postForLocation(BASE_REST_URL + "/maps", createUserEntity);

        // Update map xml content ...
        final String resourceUrl = HOST_PORT + resourceLocation.toString();
        requestHeaders.setContentType(MediaType.APPLICATION_XML);
        final String newXmlContent = "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<String>(newXmlContent, requestHeaders);
        templateRest.put(resourceUrl + "/xml", updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findUserEntity = new HttpEntity<RestUser>(requestHeaders);
        final ResponseEntity<RestMindmap> response = templateRest.exchange(HOST_PORT + resourceLocation.toString(), HttpMethod.GET, findUserEntity, RestMindmap.class);
        assertEquals(response.getBody().getXml(), newXmlContent);
    }


    @Test(dataProvider = "ContentType-Provider-Function")
    public void updateMap(final @NotNull MediaType mediaType) throws IOException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final RestMindmap newRestMindmap = new RestMindmap();
        final String title = "Update sample " + mediaType.toString();
        newRestMindmap.setTitle(title);
        newRestMindmap.setDescription("My Map Desc");

        // Create a new map ...
        final HttpEntity<RestMindmap> createUserEntity = new HttpEntity<RestMindmap>(newRestMindmap, requestHeaders);
        final URI resourceLocation = templateRest.postForLocation(BASE_REST_URL + "/maps", createUserEntity);

        // Build map to update ...
        final RestMindmap mapToUpdate = new RestMindmap();
        mapToUpdate.setXml("<map>this is not valid</map>");
        mapToUpdate.setProperties("{zoom:x}");

        // Update map ...
        final String resourceUrl = HOST_PORT + resourceLocation.toString();
        requestHeaders.setContentType(MediaType.APPLICATION_XML);
        final HttpEntity<RestMindmap> updateEntity = new HttpEntity<RestMindmap>(mapToUpdate, requestHeaders);
        templateRest.put(resourceUrl, updateEntity);

        // Check that the map has been updated ...
        HttpEntity<RestUser> findUserEntity = new HttpEntity<RestUser>(requestHeaders);
        final ResponseEntity<RestMindmap> response = templateRest.exchange(HOST_PORT + resourceLocation.toString(), HttpMethod.GET, findUserEntity, RestMindmap.class);
        assertEquals(response.getBody().getXml(), mapToUpdate.getXml());
        assertEquals(response.getBody().getProperties(), mapToUpdate.getProperties());
    }

    private HttpHeaders createHeaders(@NotNull MediaType mediaType) {
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
                String authorization = userEmail + ":" + "admin";
                byte[] encodedAuthorisation = Base64.encode(authorization.getBytes());
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
