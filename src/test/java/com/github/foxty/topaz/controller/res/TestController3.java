package com.github.foxty.topaz.controller.res;

import com.github.foxty.topaz.annotation.Controller;
import com.github.foxty.topaz.annotation.Endpoint;
import com.github.foxty.topaz.controller.HttpMethod;

/**
 * Created by itian on 6/13/2017.
 */
@Controller(uri = "/test3/users/{id}")
public class TestController3 {

	@Endpoint(method = HttpMethod.GET)
	public void getUser() {

	}

	@Endpoint(method = HttpMethod.POST)
	public void createUser() {

	}

	@Endpoint(uri = "info", method = HttpMethod.GET)
	public void getUserInfo() {

	}
}