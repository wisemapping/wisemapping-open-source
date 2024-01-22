package com.wisemapping.test.rest;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class RestHelper {

    public static final String HOST_PORT = "http://localhost:8081";
    public static final String BASE_REST_URL = HOST_PORT + "/api/restfull";

    static HttpHeaders createHeaders(@NotNull MediaType mediaType) {
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(mediaType);

        final HttpHeaders result = new HttpHeaders();
        result.setAccept(acceptableMediaTypes);
        result.setContentType(mediaType);
        return result;
    }

}
