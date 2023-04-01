/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.service.google.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.constraints.NotNull;

@Service
public class HttpInvoker {

    protected static Logger logger = LogManager.getLogger(HttpInvoker.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public HttpInvoker() {
        super();
    }

    public JsonNode invoke(
            @NotNull String url,
            HttpInvokerContentType requestContentType,
            HttpMethod method,
            Map<String, String> headers,
            String jsonPayload,
            Map<String, String> formData)
            throws HttpInvokerException {
        String responseBody = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("finalUrl: " + url);
                logger.debug("method: " + method);
                logger.debug("payload: " + jsonPayload);
                logger.debug("header: " + headers);
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpRequestBase httpRequest;

            // build request
            if (method.equals(HttpMethod.POST))
                httpRequest = new HttpPost(url);
            else if (method.equals(HttpMethod.PUT))
                httpRequest = new HttpPut(url);
            else if (method.equals(HttpMethod.GET))
                httpRequest = new HttpGet(url);
            else if (method.equals(HttpMethod.DELETE))
                httpRequest = new HttpDelete(url);
            else
                throw new HttpInvokerException("Method " + method + " not supported by http connector");

            if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
                HttpEntity entity = null;
                if (requestContentType.equals(HttpInvokerContentType.JSON)) {
                    if (jsonPayload == null)
                        throw new HttpInvokerException("Json content is required");
                    entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
                    ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(entity);
                }
                if (requestContentType.equals(HttpInvokerContentType.FORM_ENCODED)) {
                    List<NameValuePair> nameValuePairs = new ArrayList<>();
                    Set<String> keys = formData.keySet();
                    for (String key : keys) {
                        nameValuePairs.add(new BasicNameValuePair(key, formData.get(key).toString()));
                    }
                    entity = new UrlEncodedFormEntity(nameValuePairs);
                    ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(entity);
                }
                if (entity == null)
                    throw new HttpInvokerException("Cant build entity to send");
            }

            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    httpRequest.setHeader(key, headers.get(key));
                }
            }

            if (requestContentType != null)
                httpRequest.setHeader("Content-Type", requestContentType.getHttpContentType());

            // invoke
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            // response process
            JsonNode root = null;
            responseBody = response.getEntity() != null && response.getEntity().getContent() != null
                    ? IOUtils.toString(response.getEntity().getContent(), (String) null)
                    : null;
            if (responseBody != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("response plain: " + responseBody);
                }
                try {
                    root = mapper.readTree(responseBody);
                } catch (Exception e) {
                    int returnCode = response.getStatusLine().getStatusCode();
                    throw new HttpInvokerException("cant transform response to JSON. RQ: " + jsonPayload + ", RS: "
                            + responseBody + ", status: " + returnCode, e);
                }
            }

            if (response.getStatusLine().getStatusCode() >= 400) {
                logger.error("error response: " + responseBody);
                throw new HttpInvokerException("error invoking " + url + ", response: " + responseBody + ", status: "
                        + response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
            }

            httpRequest.releaseConnection();
            response.close();
            httpClient.close();

            return root;
        } catch (HttpInvokerException e) {
            throw e;
        } catch (Exception e) {
            logger.error("cant invoke service " + url);
            logger.error("response: " + responseBody, e);
            throw new HttpInvokerException("cant invoke service " + url, e);
        }
    }


}
