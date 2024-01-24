package com.wisemapping.test.rest;


import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.rest.AdminController;
import com.wisemapping.rest.LabelController;
import com.wisemapping.rest.UserController;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestLabelList;
import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.wisemapping.test.rest.RestHelper.BASE_REST_URL;
import static com.wisemapping.test.rest.RestHelper.createHeaders;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = {RestAppConfig.class, CommonConfig.class, LabelController.class, AdminController.class, UserController.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestLabelControllerTest {
    private static final String COLOR = "#000000";

    private RestUser user;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void createUser() {

        // Remote debug ...
        if (restTemplate == null) {
            this.restTemplate = new TestRestTemplate();
            this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081/"));
        }
        RestAccountControllerTest restAccount = RestAccountControllerTest.create(restTemplate);
        this.user = restAccount.createNewUser();
    }

    static RestLabelList getLabels(HttpHeaders requestHeaders, @NotNull TestRestTemplate template) {
        final HttpEntity<RestLabelList> findLabelEntity = new HttpEntity<>(requestHeaders);
        final ResponseEntity<RestLabelList> response = template.exchange(BASE_REST_URL + "/labels/", HttpMethod.GET, findLabelEntity, RestLabelList.class);
        return response.getBody();
    }

    @Test
    public void createLabel() {

        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        // Create a new label
        final String title1 = "Label 1  - ";

        addNewLabel(requestHeaders, restTemplate, title1, COLOR);

        // Create a new label
        final String title2 = "Label 2  - ";

        addNewLabel(requestHeaders, restTemplate, title2, COLOR);

        // Check that the label has been created ...
        final RestLabelList restLabelList = getLabels(requestHeaders, restTemplate);

        // Validate that the two labels are there ...
        final List<RestLabel> labels = restLabelList.getLabels();

        long count = labels.stream().filter(l -> Objects.equals(l.getTitle(), title1) || Objects.equals(l.getTitle(), title2)).count();
        assertEquals(2, count, "Labels could not be found");

    }

    @Test
    public void createLabelWithoutRequiredField() {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        requestHeaders.set(HttpHeaders.ACCEPT_LANGUAGE, "en");

        try {
            addNewLabel(requestHeaders, restTemplate, null, COLOR);
            fail("Wrong response");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Required field cannot be left blank"), e.getMessage());
        }

        try {
            addNewLabel(requestHeaders, restTemplate, "title12345", null);
            fail("Wrong response");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Required field cannot be left blank"), e.getMessage());
        }
    }

    @Test
    public void deleteLabel() {
        final HttpHeaders requestHeaders = createHeaders(MediaType.APPLICATION_JSON);
        final TestRestTemplate restTemplate = this.restTemplate.withBasicAuth(user.getEmail(), user.getPassword());

        final String title = "title to delete";
        final URI resourceUri = addNewLabel(requestHeaders, restTemplate, title, COLOR);

        // Now remove it ...
        restTemplate.delete(resourceUri.toString());
        final RestLabelList restLabelList = getLabels(requestHeaders, restTemplate);

        for (RestLabel restLabel : restLabelList.getLabels()) {
            if (title.equals(restLabel.getTitle())) {
                fail("Label could not be removed:" + resourceUri);
            }
        }
    }

    static URI addNewLabel(@NotNull HttpHeaders requestHeaders, @NotNull TestRestTemplate template, @Nullable String title, @Nullable String color) {
        final RestLabel restLabel = new RestLabel();
        if (title != null) {
            restLabel.setTitle(title);
        }
        if (color != null) {
            restLabel.setColor(color);
        }

        // Create a new label ...
        final HttpEntity<RestLabel> createUserEntity = new HttpEntity<>(restLabel, requestHeaders);
        final ResponseEntity<String> result = template.exchange("/api/restfull/labels", HttpMethod.POST, createUserEntity, String.class);
        if (!result.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(result.toString());
        }
        ;
        return result.getHeaders().getLocation();
    }
}