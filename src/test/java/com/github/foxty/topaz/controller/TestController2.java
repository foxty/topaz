package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.annotation._Endpoint;

/**
 * Created by itian on 6/13/2017.
 */
@_Controller(uri = "/test2", interceptors = { TestInterceptor.class })
public class TestController2 {

	@_Endpoint(uri = "res1", method = HttpMethod.GET)
	public void getRes1() {

	}

	@_Endpoint(uri = "res1", method = HttpMethod.POST)
	public void createRes1() {

	}

	@_Endpoint(uri = "res1", method = HttpMethod.PUT)
	public void updateRes1() {

	}

	@_Endpoint(uri = "res1", method = HttpMethod.DELETE)
	public void deleteRes1() {

	}

}