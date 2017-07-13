package com.github.foxty.topaz.controller.response;

/**
 * Json response
 * 
 * Created by itian on 6/15/2017.
 */
public class Json extends Response {

	private int statusCode;

	private Json(int code) {
		this.statusCode = code;
	}

	public static Json create(int code, String key, Object data) {
		Json j = new Json(code);
		j.data(key, data);
		return j;
	}

	public static Json create(String key, Object data) {
		return create(200, key, data);
	}

	public void statusCode(int code) {
		statusCode = code;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
