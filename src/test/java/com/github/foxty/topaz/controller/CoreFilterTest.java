package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.tool.Mocks;
import org.junit.AfterClass;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by foxty on 17/6/14.
 */
public class CoreFilterTest {

    static String cfgFile = ClassLoader.class.getResource("/topaz.properties").getFile();
    static CoreFilter filter;

    @BeforeClass
    public static void setup() throws Exception {
        filter = new CoreFilter();
        FilterConfig config = Mocks.mockServletConfig(new HashMap<String, String>(){{
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

    private Map<String, Object> checkControllerMap(CoreFilter filter) throws Exception {
        Map<String, Object> controllerMap = Mocks.getPrivateFieldValue(filter, "controllerMap");
        assertEquals(1, controllerMap.size());
        assertTrue(controllerMap.containsKey(TestController.class.getName()));
        return controllerMap;
    }

    private Map<String, IInterceptor> checkInterceptorMap(CoreFilter filter) throws Exception {
        Map<String, IInterceptor> interceptorMap = Mocks.getPrivateFieldValue(filter, "interceptorMap");
        assertEquals(1, interceptorMap.size());
        assertTrue(interceptorMap.containsKey(TestInterceptor.class.getName()));
        return interceptorMap;
    }

    private Map<String, Endpoint> checkEndpointMap(CoreFilter filter) throws Exception {
        Map<String, Endpoint> endpointMap = Mocks.getPrivateFieldValue(filter, "endpointMap");
        assertEquals(2, endpointMap.size());
        assertTrue(endpointMap.containsKey("/test"));
        assertTrue(endpointMap.containsKey("/test/post"));

        Endpoint ep1 = endpointMap.get("/test");
        Endpoint ep2 = endpointMap.get("/test/post");
        assertEquals("/test", ep1.getEndpointUri());
        assertEquals("/test/post", ep2.getEndpointUri());
        return endpointMap;
    }

    @Test
    public void testInit() throws Exception {
        // Check init params
        String contPkgName = Mocks.getPrivateFieldValue(filter, "contPackageName");
        String viewBase = Mocks.getPrivateFieldValue(filter, "viewBase");
        boolean xssFilterOn = Mocks.getPrivateFieldValue(filter, "xssFilterOn");
        assertEquals(CoreFilter.DEFAULT_CONT_PACKAGE, contPkgName);
        assertEquals("/config/", viewBase);
        assertEquals(true, xssFilterOn);

        //Check scan controller/interceptor/endpoints
        checkControllerMap(filter);
        checkInterceptorMap(filter);
        checkEndpointMap(filter);
    }

    @Test
    public void testAccessValidEndpoint() throws Exception {
        HttpServletRequest request = Mocks.mockHttpServletRequest(HttpMethod.GET, "",
                "/test", null);
        HttpServletResponse response = Mocks.mockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(chain, never()).doFilter(request, response);


        Map<String, Object> controllerMap = checkControllerMap(filter);
        TestController tc = (TestController) controllerMap.get(TestController.class.getName());
        assertTrue(tc.testGetAccessed);
        assertFalse(tc.testPostAccessed);

        WebContext wc = WebContext.get();
        Endpoint endpointInfo =  wc.getEndpoint();
        assertNotNull(endpointInfo);
        assertEquals("/test", endpointInfo.getEndpointUri());

    }

    @Test
    public void testAccessNonexistEndpoint() throws Exception {
        HttpServletRequest request = Mocks.mockHttpServletRequest(HttpMethod.GET, "",
                "/test1", null);
        HttpServletResponse response = Mocks.mockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testAccessNotAllowedEndpoint() throws Exception {
        HttpServletRequest request = Mocks.mockHttpServletRequest(HttpMethod.POST, "",
                "/test", null);
        HttpServletResponse response = Mocks.mockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
