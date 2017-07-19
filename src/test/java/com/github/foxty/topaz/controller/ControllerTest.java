/**
 * 
 */
package com.github.foxty.topaz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.foxty.topaz.controller.interceptor.IIntercepter;
import com.github.foxty.topaz.controller.res.TestController1;
import com.github.foxty.topaz.controller.res.TestController2;

/**
 * @author itian
 *
 */
public class ControllerTest {

	static Controller controller1;
	static Controller controller2;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		controller1 = new Controller(new TestController1());
		controller2 = new Controller(new TestController2());
	}

	/**
	 * Test method for
	 * {@link com.github.foxty.topaz.controller.Controller#getUri()}.
	 */
	@Test
	public void testGetUri() {
		assertEquals("/test", controller1.getUri());
		assertEquals("/test2", controller2.getUri());
	}

	/**
	 * Test method for
	 * {@link com.github.foxty.topaz.controller.Controller#getLayout()}.
	 */
	@Test
	public void testGetLayout() {
		assertEquals("layout1.ftl", controller1.getLayout());
		assertEquals("", controller2.getLayout());
	}

	/**
	 * Test method for
	 * {@link com.github.foxty.topaz.controller.Controller#getResource()}.
	 */
	@Test
	public void testGetResource() {
		Object res11 = controller1.getResource();
		Object res12 = controller1.getResource();
		assertNotNull(res11);
		assertNotNull(res12);
		assertEquals(res11, res12);
	}

	/**
	 * Test method for
	 * {@link com.github.foxty.topaz.controller.Controller#getIntercepters()}.
	 */
	@Test
	public void testGetInterceptors() {
		List<IIntercepter> intercepter11 = controller1.getIntercepters();
		List<IIntercepter> intercepter12 = controller1.getIntercepters();
		assertEquals(1, intercepter11.size());
		assertEquals(1, intercepter12.size());
		assertEquals(intercepter11, intercepter12);

		List<IIntercepter> intercepter21 = controller2.getIntercepters();
		List<IIntercepter> intercepter22 = controller2.getIntercepters();
		assertEquals(1, intercepter21.size());
		assertEquals(1, intercepter22.size());
	}

	@Test
	public void testfindEndpoint() throws Exception {
		Endpoint ep = controller1.findEndpoint("/test", HttpMethod.GET);
		assertEquals("/test", ep.getEndpointUri());
		assertEquals(HttpMethod.ANY, ep.getAllowHttpMethod());

		ep = controller1.findEndpoint("/test/post", HttpMethod.POST);
		assertEquals("/test/post", ep.getEndpointUri());
		assertEquals(HttpMethod.POST, ep.getAllowHttpMethod());
		assertEquals(null, controller1.findEndpoint("/aaa", HttpMethod.GET));

		ep = controller2.findEndpoint("/test2/res1", HttpMethod.GET);
		assertEquals("/test2/res1", ep.getEndpointUri());
		assertEquals(HttpMethod.GET, ep.getAllowHttpMethod());

		ep = controller2.findEndpoint("/test2/res1", HttpMethod.PUT);
		assertEquals("/test2/res1", ep.getEndpointUri());
		assertEquals(HttpMethod.PUT, ep.getAllowHttpMethod());
	}

}
