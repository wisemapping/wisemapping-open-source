package com.wisemapping.test.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestLabelList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.fail;

@Test
public class RestLabelITCase {

    private String userEmail;
    private static final String COLOR = "#000000";

    @BeforeClass
    void createUser() {

        final RestAdminITCase restAdminITCase = new RestAdminITCase();
        userEmail = restAdminITCase.createNewUser(MediaType.APPLICATION_JSON);
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider="ContentType-Provider-Function")
    public void createLabel(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {    // Configure media types ...
        final HttpHeaders requestHeaders = RestHelper.createHeaders(mediaType);
        final RestTemplate template = RestHelper.createTemplate( userEmail + ":" + "admin");

        // Create a new label
        final String title1 = "Label 1  - " + mediaType.toString();
        addNewLabel(requestHeaders, template, title1, COLOR);

        // Create a new label
        final String title2 = "Label 2  - " + mediaType.toString();
        addNewLabel(requestHeaders, template, title2, COLOR);

        // Check that the label has been created ...
        final RestLabelList restLabelList = getLabels(requestHeaders, template);

        // Validate that the two labels are there ...
        final List<RestLabel> labels = restLabelList.getLabels();

        boolean found1 = false;
        boolean found2 = false;
        for (RestLabel label : labels) {
            if (title1.equals(label.getTitle())) {
                found1 = true;
            }
            if (title2.equals(label.getTitle())) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2, "Labels could not be found");

    }

    static RestLabelList getLabels(HttpHeaders requestHeaders, RestTemplate template) {
        final HttpEntity findLabelEntity = new HttpEntity(requestHeaders);
        final ResponseEntity<RestLabelList> response = template.exchange(BASE_REST_URL + "/labels", HttpMethod.GET, findLabelEntity, RestLabelList.class);
        return response.getBody();
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider="ContentType-Provider-Function")
    public void createLabelWithoutRequiredField(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {
        final HttpHeaders requestHeaders = RestHelper.createHeaders(mediaType);
        final RestTemplate template = RestHelper.createTemplate( userEmail + ":" + "admin");

        try {
            addNewLabel(requestHeaders, template, null, COLOR);
            fail("Wrong response");
        } catch (HttpClientErrorException e) {
            final String responseBodyAsString = e.getResponseBodyAsString();
            assertTrue (responseBodyAsString.contains("Required field cannot be left blank"));
        }

        try {
            addNewLabel(requestHeaders, template, "title12345", null);
            fail("Wrong response");
        } catch (HttpClientErrorException e) {
            final String responseBodyAsString = e.getResponseBodyAsString();
            assert (responseBodyAsString.contains("Required field cannot be left blank"));
        }
    }

    @Test(dataProviderClass = RestHelper.class, dataProvider="ContentType-Provider-Function")
    public void deleteLabel(final @NotNull MediaType mediaType) throws IOException, WiseMappingException {
        final HttpHeaders requestHeaders = RestHelper.createHeaders(mediaType);
        final RestTemplate template = RestHelper.createTemplate( userEmail + ":" + "admin");

        final String title = "title to delete";
        final URI resourceUri = addNewLabel(requestHeaders, template, title, COLOR);

        // Now remove it ...
        template.delete(RestHelper.HOST_PORT + resourceUri.toString());
        final RestLabelList restLabelList = getLabels(requestHeaders, template);

        for (RestLabel restLabel : restLabelList.getLabels()) {
            if (title.equals(restLabel.getTitle())) {
                Assert.fail("Label could not be removed:" + resourceUri);
            }
        }

    }

    static URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @Nullable String title, @Nullable String color ) throws IOException, WiseMappingException {
        final RestLabel restLabel = new RestLabel();
        if (title != null) {
            restLabel.setTitle(title);
        }
        if (color != null) {
            restLabel.setColor(color);
        }

        // Create a new label ...
        HttpEntity<RestLabel> createUserEntity = new HttpEntity<RestLabel>(restLabel, requestHeaders);
        return template.postForLocation(BASE_REST_URL + "/labels", createUserEntity);
    }
}