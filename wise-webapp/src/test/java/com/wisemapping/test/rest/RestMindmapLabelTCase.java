package com.wisemapping.test.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.rest.model.RestLabelList;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.SkipException;
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
public class RestMindmapLabelTCase {

    private String userEmail = "admin@wisemapping.com";
    private static final String HOST_PORT = "http://localhost:8080";
    private static final String BASE_REST_URL = HOST_PORT + "/service";

    @BeforeClass
    void createUser() {

        final RestAdminITCase restAdminITCase = new RestAdminITCase();
        userEmail = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
    }

    @Test(dataProvider = "ContentType-Provider-Function")
    public void createLabel(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate template = createTemplate();

        // Create a new label
        final String title1 = "Label 1  - " + mediaType.toString();
        addNewLabel(requestHeaders,template,title1);

        // Create a new label
        final String title2 = "Label 2  - " + mediaType.toString();
        addNewLabel(requestHeaders,template,title2);

        // Check that the map has been created ...
        final HttpEntity findMapEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestLabelList> response = template.exchange(BASE_REST_URL + "/labels/", HttpMethod.GET, findMapEntity, RestLabelList.class);

        // Validate that the two maps are there ...
        final RestLabelList body = response.getBody();
        final List<RestLabel> labels = body.getLabels();

        boolean found1 = false;
        boolean found2 = false;
        for (RestLabel label : labels) {
            if (label.getTitle().equals(title1)) {
                found1 = true;
            }
            if (label.getTitle().equals(title2)) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2, "Labels could not be found");

    }

    private URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title, @Nullable String xml) throws IOException, WiseMappingException {
        final RestLabel restLabel = new RestLabel();
        restLabel.setTitle(title);
        restLabel.setColor("#666666");

        // Create a new label ...
        HttpEntity<RestLabel> createUserEntity = new HttpEntity<RestLabel>(restLabel, requestHeaders);
        return template.postForLocation(BASE_REST_URL + "/labels", createUserEntity);
    }

    private URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title) throws IOException, WiseMappingException {
        return addNewLabel(requestHeaders, template, title, null);
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
