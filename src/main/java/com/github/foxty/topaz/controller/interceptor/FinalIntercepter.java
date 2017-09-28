package com.github.foxty.topaz.controller.interceptor;

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
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.controller.Controller;
import com.github.foxty.topaz.controller.ControllerException;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.controller.response.Json;
import com.github.foxty.topaz.controller.response.Redirect;
import com.github.foxty.topaz.controller.response.View;

/**
 * Final interceptor was last in the chain to handle the resource request.
 *
 * @author itian
 */
final public class FinalIntercepter implements IIntercepter {

	private static String WEB_ERRORS = "errors";
	private static String LAYOUT_CHILDREN = "children";
	private static Log log = LogFactory.getLog(FinalIntercepter.class);
	private Controller controller;
	private Method targetMethod;

	public FinalIntercepter(Controller controller, Method targetMethod) {
		this.controller = controller;
		this.targetMethod = targetMethod;
	}

	final public void intercept(IntercepterChain chain) {
		if (log.isDebugEnabled()) {
			log.debug("Execute method " + controller.getResource().getClass() + "." + targetMethod.getName());
		}
		Object result = invokeTargetMethod();
		dispatchResponse(result);
	}

	private Object invokeTargetMethod() {
		try {
			return targetMethod.invoke(controller.getResource());
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ControllerException(e);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new ControllerException(e);
			}
		} catch (RuntimeException re) {
			throw re;
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
			renderView(controller.getUri(), v);
		} else if (result instanceof Redirect) {
			redirect(((Redirect) result).getTargetUri());
		} else if (wc.isAcceptJson() || result instanceof Json) {
			renderJson(result);
		} else if (wc.isAcceptXml()) {
			renderXml(result);
		} else if (wc.isAcceptPlain()) {
			renderText(result.toString());
		} else if(result != null) {
			log.warn(wc.getRequest().getMethod() + " on " + wc.getRequest().getRequestURI() + " is not acceptable.");
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

	private void renderView(String baseUri, View v) {
		WebContext wc = WebContext.get();
		HttpServletRequest request = wc.getRequest();
		HttpServletResponse response = wc.getResponse();

		request.setAttribute(WEB_ERRORS, wc.getErrors());
		// Add the controller uri as the folder name if resource name not start
		// with /
		String resPath = TopazUtil.cleanUri(isAbsolutePath(v.getName()) ? wc.getViewBase() + v.getName()
				: wc.getViewBase() + "/" + baseUri + "/" + v.getName());
		String resRealPath = wc.getApplication().getRealPath(resPath);
		if (null == resRealPath) {
			log.error("Can't find resource " + resPath);
			render404();
			return;
		}

		// Find the layout if exist
		String targetRes = resPath;
		if (!v.isNoLayout() && StringUtils.isNotBlank(v.getLayout())) {
			targetRes = TopazUtil.cleanUri(wc.getViewBase() + v.getLayout());
			String layoutRealPath = wc.getApplication().getRealPath(targetRes);
			if (null == layoutRealPath) {
				log.warn("Layout " + layoutRealPath + " not exist, raise 404 to client.");
				render404();
				return;
			}
			request.setAttribute(LAYOUT_CHILDREN, resPath);
		}
		if (log.isDebugEnabled()) {
			log.debug("Render  " + v);
		}

		if (v.getData() != null) {
			for (Entry<String, Object> data : v.getData().entrySet()) {
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
		Object data = object;
		if (object instanceof Json) {
			Json re = (Json) object;
			response.setStatus(re.getStatusCode());
			data = re.getData();
		}
		String json = JSON.toJSONString(data);
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
