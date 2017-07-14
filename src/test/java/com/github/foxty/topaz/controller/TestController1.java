package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.annotation._Endpoint;
import com.github.foxty.topaz.controller.response.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itian on 6/13/2017.
 */
@_Controller(uri = "/test", layout = "layout1.ftl", interceptors = { TestInterceptor.class })
public class TestController1 {

	public boolean testGetAccessed = false;
	public boolean testPostAccessed = false;

	@_Endpoint
	public void testGet() {
		testGetAccessed = true;
	}

	@_Endpoint(uri = "/post", method = HttpMethod.POST, isTransactional = true)
	public void testPost() {
		testPostAccessed = true;
	}

	@_Endpoint(uri = "html", method = HttpMethod.GET)
	public View renderHtml() {
		return View.create("html.ftl");
	}

	@_Endpoint(uri = "json", method = HttpMethod.GET)
	public Map<String, Object> renderJson() {
		WebContext wc = WebContext.get();

		Map<String, Object> result = new HashMap<>();
		result.put("success", Boolean.TRUE);
		result.put("prop", wc.param("prop"));
		result.put("int16", Integer.parseInt(wc.param("int16")));
		result.put("float", Float.parseFloat(wc.param("float")));
		result.put("json", Boolean.TRUE);
		return result;
	}

	@_Endpoint(uri = "xml", method = HttpMethod.GET)
	public Map<String, Object> renderXml() {
		WebContext wc = WebContext.get();

		Map<String, Object> result = new HashMap<>();
		result.put("success", Boolean.TRUE);
		result.put("prop", wc.param("prop"));
		result.put("int16", Integer.parseInt(wc.param("int16")));
		result.put("float", Float.parseFloat(wc.param("float")));
		result.put("xml", Boolean.TRUE);
		return result;
	}

	@_Endpoint(uri = "text", method = HttpMethod.GET)
	public Map<String, Object> renderText() {
		WebContext wc = WebContext.get();

		Map<String, Object> result = new HashMap<>();
		result.put("success", Boolean.TRUE);
		result.put("prop", wc.param("prop"));
		result.put("int16", Integer.parseInt(wc.param("int16")));
		result.put("float", Float.parseFloat(wc.param("float")));
		result.put("text", Boolean.TRUE);
		return result;
	}
}
