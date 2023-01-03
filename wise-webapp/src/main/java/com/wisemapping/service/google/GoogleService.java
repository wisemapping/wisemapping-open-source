package com.wisemapping.service.google;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.wisemapping.service.http.HttpInvoker;
import com.wisemapping.service.http.HttpInvokerContentType;
import com.wisemapping.service.http.HttpInvokerException;
import com.wisemapping.service.http.HttpMethod;

@Service
public class GoogleService {
	private HttpInvoker httpInvoker;
	private String optinConfirmUrl;
	private String accountBasicDataUrl;
	private String clientId;
	private String clientSecret;
	private String callbackUrl;

	public void setHttpInvoker(HttpInvoker httpInvoker) {
		this.httpInvoker = httpInvoker;
	}

	public void setOptinConfirmUrl(String optinConfirmUrl) {
		this.optinConfirmUrl = optinConfirmUrl;
	}

	public void setAccountBasicDataUrl(String accountBasicDataUrl) {
		this.accountBasicDataUrl = accountBasicDataUrl;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	private String getNodeAsString(JsonNode node, String fieldName) {
		return getNodeAsString(node, fieldName, null);
	}

	private String getNodeAsString(JsonNode node, String fieldName, String defaultValue) {
		JsonNode subNode = node.get(fieldName);
		return subNode != null ? subNode.asText() : defaultValue;
	}

	private Map<String, String> getHeaders(String token) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-type", "application/json");
		headers.put("Authorization", "Bearer " + token);
		return headers;
	}

	private GoogleAccountBasicData getAccountBasicData(String token) throws HttpInvokerException {
		JsonNode response = httpInvoker.invoke(accountBasicDataUrl, null, HttpMethod.GET, this.getHeaders(token), null,
				null);
		GoogleAccountBasicData data = new GoogleAccountBasicData();
		data.setEmail(getNodeAsString(response, "email"));
		data.setAccountId(getNodeAsString(response, "id"));
		data.setName(getNodeAsString(response, "given_name", data.getEmail()));
		data.setLastName(getNodeAsString(response, "family_name"));
		return data;
	}

	private Map<String, String> getOptinConfirmBody(String code) {
		Map<String, String> result = new HashMap<String, String>();
		result.put("client_id", clientId);
		result.put("client_secret", clientSecret);
		result.put("code", code);
		result.put("redirect_uri", callbackUrl);
		result.put("grant_type", "authorization_code");
		return result;
	}

	public GoogleAccountBasicData processCallback(String code)
			throws HttpInvokerException, JsonMappingException, JsonProcessingException {
		Map<String, String> body = this.getOptinConfirmBody(code);
		JsonNode optinConfirmResponse = httpInvoker.invoke(
				optinConfirmUrl,
				HttpInvokerContentType.FORM_ENCODED,
				HttpMethod.POST,
				null,
				null,
				body);

		String accessToken = getNodeAsString(optinConfirmResponse, "access_token");
		String refreshToken = getNodeAsString(optinConfirmResponse, "refresh_token");

		GoogleAccountBasicData data = this.getAccountBasicData(accessToken);
		data.setAccessToken(accessToken);
		data.setRefreshToken(refreshToken);
		return data;
	}

}