package com.wisemapping.test.rest;

import com.wisemapping.rest.model.RestUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class RestHelper {
    public static final String BASE_REST_URL = "/api/restful";

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
        restUser.setPassword("fooPassword");
        return restUser;
    }
}
