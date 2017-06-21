package com.github.foxty.topaz.tool;

import com.github.foxty.topaz.controller.HttpMethod;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by itian on 6/21/2017.
 */
public class HttpServletRequestMockBuilder {

    private HttpServletRequest request;
    private HttpSession session;
    private ServletContext servletContext;

    public HttpServletRequestMockBuilder() {
        request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getRequestURI()).thenReturn("/");
        when(request.getContextPath()).thenReturn("/");

        session = mock(HttpSession.class);
        servletContext = mock(ServletContext.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    public HttpServletRequestMockBuilder method(HttpMethod method) {
        when(request.getMethod()).thenReturn(method.name());
        return this;
    }

    public HttpServletRequestMockBuilder context(String contextPath) {
        when(request.getContextPath()).thenReturn(contextPath);
        return this;
    }

    public HttpServletRequestMockBuilder requestURI(String requestURI) {
        when(request.getRequestURI()).thenReturn(requestURI);
        return this;
    }

    public HttpServletRequestMockBuilder param(String name, String value) {
        when(request.getParameter(name)).thenReturn(value);
        return this;
    }

    public HttpServletRequestMockBuilder session(String name, Object value) {
        when(session.getAttribute(name)).thenReturn(value);
        return this;
    }

    public HttpServletRequestMockBuilder header(String name, String value) {
        when(request.getHeader(name)).thenReturn(value);
        return this;
    }

    public HttpServletRequest build() {
        return request;
    }
}
