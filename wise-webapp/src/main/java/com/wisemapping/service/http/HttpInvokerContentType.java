package com.wisemapping.service.http;

public enum HttpInvokerContentType {

	JSON("application/json"), 
	FORM_ENCODED("application/x-www-form-urlencoded");

	private String httpContentType;
	
	private HttpInvokerContentType(String type) {
		this.httpContentType = type;
	}

	public String getHttpContentType() {
		return httpContentType;
	}
	
}