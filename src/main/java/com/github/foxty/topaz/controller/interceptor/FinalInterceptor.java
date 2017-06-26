package com.github.foxty.topaz.controller.interceptor;

import com.alibaba.fastjson.JSON;
import com.github.foxty.topaz.common.ControllerException;
import com.github.foxty.topaz.controller.View;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.annotation._Controller;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Final interceptor was last in the chain to handle the resource request.
 *
 * @author itian
 */
final public class FinalInterceptor implements IInterceptor {

    private static String WEB_ERRORS = "errors";
    private static String LAYOUT_CHILDREN = "children";
    private static Log log = LogFactory.getLog(FinalInterceptor.class);
    private Object controller;
    private Method targetMethod;
    private _Controller _controller;

    public FinalInterceptor(Object controller, Method targetMethod) {
        this.controller = controller;
        this.targetMethod = targetMethod;
        this._controller = controller.getClass().getAnnotation(_Controller.class);
        Objects.requireNonNull(_controller);
    }

    final public void intercept(InterceptorChain chain) {
        WebContext wc = WebContext.get();

        if (log.isDebugEnabled()) {
            log.debug("Execute method " + controller.getClass() + "." + targetMethod.getName());
        }
        Object result = invokeTargetMethod();
        dispatchResponse(result);
    }

    private Object invokeTargetMethod() {
        try {
            return targetMethod.invoke(controller);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof ControllerException) {
                throw (ControllerException) e.getTargetException();
            } else {
                throw new com.github.foxty.topaz.controller.ControllerException(e);
            }
        } catch (Exception e) {
            throw new com.github.foxty.topaz.controller.ControllerException(e);
        }
    }

    /**
     * Dispatch response object to different handler.
     *
     * @param result
     */
    private void dispatchResponse(Object result) {
        WebContext wc = WebContext.get();
        if (result instanceof View) {
            View v = (View) result;
            String layout = _controller.layout();
            if (StringUtils.isBlank(v.getLayout())) {
                v.setLayout(layout);
            }
            renderView(v);
        } else if (wc.isAcceptJson()) {
            renderJson(result);
        } else if (wc.isAcceptXml()) {
            renderXml(result);
        } else if (wc.isAcceptPlain()) {
            renderText(result.toString());
        } else {
            wc.getResponse().setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }

    private void render404() {
        WebContext wc = WebContext.get();
        HttpServletResponse response = wc.getResponse();
        try {
            response.sendError(404);
        } catch (IOException e1) {
        }
    }

    private boolean isAbsolutePath(String resPath) {
        return resPath != null
                && (resPath.startsWith("/") || resPath.startsWith("\\"));
    }

    private void renderView(View v) {
        WebContext wc = WebContext.get();
        HttpServletRequest request = wc.getRequest();
        HttpServletResponse response = wc.getResponse();

        request.setAttribute(WEB_ERRORS, wc.getErrors());
        String resPath = isAbsolutePath(v.getName()) ? v.getName() : "/" + v.getName();
        File resFile = new File(wc.getApplication().getRealPath(
                wc.getViewBase() + resPath));
        if (!resFile.exists()) {
            log.error("Can't find resource " + resFile);
            render404();
            return;
        }
        // Find the layout if exist
        String targetRes = wc.getViewBase() + resPath;
        if (StringUtils.isNotBlank(v.getLayout())) {
            String layoutResPath = wc.getApplication().getRealPath(
                    wc.getViewBase() + v.getLayout());
            File layoutFile = new File(layoutResPath);
            if (layoutFile.exists()) {
                targetRes = wc.getViewBase() + v.getLayout();
            } else {
                log.warn("Layout " + v.getLayout() + " not exist, raise 404 to client.");
                render404();
                return;
            }
            request.setAttribute(LAYOUT_CHILDREN, wc.getViewBase() + resPath);
        }
        if (log.isDebugEnabled()) {
            log.debug("Render  " + v);
        }
        RequestDispatcher rd = wc.getApplication().getRequestDispatcher(
                targetRes);
        try {
            rd.include(request, response);
        } catch (Exception e) {
            log.error(e);
            throw new com.github.foxty.topaz.controller.ControllerException(e);
        }
        wc.clearFlash();
    }

    private void renderJson(Object object) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("application/json");
        String json = JSON.toJSONString(object);
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void renderXml(Object data) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("application/xml");
        try {
            //ToDO: convert data to xml
            response.getWriter().write("");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void renderText(String text) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("text/plain");
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void redirect(String resourcePath) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        try {
            response.sendRedirect(resourcePath);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }
}
