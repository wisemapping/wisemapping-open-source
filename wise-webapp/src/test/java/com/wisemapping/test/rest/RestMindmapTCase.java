package com.wisemapping.test.rest;


import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
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
    public void listMaps(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final String title1 = "List Maps 1  - " + mediaType.toString();
        addNewMap(requestHeaders, templateRest, title1);

        final String title2 = "List Maps 2 - " + mediaType.toString();
        addNewMap(requestHeaders, templateRest, title2);

        // Check that the map has been created ...
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestMindmapList> response = templateRest.exchange(BASE_REST_URL + "/maps", HttpMethod.GET, findMapEntity, RestMindmapList.class);

        // Validate that the two maps are there ...
        final RestMindmapList body = response.getBody();
        final List<RestMindmapInfo> mindmaps = body.getMindmapsInfo();

        boolean found1 = false;
        boolean found2 = false;
        for (RestMindmapInfo mindmap : mindmaps) {
            if (mindmap.getTitle().equals(title1)) {
                found1 = true;
            }
            if (mindmap.getTitle().equals(title2)) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2, "Map could not be found");

    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void deleteMap(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final String title1 = "Map to delete  - " + mediaType.toString();
        final URI resourceLocation = addNewMap(requestHeaders, templateRest, title1);

        // Now remove it ...
        templateRest.delete(HOST_PORT + resourceLocation.toString());

        // Check that has been removed ...
        try {
            findMap(requestHeaders, templateRest, resourceLocation);
            fail("Map could not be removed:" + resourceLocation);
        } catch (Exception e) {
        }
    }

    private URI addNewMap(HttpHeaders requestHeaders, RestTemplate templateRest, String title) {
        final RestMindmapInfo restMindmap = new RestMindmapInfo();
        restMindmap.setTitle(title);
        restMindmap.setDescription("My Map Desc");

        // Create a new map ...
        HttpEntity<RestMindmapInfo> createUserEntity = new HttpEntity<RestMindmapInfo>(restMindmap, requestHeaders);
        return templateRest.postForLocation(BASE_REST_URL + "/maps", createUserEntity);
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void discardChange(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final String title = "Add map to discard " + mediaType.toString();
        final URI resourceLocation = addNewMap(requestHeaders, templateRest, title);

        // Update with "minor" flag ...

        // Revert the change ...

        // Check that the map is the
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void updateMapXml(final @NotNull MediaType mediaType) throws IOException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final String title = "Update XML sample " + mediaType.toString();
        final URI resourceLocation = addNewMap(requestHeaders, templateRest, title);

        // Update map xml content ...
        final String resourceUrl = HOST_PORT + resourceLocation.toString();
        requestHeaders.setContentType(MediaType.APPLICATION_XML);
        final String newXmlContent = "<map>this is not valid</map>";
        HttpEntity<String> updateEntity = new HttpEntity<String>(newXmlContent, requestHeaders);
        templateRest.put(resourceUrl + "/xml", updateEntity);

        // Check that the map has been updated ...
        final RestMindmap response = findMap(requestHeaders, templateRest, resourceLocation);
        assertEquals(response.getXml(), newXmlContent);
    }

    private RestMindmap findMap(HttpHeaders requestHeaders, RestTemplate templateRest, URI resourceLocation) {
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestMindmap> response = templateRest.exchange(HOST_PORT + resourceLocation.toString(), HttpMethod.GET, findMapEntity, RestMindmap.class);
        return response.getBody();
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void updateMap(final @NotNull MediaType mediaType) throws IOException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

        // Create a sample map ...
        final String title = "Update sample " + mediaType.toString();
        final URI resourceLocation = addNewMap(requestHeaders, templateRest, title);

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
