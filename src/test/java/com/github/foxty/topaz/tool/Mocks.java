package com.github.foxty.topaz.tool;

import com.github.foxty.topaz.controller.HttpMethod;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * Mock object for all unit test.
 * Created by itian on 6/14/2017.
 */
public class Mocks {

    static public HttpServletRequest mockHttpServletRequest(HttpMethod method, String ctxPath,
                                                            String requestURI, Map<String, String> reqParams) {
        HttpServletRequest mockReq = mock(HttpServletRequest.class);
        when(mockReq.getMethod()).thenReturn(method.name());
        when(mockReq.getRequestURI()).thenReturn(requestURI);
        when(mockReq.getContextPath()).thenReturn(ctxPath);
        if (reqParams != null) {
            for (String param : reqParams.keySet()) {
                when(mockReq.getParameter(param)).thenReturn(reqParams.get(param));
            }
        }

        // Mock session and ServletContext
        HttpSession mockSess = mock(HttpSession.class);
        ServletContext mockCtx = mock(ServletContext.class);

        when(mockReq.getSession()).thenReturn(mockSess);
        when(mockSess.getServletContext()).thenReturn(mockCtx);
        return mockReq;
    }

    static public HttpServletResponse mockHttpServletResponse() {
        HttpServletResponse mockRes = Mockito.mock(HttpServletResponse.class);
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
    static public <T> T getPrivateFieldValue(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
