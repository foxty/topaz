package com.github.foxty.topaz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.tool.Mocks;

/**
 * Created by itian on 6/13/2017.
 */
public class EndpointTest {

	Controller controller;

	@Before
	public void before() {
		TestController tc = new TestController();
		controller = new Controller(tc);
	}

	@Test
	public void testGetEndpoint() throws Exception {
		TestController tc = new TestController();
		Method getMethod = tc.getClass().getMethod("testGet", null);

		List<IInterceptor> interceptorList = new ArrayList<>();
		interceptorList.add(new TestInterceptor());

		Endpoint endpoint1 = new Endpoint(controller, getMethod);
		assertEquals("/test", endpoint1.getBaseUri());
		assertEquals("/test", endpoint1.getEndpointUri());

		List<IInterceptor> actualInterceptorList = Mocks.getPrivate(endpoint1, "interceptorList");
		assertNotEquals(interceptorList, actualInterceptorList);

		HttpMethod method = Mocks.getPrivate(endpoint1, "allowHttpMethod");
		assertEquals(HttpMethod.GET, method);

		boolean isTransactional = Mocks.getPrivate(endpoint1, "isTransactional");
		assertFalse(isTransactional);
	}

	@Test
	public void testPostEndpoint() throws Exception {
		TestController tc = new TestController();
		Method getMethod = tc.getClass().getMethod("testPost", null);

		List<IInterceptor> interceptorList = new ArrayList<>();
		interceptorList.add(new TestInterceptor());

		Endpoint endpoint = new Endpoint(controller, getMethod);
		assertEquals("/test", endpoint.getBaseUri());
		assertEquals("/test/post", endpoint.getEndpointUri());

		List<IInterceptor> actualInterceptorList = Mocks.getPrivate(endpoint, "interceptorList");
		assertNotEquals(interceptorList, actualInterceptorList);

		HttpMethod method = Mocks.getPrivate(endpoint, "allowHttpMethod");
		assertEquals(HttpMethod.POST, method);

		boolean isTransactional = Mocks.getPrivate(endpoint, "isTransactional");
		assertTrue(isTransactional);
	}
}
