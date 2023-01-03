package com.wisemapping.service.google;

public class GoogleAccountBasicData {

	private String email;
	private String accountId;
	private String name;
	private String lastName;
	private String accessToken;
	private String refreshToken;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
 	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@Override
	public String toString() {
		return "GoogleAccountBasicData [email=" + email + ", accountId=" + accountId + ", name=" + name + ", lastName="
				+ lastName + ", accessToken=" + accessToken + ", refreshToken=" + refreshToken + "]";
	}
	
}
