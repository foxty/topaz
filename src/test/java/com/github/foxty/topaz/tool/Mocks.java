package com.github.foxty.topaz.tool;

import com.github.foxty.topaz.controller.HttpMethod;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * Mock object for all unit test.
 * Created by itian on 6/14/2017.
 */
public class Mocks {

    static public HttpServletRequest httpRequest(HttpMethod method, String ctxPath,
                                                 String requestURI, Map<String, String> reqParams) {
        HttpServletRequestMockBuilder reqBuilder = new HttpServletRequestMockBuilder();
        reqBuilder.method(method).context(ctxPath).requestURI(requestURI).header("Accept", "text/html");

        if (reqParams != null) {
            for (String param : reqParams.keySet()) {
                reqBuilder.param(param, reqParams.get(param));
            }
        }
        return reqBuilder.build();
    }

    static public HttpServletRequestMockBuilder httpRequestBuilder() {
        return new HttpServletRequestMockBuilder();
    }

    static public HttpServletResponse httpResponse() throws IOException {
        HttpServletResponse mockRes = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = Mockito.mock(PrintWriter.class);
        when(mockRes.getWriter()).thenReturn(writer);
        return mockRes;
    }

    static public FilterConfig mockServletConfig(Map<String, String> initParams) {
        FilterConfig mockConfig = Mockito.mock(FilterConfig.class);
        for (String key : initParams.keySet()) {
            Mockito.when(mockConfig.getInitParameter(key)).thenReturn(initParams.get(key));
        }

        Mockito.when(mockConfig.getInitParameterNames())
                .thenReturn(Collections.enumeration(initParams.keySet()));
        return mockConfig;
    }

    /**
     * Return field value of object
     *
     * @param obj
     * @param name
     * @return
     */
    static public <T> T getPrivate(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}

