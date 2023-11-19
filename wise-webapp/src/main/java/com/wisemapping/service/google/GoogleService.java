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
package com.wisemapping.service.google;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wisemapping.service.google.http.HttpInvoker;
import com.wisemapping.service.google.http.HttpInvokerContentType;
import com.wisemapping.service.google.http.HttpInvokerException;

@Service
public class GoogleService {
	@Autowired
	private HttpInvoker httpInvoker;
	@Value("${security.oauth2.google.confirmUrl}")
	private String optinConfirmUrl;
	@Value("${security.oauth2.google.userinfoUrl}")
	private String accountBasicDataUrl;
	@Value("${security.oauth2.google.clientId}")
	private String clientId;
	@Value("${security.oauth2.google.clientSecret}")
	private String clientSecret;
	@Value("${security.oauth2.google.callbackUrl}")
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

	public GoogleAccountBasicData processCallback(final String code)
			throws HttpInvokerException {
		Map<String, String> body = this.getOptinConfirmBody(code);
		JsonNode optionConfirmResponse = httpInvoker.invoke(
				optinConfirmUrl,
				HttpInvokerContentType.FORM_ENCODED,
				HttpMethod.POST,
				null,
				null,
				body);

		final String accessToken = getNodeAsString(optionConfirmResponse, "access_token");
		final String refreshToken = getNodeAsString(optionConfirmResponse, "refresh_token");

		GoogleAccountBasicData data = this.getAccountBasicData(accessToken);
		data.setAccessToken(accessToken);
		data.setRefreshToken(refreshToken);
		return data;
	}

}