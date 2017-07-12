package com.github.foxty.topaz.controller.response;

/**
 * Json response
 * 
 * Created by itian on 6/15/2017.
 */
public class Json extends Response {

	private Json() {
	}

	public static Json create(String key, Object data) {
		Json j = new Json();
		j.data(key, data);
		return j;
	}
}
