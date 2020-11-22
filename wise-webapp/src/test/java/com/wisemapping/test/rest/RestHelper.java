package com.wisemapping.test.rest;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class RestHelper {

    public static final String HOST_PORT = "http://localhost:8080";
    public static final String BASE_REST_URL = HOST_PORT + "/service";
    public static final String ADMIN_CREDENTIALS = "admin@wisemapping.org" + ":" + "test";
    public static final String COLOR = "#000000";

    static HttpHeaders createHeaders(@NotNull MediaType mediaType) {
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        acceptableMediaTypes.add(mediaType);

        final HttpHeaders result = new HttpHeaders();
        result.setAccept(acceptableMediaTypes);
        result.setContentType(mediaType);
        return result;
    }

    static RestTemplate createTemplate(@NotNull final String authorisation) {
        SimpleClientHttpRequestFactory s = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);

                byte[] encodedAuthorisation = Base64.encode(authorisation.getBytes());
                connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuthorisation));
            }

        };
        return new RestTemplate(s);
    }

    @DataProvider(name = "ContentType-Provider-Function")
    static Object[][] contentTypes() {
        return new Object[][]{{MediaType.APPLICATION_XML}, {MediaType.APPLICATION_JSON}};
    }

}
