package com.github.foxty.topaz.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

import com.github.foxty.topaz.controller.interceptor.FinalInterceptor;
import com.github.foxty.topaz.tool.Mocks;

/**
 * Created by itian on 6/21/2017.
 */
public class FinalInterceptorTest {

	private static TestController controller = new TestController();
	private static Controller c = new Controller(controller);

	public void setup(String uri, String headerName, String headerValue) throws IOException {
		HttpServletRequest req = Mocks.httpRequestBuilder().requestURI(uri).header(headerName, headerValue)
				.param("prop", "test").param("int16", "16").param("float", "5.123")
				.param("string", "this is a string value").build();

		HttpServletResponse resp = Mocks.httpResponse();
		WebContext.create(req, resp, "/");
	}

	@Test
	public void testRnderJSON() throws Exception {
		setup("/test/json", "Accept", "application/json");
		WebContext wc = WebContext.get();
		HttpServletResponse response = wc.getResponse();

		Method m = TestController.class.getMethod("renderJson");
		FinalInterceptor interceptor = new FinalInterceptor(c, m);
		interceptor.intercept(null);

		verify(response).setContentType("application/json");
		verify(response.getWriter()).write(anyString());
	}

	@Test
	public void testRenderXML() throws Exception {
		setup("/test/xml", "Accept", "application/xml");
		WebContext wc = WebContext.get();
		HttpServletResponse response = wc.getResponse();

		Method m = TestController.class.getMethod("renderXml");
		FinalInterceptor interceptor = new FinalInterceptor(c, m);
		interceptor.intercept(null);

		verify(response).setContentType("application/xml");
		verify(response.getWriter()).write(anyString());
	}

	@Test
	@Ignore
	public void testRenderHTML() throws Exception {
		setup("/test/html", "Accept", "text/html");
		WebContext wc = WebContext.get();
		HttpServletRequest request = wc.getRequest();
		HttpServletResponse response = wc.getResponse();

		Method m = TestController.class.getMethod("renderHtml");
		FinalInterceptor interceptor = new FinalInterceptor(c, m);
		interceptor.intercept(null);

		verify(response).setContentType("text/html");
		verify(wc.getApplication().getRequestDispatcher(any())).include(request, response);
	}

	@Test
	public void testRnderText() throws Exception {
		setup("/test/text", "Accept", "text/plain");
		WebContext wc = WebContext.get();
		HttpServletRequest request = wc.getRequest();
		HttpServletResponse response = wc.getResponse();

		Method m = TestController.class.getMethod("renderText");
		FinalInterceptor interceptor = new FinalInterceptor(c, m);
		interceptor.intercept(null);

		verify(response).setContentType("text/plain");
		verify(response.getWriter()).write(anyString());
	}
}
