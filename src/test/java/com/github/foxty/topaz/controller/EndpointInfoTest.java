package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.anno.Endpoint;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by itian on 6/13/2017.
 */
public class EndpointInfoTest {

    @Test
    public void testEndpoint() throws Exception {
        TestController tc = new TestController();
        Method m = tc.getClass().getMethod("get", null);
        EndpointInfo ep = new EndpointInfo("/", new ArrayList<>(), tc, m);

        assertEquals("/", ep.getBaseUri());
        assertEquals("/", ep.getEndpointUri());
    }
}
