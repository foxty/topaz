package com.github.foxty.topaz.controller.res;

import com.github.foxty.topaz.annotation.Controller;
import com.github.foxty.topaz.annotation.Endpoint;
import com.github.foxty.topaz.controller.HttpMethod;

/**
 * Created by itian on 6/13/2017.
 */
@Controller(uri = "/test2", interceptors = { TestInterceptor.class })
public class TestController2 {

	@Endpoint(uri = "res1", method = HttpMethod.GET)
	public void getRes1() {

	}

	@Endpoint(uri = "res1", method = HttpMethod.POST)
	public void createRes1() {

	}

	@Endpoint(uri = "res1", method = HttpMethod.PUT)
	public void updateRes1() {

	}

	@Endpoint(uri = "res1", method = HttpMethod.DELETE)
	public void deleteRes1() {

	}

}