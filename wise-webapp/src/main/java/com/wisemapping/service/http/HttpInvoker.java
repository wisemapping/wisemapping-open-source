package com.wisemapping.service.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HttpInvoker {

	protected static Logger logger = LogManager.getLogger(HttpInvoker.class);

	private ObjectMapper mapper = new ObjectMapper();

	public HttpInvoker() {
		super();
	}

	public JsonNode invoke(
			String url, 
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
			HttpRequestBase httpRequst = null;

			// build request
			if (method.equals(HttpMethod.POST))
				httpRequst = new HttpPost(url);
			else if (method.equals(HttpMethod.PUT))
				httpRequst = new HttpPut(url);
			else if (method.equals(HttpMethod.GET))
				httpRequst = new HttpGet(url);
			else if (method.equals(HttpMethod.DELETE))
				httpRequst = new HttpDelete(url);
			else
				throw new HttpInvokerException("Method " + method + " not suppoprted by http connector");

			if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
				HttpEntity entity = null;
				if (requestContentType.equals(HttpInvokerContentType.JSON)) {
					if (jsonPayload == null)
						throw new HttpInvokerException("Json content is required");
					entity = new StringEntity(jsonPayload, Charset.forName("UTF-8"));
					((HttpEntityEnclosingRequestBase) httpRequst).setEntity(entity);
				}
				if (requestContentType.equals(HttpInvokerContentType.FORM_ENCODED)) {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					Set<String> keys = formData.keySet();
					for (String key : keys) {
						nameValuePairs.add(new BasicNameValuePair(key, formData.get(key).toString()));
					}
					entity = new UrlEncodedFormEntity(nameValuePairs);
					((HttpEntityEnclosingRequestBase) httpRequst).setEntity(entity);
				}
				if (entity == null)
					throw new HttpInvokerException("Cant build entity to send");
			}

			if (headers != null) {
				Set<String> keys = headers.keySet();
				for (String key : keys) {
					httpRequst.setHeader(key, headers.get(key));
				}
			}

			if (requestContentType != null)
				httpRequst.setHeader("Content-Type", requestContentType.getHttpContentType());

			// invoke
			CloseableHttpResponse response = httpClient.execute(httpRequst);
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
						+ response.getStatusLine().getStatusCode());
			}

			httpRequst.releaseConnection();
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
