package com.github.foxty.topaz.controller.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Response object for endpoint
 * 
 * @author itian
 *
 */
public class Response {

	private Map<String, Object> responseData;

	public Response data(String name, Object value) {
		if (responseData == null) {
			responseData = new HashMap<>();
		}
		responseData.put(name, value);
		return this;
	}

	public Map<String, Object> getResponseDate() {
		return responseData;
	}
}
