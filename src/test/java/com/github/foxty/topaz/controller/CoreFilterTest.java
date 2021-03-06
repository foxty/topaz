package com.github.foxty.topaz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.foxty.topaz.controller.res.TestController1;
import com.github.foxty.topaz.controller.res.TestController2;
import com.github.foxty.topaz.controller.res.TestController3;
import com.github.foxty.topaz.controller.res.TestLauncher1;
import com.github.foxty.topaz.tool.Mocks;

/**
 * Created by foxty on 17/6/14.
 */
public class CoreFilterTest {

	static String cfgFile = ClassLoader.class.getResource("/test_topaz.properties").getFile();
	static CoreFilter filter;

	@BeforeClass
	public static void setup() throws Exception {
		filter = new CoreFilter();
		FilterConfig config = Mocks.mockServletConfig(new HashMap<String, String>() {
			{
				put("scanPath", "");
				put("viewBase", "/config/");
				put("configFile", cfgFile);
				put("xssFilterOn", "true");
			}
		});
		filter.init(config);
	}

	@AfterClass
	public static void done() throws Exception {
		filter.destroy();
		ExecutorService launcherExecutor = Mocks.getPrivate(filter, "launcherExecutor");
		assertTrue(launcherExecutor.isShutdown());
	}

	private Map<String, Controller> checkControllerMap(CoreFilter filter) throws Exception {
		Map<String, Controller> controllerMap = Mocks.getPrivate(filter, "controllerUriMap");
		assertEquals(3, controllerMap.size());
		Controller c1 = controllerMap.get("/test");
		Controller c2 = controllerMap.get("/test2");
		Controller c3 = controllerMap.get("/test3/users/{id}");
		assertTrue(c1.getResource() instanceof TestController1);
		assertTrue(c2.getResource() instanceof TestController2);
		assertTrue(c3.getResource() instanceof TestController3);
		assertEquals(1, c1.getIntercepters().size());
		assertEquals(1, c2.getIntercepters().size());
		assertEquals(0, c3.getIntercepters().size());

		assertEquals(6, c1.getEndpointCount());
		assertEquals(4, c2.getEndpointCount());
		assertEquals(3, c3.getEndpointCount());
		return controllerMap;
	}

	@Test
	public void testInit() throws Exception {
		// Check init params
		String contPkgName = Mocks.getPrivate(filter, "scanPath");
		String viewBase = Mocks.getPrivate(filter, "viewBase");
		boolean xssFilterOn = Mocks.getPrivate(filter, "xssFilterOn");
		assertEquals(CoreFilter.DEFAULT_CONT_PACKAGE, contPkgName);
		assertEquals("/config/", viewBase);
		assertEquals(true, xssFilterOn);

		// Check scan controller/intercepter/endpoint
		checkControllerMap(filter);
	}

	@Test
	public void testLauncher() throws Exception {
		List<Runnable> launchers = Mocks.getPrivate(filter, "launchers");
		Runnable launcher = launchers.get(0);
		assertEquals(1, launchers.size());
		assertTrue(launcher instanceof TestLauncher1);
		assertTrue(((TestLauncher1) launcher).executed);
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
		Controller c = controllerMap.get("/test");
		TestController1 tc = (TestController1) c.getResource();
		assertTrue(tc.testGetAccessed);
		assertFalse(tc.testPostAccessed);
	}
	
	@Test
	public void testMultiLayerEndpoint() throws Exception {
		HttpServletRequest request = Mocks.httpRequest(HttpMethod.GET, "", "/test3/users/{id}", null);
		HttpServletResponse response = Mocks.httpResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(request).setCharacterEncoding("UTF-8");
		verify(response).setCharacterEncoding("UTF-8");
		verify(chain, never()).doFilter(request, response);
		checkControllerMap(filter);
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

	@Test(expected = ControllerException.class)
	public void testInvalidScanPath() throws Exception {
		CoreFilter f = new CoreFilter();
		FilterConfig config = Mocks.mockServletConfig(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("scanPath", "test.a.b");
				put("viewBase", "/config/");
				put("configFile", cfgFile);
				put("xssFilterOn", "true");
			}
		});
		f.init(config);
	}
}
