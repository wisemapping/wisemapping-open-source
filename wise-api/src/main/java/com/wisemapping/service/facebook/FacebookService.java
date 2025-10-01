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
package com.wisemapping.service.facebook;

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
public class FacebookService {
	@Autowired
	private HttpInvoker httpInvoker;
	@Value("${app.security.oauth2.facebook.confirmUrl:}")
	private String optinConfirmUrl;
	@Value("${app.security.oauth2.facebook.userinfoUrl:}")
	private String accountBasicDataUrl;
	@Value("${app.security.oauth2.facebook.clientId:}")
	private String clientId;
	@Value("${app.security.oauth2.facebook.clientSecret:}")
	private String clientSecret;
	@Value("${app.security.oauth2.facebook.callbackUrl:}")
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

	private FacebookAccountBasicData getAccountBasicData(String token) throws HttpInvokerException {
		JsonNode response = httpInvoker.invoke(accountBasicDataUrl, null, HttpMethod.GET, this.getHeaders(token), null,
				null);
		FacebookAccountBasicData data = new FacebookAccountBasicData();
		data.setEmail(getNodeAsString(response, "email"));
		data.setAccountId(getNodeAsString(response, "id"));
		data.setName(getNodeAsString(response, "first_name", data.getEmail()));
		data.setLastName(getNodeAsString(response, "last_name"));
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

	public FacebookAccountBasicData processCallback(final String code)
			throws HttpInvokerException {

		final Map<String, String> body = this.getOptinConfirmBody(code);
		final JsonNode optionConfirmResponse = httpInvoker.invoke(
				optinConfirmUrl,
				HttpInvokerContentType.FORM_ENCODED,
				HttpMethod.POST,
				null,
				null,
				body);

		final String accessToken = getNodeAsString(optionConfirmResponse, "access_token");

		final FacebookAccountBasicData data = this.getAccountBasicData(accessToken);
		data.setAccessToken(accessToken);
		return data;
	}

}