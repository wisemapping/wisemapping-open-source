package org.springframework.boot.test.web.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.io.IOException;

public class TestRestTemplate {
    private final RestTemplate restTemplate;
    private @Nullable String rootUri;

    public TestRestTemplate() {
        this.restTemplate = createRestTemplate();
    }

    public TestRestTemplate(String rootUri) {
        this();
        setUriTemplateHandler(new DefaultUriBuilderFactory(rootUri));
    }

    private TestRestTemplate(RestTemplate restTemplate, @Nullable String rootUri) {
        this.restTemplate = restTemplate;
        this.rootUri = rootUri;
    }

    public void setUriTemplateHandler(UriTemplateHandler handler) {
        this.restTemplate.setUriTemplateHandler(handler);
        this.rootUri = normalizeRootUri(handler.expand("").toString());
    }

    public String getRootUri() {
        return this.rootUri == null ? "" : this.rootUri;
    }

    public TestRestTemplate withBasicAuth(String username, String password) {
        RestTemplate authenticated = createRestTemplate();
        if (this.restTemplate.getUriTemplateHandler() != null) {
            authenticated.setUriTemplateHandler(this.restTemplate.getUriTemplateHandler());
        }
        authenticated.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        return new TestRestTemplate(authenticated, this.rootUri);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType) {
        return this.restTemplate.exchange(resolveUrl(url), method, requestEntity == null ? HttpEntity.EMPTY : requestEntity, responseType);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType,
                                          Object... uriVariables) {
        return this.restTemplate.exchange(resolveUrl(url), method, requestEntity == null ? HttpEntity.EMPTY : requestEntity, responseType, uriVariables);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType) {
        return this.restTemplate.exchange(resolveUrl(url), method, requestEntity == null ? HttpEntity.EMPTY : requestEntity, responseType);
    }

    public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request, Class<T> responseType) {
        return this.restTemplate.postForEntity(resolveUrl(url), request == null ? HttpEntity.EMPTY : request, responseType);
    }

    public URI postForLocation(String url, @Nullable Object request) {
        return this.restTemplate.postForLocation(resolveUrl(url), request == null ? HttpEntity.EMPTY : request);
    }

    public void put(String url, @Nullable HttpEntity<?> request) {
        this.restTemplate.put(resolveUrl(url), request == null ? HttpEntity.EMPTY : request);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
        return this.restTemplate.getForEntity(resolveUrl(url), responseType);
    }

    public void delete(String url) {
        this.restTemplate.delete(resolveUrl(url));
    }

    private String resolveUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://") || this.rootUri == null || this.rootUri.isEmpty()) {
            return url;
        }
        if (url.startsWith("/")) {
            return this.rootUri + url;
        }
        return this.rootUri + "/" + url;
    }

    private static String normalizeRootUri(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(URI url, HttpMethod method, org.springframework.http.client.ClientHttpResponse response) throws IOException {
            }
        });
        return restTemplate;
    }
}
