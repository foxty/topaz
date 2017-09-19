package com.github.foxty.topaz.controller.res;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.annotation._Endpoint;
import com.github.foxty.topaz.controller.HttpMethod;

/**
 * Created by itian on 6/13/2017.
 */
@_Controller(uri = "/test3/users/{id}")
public class TestController3 {

	@_Endpoint(method = HttpMethod.GET)
	public void getUser() {

	}

	@_Endpoint(method = HttpMethod.POST)
	public void createUser() {

	}

	@_Endpoint(uri = "info", method = HttpMethod.GET)
	public void getUserInfo() {

	}
}