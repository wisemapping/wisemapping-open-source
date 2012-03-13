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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


@Test
public class RestMindmapTCase {

    @NonNls
    private static final String HOST_PORT = "http://localhost:8080/";
    private static final String BASE_REST_URL = HOST_PORT + "service";

    private URI createUser(@NotNull HttpHeaders requestHeaders, @NotNull RestTemplate templateRest) {
        final RestUser restUser = new RestUser();
        final String email = "foo-to-delete" + System.nanoTime() + "@example.org";
        restUser.setEmail(email);
        restUser.setUsername("foo");
        restUser.setFirstname("foo first name");
        restUser.setLastname("foo last name");
        restUser.setPassword("foo password");

        HttpEntity<RestUser> createUserEntity = new HttpEntity<RestUser>(restUser, requestHeaders);
        return templateRest.postForLocation(BASE_REST_URL + "/admin/users", createUserEntity);
    }


    @Test(dataProvider = "ContentType-Provider-Function")
    public void createMap(final @NotNull MediaType mediaType) {    // Configure media types ...
        final HttpHeaders requestHeaders = createHeaders(mediaType);
        final RestTemplate templateRest = createTemplate();

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
