package com.github.foxty.topaz.controller.interceptor;

import com.alibaba.fastjson.JSON;
import com.github.foxty.topaz.common.TopazException;
import com.github.foxty.topaz.controller.ControllerException;
import com.github.foxty.topaz.controller.HttpMethod;
import com.github.foxty.topaz.controller.View;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.controller.anno.Allow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Final interceptor was last in the chain to handle the resource request.
 *
 * @author itian
 */
public class FinalInterceptor implements IInterceptor {

    private static String WEB_ERRORS = "errors";
    private static String LAYOUT_CHILDREN = "children";
    private static Log log = LogFactory.getLog(FinalInterceptor.class);
    private Object controller;
    private Method targetMethod;

    public FinalInterceptor(Object controller, Method targetMethod) {
        this.controller = controller;
        this.targetMethod = targetMethod;
    }

    public void intercept(InterceptorChain chain) {
        WebContext wc = WebContext.get();

        if (log.isDebugEnabled()) {
            log.debug("Execute method " + controller.getClass() + "." + targetMethod.getName());
        }
        Object result = invokeTargetMethod();
    }

    private Object invokeTargetMethod() {
        try {
            return targetMethod.invoke(controller);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof TopazException) {
                throw (TopazException) e.getTargetException();
            } else {
                throw new ControllerException(e);
            }
        } catch (Exception e) {
            throw new ControllerException(e);
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
            String layout = wc.getEndpoint().getLayout();
            if(StringUtils.isBlank(v.getLayout())) {
                v.setLayout(layout);
            }
            renderView(v);
        } else {

        }
    }

    final private boolean isAbsolutePath(String resPath) {
        return resPath != null
                && (resPath.startsWith("/") || resPath.startsWith("\\"));
    }

    protected void renderView(View v) {
        WebContext wc = WebContext.get();
        HttpServletRequest request = wc.getRequest();
        HttpServletResponse response = wc.getResponse();

        request.setAttribute(WEB_ERRORS, wc.getErrors());
        String resPath = isAbsolutePath(v.getName()) ? v.getName() : "/" + v.getName();
        File resFile = new File(wc.getApplication().getRealPath(
                wc.getViewBase() + resPath));
        if (!resFile.exists()) {
            log.error("Can't find resource " + resFile);
            try {
                response.sendError(404);
            } catch (IOException e1) {
            }
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
                targetRes = wc.getViewBase() + v.getLayout();
                log.warn("Layout " + v.getLayout()
                        + " not exist, now using default layout" + v.getLayout());
            }
            request.setAttribute(LAYOUT_CHILDREN, wc.getViewBase() + resPath);
        }
        if (log.isDebugEnabled())
            log.debug("Render  " + v);
        RequestDispatcher rd = wc.getApplication().getRequestDispatcher(
                targetRes);
        try {
            rd.include(request, response);
        } catch (Exception e) {
            log.error(e);
            throw new ControllerException(e);
        }
        wc.clearFlash();
    }

    protected void renderJSON(Object object) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("application/json");
        String json = JSON.toJSONString(object);
        try {
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    protected void renderText(String text) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("text/plain");
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    protected void redirect(String resourcePath) {
        WebContext ctx = WebContext.get();
        HttpServletResponse response = ctx.getResponse();
        try {
            response.sendRedirect(resourcePath);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }
}
