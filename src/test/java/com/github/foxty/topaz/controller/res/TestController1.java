package com.github.foxty.topaz.controller.res;

import java.util.HashMap;
import java.util.Map;

import com.github.foxty.topaz.annotation.Controller;
import com.github.foxty.topaz.annotation.Endpoint;
import com.github.foxty.topaz.controller.HttpMethod;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.controller.response.View;

/**
 * Created by itian on 6/13/2017.
 */
@Controller(uri = "/test", layout = "layout1.ftl", interceptors = { TestInterceptor.class })
public class TestController1 extends TestBaseController {

	public boolean testGetAccessed = false;
	public boolean testPostAccessed = false;

	@Endpoint
	public void testGet() {
		testGetAccessed = true;
	}

	@Endpoint(uri = "/post", method = HttpMethod.POST, isTransactional = true)
	public void testPost() {
		testPostAccessed = true;
	}

	@Endpoint(uri = "html", method = HttpMethod.GET)
	public View renderHtml() {
		return View.create("html.ftl");
	}

	@Endpoint(uri = "json", method = HttpMethod.GET)
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

	@Endpoint(uri = "xml", method = HttpMethod.GET)
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

	@Endpoint(uri = "text", method = HttpMethod.GET)
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
