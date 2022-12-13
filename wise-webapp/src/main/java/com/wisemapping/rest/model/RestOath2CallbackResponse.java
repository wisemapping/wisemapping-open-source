package com.wisemapping.rest.model;

public class RestOath2CallbackResponse {

	private String email;
	private Boolean googleSync;
	private String syncCode;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getGoogleSync() {
		return googleSync;
	}

	public void setGoogleSync(Boolean googleSync) {
		this.googleSync = googleSync;
	}

	public String getSyncCode() {
		return syncCode;
	}

	public void setSyncCode(String syncCode) {
		this.syncCode = syncCode;
	}

}
