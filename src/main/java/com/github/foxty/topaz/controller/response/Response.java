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

	private Map<String, Object> data;

	public <T extends Response> T data(String name, Object value) {
		if (data == null) {
			data = new HashMap<>();
		}
		data.put(name, value);
		return (T) this;
	}

	public Map<String, Object> getData() {
		return data;
	}
}
