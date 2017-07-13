package com.github.foxty.topaz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.foxty.topaz.tool.Mocks;

/**
 * Created by foxty on 17/6/14.
 */
public class CoreFilterTest {

	static String cfgFile = ClassLoader.class.getResource("/topaz.properties").getFile();
	static CoreFilter filter;

	@BeforeClass
	public static void setup() throws Exception {
		filter = new CoreFilter();
		FilterConfig config = Mocks.mockServletConfig(new HashMap<String, String>() {
			{
				put("controllerPackage", "");
				put("viewBase", "/config/");
				put("configFile", cfgFile);
				put("xssFilterOn", "true");
			}
		});
		filter.init(config);
	}

	@AfterClass
	public static void done() {
		filter.destroy();
	}

	private Map<String, Controller> checkControllerMap(CoreFilter filter) throws Exception {
		Map<String, Controller> controllerMap = Mocks.getPrivate(filter, "controllerMap");
		assertEquals(1, controllerMap.size());
		assertTrue(controllerMap.containsKey(TestController.class.getName()));
		return controllerMap;
	}

	private Map<String, Endpoints> checkEndpointMap(CoreFilter filter) throws Exception {
		Map<String, Endpoints> endpointsMap = Mocks.getPrivate(filter, "endpointsMap");
		assertEquals(6, endpointsMap.size());
		assertTrue(endpointsMap.containsKey("/test"));
		assertTrue(endpointsMap.containsKey("/test/post"));

		Endpoints eps1 = endpointsMap.get("/test");
		Endpoints eps2 = endpointsMap.get("/test/post");
		assertEquals("/test", eps1.findEndpoint(HttpMethod.GET).getEndpointUri());
		assertEquals("/test/post", eps2.findEndpoint(HttpMethod.POST).getEndpointUri());
		return endpointsMap;
	}

	@Test
	public void testInit() throws Exception {
		// Check init params
		String contPkgName = Mocks.getPrivate(filter, "contPackageName");
		String viewBase = Mocks.getPrivate(filter, "viewBase");
		boolean xssFilterOn = Mocks.getPrivate(filter, "xssFilterOn");
		assertEquals(CoreFilter.DEFAULT_CONT_PACKAGE, contPkgName);
		assertEquals("/config/", viewBase);
		assertEquals(true, xssFilterOn);

		// Check scan controller/intercepter/endpoint
		checkControllerMap(filter);
		checkEndpointMap(filter);
	}

	@Test
	public void testAccessValidEndpoint() throws Exception {
		HttpServletRequest request = Mocks.httpRequest(HttpMethod.GET, "", "/test", null);
		HttpServletResponse response = Mocks.httpResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(request).setCharacterEncoding("UTF-8");
		verify(response).setCharacterEncoding("UTF-8");
		verify(chain, never()).doFilter(request, response);

		Map<String, Controller> controllerMap = checkControllerMap(filter);
		Controller c = controllerMap.get(TestController.class.getName());
		TestController tc = (TestController) c.getResource();
		assertTrue(tc.testGetAccessed);
		assertFalse(tc.testPostAccessed);
	}

	@Test
	public void testAccessNonexistEndpoint() throws Exception {
		HttpServletRequest request = Mocks.httpRequest(HttpMethod.GET, "", "/test1", null);
		HttpServletResponse response = Mocks.httpResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	public void testAccessNotAllowedEndpoint() throws Exception {
		HttpServletRequest request = Mocks.httpRequest(HttpMethod.GET, "", "/test/post", null);
		HttpServletResponse response = Mocks.httpResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);
		filter.doFilter(request, response, chain);
		verify(chain, never()).doFilter(request, response);
		verify(response).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
}
