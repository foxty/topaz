package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.tool.Mocks;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by itian on 6/21/2017.
 */
public class WebContextTest {

	public void setup(String headerName, String headerValue) throws IOException {
		HttpServletRequest req = Mocks.httpRequestBuilder().header(headerName, headerValue).build();
		HttpServletResponse resp = Mocks.httpResponse();
		WebContext.create(req, resp, "/");
	}

	@Test
	public void testAcceptJson() throws Exception {
		setup("Accept", "application/json");
		WebContext wc = WebContext.get();

		List<WebContext.Accept> acceptList = wc.getAccept();
		assertEquals(1, acceptList.size());
		assertTrue(acceptList.contains(WebContext.Accept.JSON));
	}

	@Test
	public void testAcceptXml() throws Exception {
		setup("Accept", "application/xml");
		WebContext wc = WebContext.get();

		List<WebContext.Accept> acceptList = wc.getAccept();
		assertEquals(1, acceptList.size());
		assertTrue(acceptList.contains(WebContext.Accept.XML));
	}

	@Test
	public void testAcceptHtml() throws Exception {
		setup("Accept", "text/html");
		WebContext wc = WebContext.get();

		List<WebContext.Accept> acceptList = wc.getAccept();
		assertEquals(1, acceptList.size());
		assertTrue(acceptList.contains(WebContext.Accept.HTML));
	}

	@Test
	public void testAcceptText() throws Exception {
		setup("Accept", "text/plain");
		WebContext wc = WebContext.get();

		List<WebContext.Accept> acceptList = wc.getAccept();
		assertEquals(1, acceptList.size());
		assertTrue(acceptList.contains(WebContext.Accept.PLAIN));
	}

}
