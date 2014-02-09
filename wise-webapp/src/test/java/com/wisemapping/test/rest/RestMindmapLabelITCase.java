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
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static org.testng.Assert.assertTrue;

@Test
public class RestMindmapLabelITCase {

    private String userEmail;

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
        addNewLabel(requestHeaders, template, title1);

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

    static URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title, @Nullable String xml) throws IOException, WiseMappingException {
        final RestLabel restLabel = new RestLabel();
        restLabel.setTitle(title);
        restLabel.setColor("#666666");

        // Create a new label ...
        HttpEntity<RestLabel> createUserEntity = new HttpEntity<RestLabel>(restLabel, requestHeaders);
        return template.postForLocation(BASE_REST_URL + "/labels", createUserEntity);
    }

    static URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate template, @NotNull String title) throws IOException, WiseMappingException {
        return addNewLabel(requestHeaders, template, title, null);
    }
}