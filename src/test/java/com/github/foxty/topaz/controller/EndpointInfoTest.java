package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.tool.Mocks;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by itian on 6/13/2017.
 */
public class EndpointInfoTest {

    @Test
    public void testGetEndpoint() throws Exception {
        TestController tc = new TestController();
        Method getMethod = tc.getClass().getMethod("testGet", null);

        List<IInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new TestInterceptor());

        Endpoint endpoint1 = new Endpoint("/", interceptorList, tc, getMethod);
        assertEquals("/", endpoint1.getBaseUri());
        assertEquals("/", endpoint1.getEndpointUri());

        List<IInterceptor> actualInterceptorList = Mocks.getPrivateFieldValue(endpoint1, "interceptorList");
        assertNotEquals(interceptorList, actualInterceptorList);

        HttpMethod method = Mocks.getPrivateFieldValue(endpoint1, "allowHttpMethod");
        assertEquals(HttpMethod.GET, method);

        boolean isTransactional = Mocks.getPrivateFieldValue(endpoint1, "isTransactional");
        assertFalse(isTransactional);
    }


    @Test
    public void testPostEndpoint() throws Exception {
        TestController tc = new TestController();
        Method getMethod = tc.getClass().getMethod("testPost", null);

        List<IInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new TestInterceptor());

        Endpoint endpoint = new Endpoint("/", interceptorList, tc, getMethod);
        assertEquals("/", endpoint.getBaseUri());
        assertEquals("/post", endpoint.getEndpointUri());

        List<IInterceptor> actualInterceptorList = Mocks.getPrivateFieldValue(endpoint, "interceptorList");
        assertNotEquals(interceptorList, actualInterceptorList);

        HttpMethod method = Mocks.getPrivateFieldValue(endpoint, "allowHttpMethod");
        assertEquals(HttpMethod.POST, method);

        boolean isTransactional = Mocks.getPrivateFieldValue(endpoint, "isTransactional");
        assertTrue(isTransactional);


    }
}
