/**
 * Core component of Topaz framework, WebContext represent each reqeust resources and it is not thread safe.
 * 	- You can access request, response, session stuff here
 *  - You can get current controller name, current method and current Id.
 * 
 * @author foxty 
 */
package com.topaz.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

public class WebContext {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletContext application;
	private Map<String, String> errors = new HashMap<String, String>();

	private String contextPath;
	//private String moduleName;
	private String controllerName;
	private String methodName = "index";
	private String id = "";

	private String controllerBase;
	private String viewBase;

	private static ThreadLocal<WebContext> local = new ThreadLocal<WebContext>();

	public static WebContext get() {
		return local.get();
	}

	public static void create(HttpServletRequest req, HttpServletResponse resp,
			String controllerBase, String viewBase) {
		local.set(new WebContext(req, resp, controllerBase, viewBase));
	}

	private WebContext(HttpServletRequest req, HttpServletResponse resp,
			String controllerBase, String viewBase) {

		this.request = req;
		this.response = resp;
		this.session = req.getSession();
		this.application = this.session.getServletContext();
		this.controllerBase = controllerBase;
		this.viewBase = StringUtils.isBlank(viewBase) ? "/view/" : viewBase;
		this.contextPath = request.getContextPath();

		String uri = req.getRequestURI()
				.replaceFirst(request.getContextPath(), "").toLowerCase();
		uri = uri.substring(1);
		String[] uriArr = uri.split("[/;]");
		switch (uriArr.length) {
		case 4:
		case 3:
			this.id = uriArr[2];
		case 2:
			this.methodName = uri2method(uriArr[1]);
		case 1:
			this.controllerName = uriArr[0];
		}
		request.setAttribute("requestResource", controllerName + "." + methodName);
	}

	/**
	 * Change partion of URI to method name replace "-" to "_"
	 * 
	 * @param uri
	 * @return
	 */
	private String uri2method(String uri) {
		return uri.replaceAll("\\-", "_");
	}

	public final ServletContext getApplication() {
		return application;
	}

	public final HttpServletRequest getRequest() {
		return request;
	}

	public final HttpServletResponse getResponse() {
		return response;
	}

	public final HttpSession getSession() {
		return session;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public String getViewBase() {
		return viewBase;
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getControllerName() {
		return controllerName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getId() {
		return id;
	}

	public String getControllerClassUri() {
		return controllerBase + StringUtils.capitalize(controllerName)
				+ "Controller";
	}

	/**
	 * 判断当前请求是否是GET
	 * 
	 * @return boolean
	 */
	public boolean isGet() {
		return this.request.getMethod().equalsIgnoreCase("GET");
	}

	/**
	 * 判断当前请求是否是POST
	 * 
	 * @return boolean
	 */
	public boolean isPost() {
		return this.request.getMethod().equalsIgnoreCase("POST");
	}

	/**
	 * 获取request的参数
	 * 
	 * @param key
	 * @return String
	 */
	public String parameter(String key) {
		return this.request.getParameter(key);
	}

	/**
	 * 获取当前request的属性
	 * 
	 * @param key
	 * @return Object
	 */
	public Object attribute(String key) {
		return this.request.getAttribute(key);
	}

	/**
	 * 设置对象至当前request中
	 * 
	 * @param key
	 * @param value
	 */
	public void attribute(String key, Object value) {
		this.request.setAttribute(key, value);
	}

	/**
	 * 获取session中对象
	 * 
	 * @param key
	 * @return Object
	 */
	public Object session(String key) {
		return this.session.getAttribute(key);
	}

	/**
	 * 设置对象至当前session
	 * 
	 * @param key
	 * @param value
	 */
	public void session(String key, Object value) {
		this.session.setAttribute(key, value);
	}

}
