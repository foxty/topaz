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
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.topaz.common.DataChecker;

public class WebContext {

	public final static String ACCEPT_JSON = "application/json";
	public final static String ACCEPT_XML = "application/xml";

	public final static String FLASH = "flash";

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletContext application;
	private Map<String, String> errors = new HashMap<String, String>();

	private String contextPath;
	private String moduleName;
	private String controllerName = "root";
	private String methodName = "index";
	private String viewBase;
	private boolean xssFilterOn = true;

	private static ThreadLocal<WebContext> local = new ThreadLocal<WebContext>();

	public static WebContext get() {
		return local.get();
	}

	public static WebContext create(HttpServletRequest req,
			HttpServletResponse resp, String viewBase) {
		WebContext ctx = new WebContext(req, resp, viewBase);
		local.set(ctx);
		return ctx;
	}

	private WebContext(HttpServletRequest req, HttpServletResponse resp,
			String viewBase) {

		this.request = req;
		this.response = resp;
		this.session = req.getSession();
		this.application = this.session.getServletContext();
		this.viewBase = StringUtils.isBlank(viewBase) ? "/view/" : (viewBase
				.endsWith("/") ? viewBase : viewBase + "/");
		this.contextPath = request.getContextPath();
	}

	public void xssFilterOn() {
		xssFilterOn = true;
	}

	public void xssFilterOff() {
		xssFilterOn = false;
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String name) {
		moduleName = name;
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String cName) {
		this.controllerName = cName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String mName) {
		this.methodName = mName;
	}

	public String getRequestResource() {
		return moduleName + "/" + controllerName + "/" + methodName;
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
		String p = request.getParameter(key);
		if (xssFilterOn) {
			p = DataChecker.filterHTML(p);
		}
		return p;
	}

	/**
	 * 获取当前request的属性
	 * 
	 * @param key
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	public <T> T attribute(String key) {
		Object attr = this.request.getAttribute(key);
		if (attr instanceof String && xssFilterOn) {
			attr = DataChecker.filterHTML((String) attr);
		}
		return (T) attr;
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
	@SuppressWarnings("unchecked")
	public <T> T session(String key) {
		return (T) this.session.getAttribute(key);
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

	public void flash(String key, Object value) {
		ConcurrentHashMap<String, Object> flashMap = session(FLASH);
		if (flashMap == null) {
			flashMap = new ConcurrentHashMap<String, Object>();
			session(FLASH, flashMap);
		}
		flashMap.putIfAbsent(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T flash(String key) {
		ConcurrentHashMap<String, Object> flashMap = session(FLASH);
		if (flashMap == null) {
			flashMap = new ConcurrentHashMap<String, Object>();
			session(FLASH, flashMap);
		}
		return (T) flashMap.get(key);
	}

	public void clearFlash() {
		ConcurrentHashMap<String, Object> flashMap = session(FLASH);
		if (flashMap != null)
			flashMap.clear();
	}

	public boolean isAcceptJSON() {
		String reqAccept = this.request.getHeader("Accept");
		return reqAccept.contains(ACCEPT_JSON);
	}

	public boolean isAcceptXML() {
		String reqAccept = this.request.getHeader("Accept");
		return reqAccept.contains(ACCEPT_XML);
	}

}
