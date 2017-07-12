package com.github.foxty.topaz.controller.interceptor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.github.foxty.topaz.common.ControllerException;
import com.github.foxty.topaz.controller.Controller;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.controller.response.Redirect;
import com.github.foxty.topaz.controller.response.View;

/**
 * Final interceptor was last in the chain to handle the resource request.
 *
 * @author itian
 */
final public class FinalInterceptor implements IInterceptor {

	private static String WEB_ERRORS = "errors";
	private static String LAYOUT_CHILDREN = "children";
	private static Log log = LogFactory.getLog(FinalInterceptor.class);
	private Controller controller;
	private Method targetMethod;

	public FinalInterceptor(Controller controller, Method targetMethod) {
		this.controller = controller;
		this.targetMethod = targetMethod;
	}

	final public void intercept(InterceptorChain chain) {
		if (log.isDebugEnabled()) {
			log.debug("Execute method " + controller.getResource().getClass() + "." + targetMethod.getName());
		}
		Object result = invokeTargetMethod();
		dispatchResponse(result);
	}

	private Object invokeTargetMethod() {
		try {
			return targetMethod.invoke(controller.getResource());
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
			String layout = controller.getLayout();
			if (StringUtils.isBlank(v.getLayout())) {
				v.setLayout(layout);
			}
			renderView(v);
		} else if (result instanceof Redirect) {
			redirect(((Redirect) result).getTargetUri());
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
		return resPath != null && (resPath.startsWith("/") || resPath.startsWith("\\"));
	}

	private void renderView(View v) {
		WebContext wc = WebContext.get();
		HttpServletRequest request = wc.getRequest();
		HttpServletResponse response = wc.getResponse();

		request.setAttribute(WEB_ERRORS, wc.getErrors());
		String resPath = isAbsolutePath(v.getName()) ? v.getName() : "/" + v.getName();
		File resFile = new File(wc.getApplication().getRealPath(wc.getViewBase() + resPath));
		if (!resFile.exists()) {
			log.error("Can't find resource " + resFile);
			render404();
			return;
		}
		// Find the layout if exist
		String targetRes = wc.getViewBase() + resPath;
		if (!v.isNoLayout() && StringUtils.isNotBlank(v.getLayout())) {
			String layoutResPath = wc.getApplication().getRealPath(wc.getViewBase() + v.getLayout());
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

		if (v.getResponseData() != null) {
			for (Entry<String, Object> data : v.getResponseData().entrySet()) {
				wc.attr(data.getKey(), data.getValue());
			}
		}
		RequestDispatcher rd = wc.getApplication().getRequestDispatcher(targetRes);
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
			// ToDO: convert data to xml
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
