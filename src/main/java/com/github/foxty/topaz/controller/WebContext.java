/**
 * Core component of Topaz framework, WebContext represent each reqeust resources and it is not thread safe.
 * 	- You can access request, response, session stuff here
 *  - You can get current controller name, current method and current Id.
 * 
 * @author foxty 
 */
package com.github.foxty.topaz.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.foxty.topaz.common.DataChecker;
import org.apache.commons.lang.StringUtils;

public class WebContext {

	static public enum Accept {
		JSON, XML, HTML, PLAIN, JSONP;
	}

	public final static String FLASH = "flash";

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletContext application;
	private Map<String, String> errors = new HashMap<String, String>();

	private String contextPath;
	private String viewBase;
	private boolean xssFilterOn = true;

	private static ThreadLocal<WebContext> local = new ThreadLocal<WebContext>();

	public static WebContext get() {
		return local.get();
	}

	public static WebContext create(HttpServletRequest req, HttpServletResponse resp, String viewBase) {
		WebContext ctx = new WebContext(req, resp, viewBase);
		local.set(ctx);
		return ctx;
	}

	private WebContext(HttpServletRequest req, HttpServletResponse resp, String viewBase) {

		this.request = req;
		this.response = resp;
		this.session = req.getSession();
		this.application = this.session.getServletContext();
		this.viewBase = StringUtils.isBlank(viewBase) ? "/view/" : (viewBase.endsWith("/") ? viewBase : viewBase + "/");
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

	public String getViewBase() {
		return viewBase;
	}

	public String getContextPath() {
		return contextPath;
	}

	public boolean isGet() {
		return this.request.getMethod().equalsIgnoreCase("GET");
	}

	public boolean isPost() {
		return this.request.getMethod().equalsIgnoreCase("POST");
	}

	public boolean isPUT() {
		return this.request.getMethod().equalsIgnoreCase("PUT");
	}

	public boolean isHEAD() {
		return this.request.getMethod().equalsIgnoreCase("HEAD");
	}

	public boolean isDELETE() {
		return this.request.getMethod().equalsIgnoreCase("DELETE");
	}

	public String header(String key) {
		return request.getHeader(key);
	}

	public void header(String k, String v) {
		response.setHeader(k, v);
	}

	/**
	 * Get parameters in request.
	 * 
	 * @param key
	 *            key of the parameter
	 * @return value for the key
	 */
	public String param(String key) {
		String p = request.getParameter(key);
		if (xssFilterOn) {
			p = DataChecker.filterHTML(p);
		}
		return p;
	}

	/**
	 * Get attribute in request.
	 * 
	 * @param key
	 *            key of the attribute
	 * @param <T>
	 *            type of the attribute
	 * @return value for the key
	 */
	@SuppressWarnings("unchecked")
	public <T> T attr(String key) {
		Object attr = this.request.getAttribute(key);
		if (attr instanceof String && xssFilterOn) {
			attr = DataChecker.filterHTML((String) attr);
		}
		return (T) attr;
	}

	/**
	 * Set attributes to request attributes.
	 * 
	 * @param key
	 *            key of the attribute
	 * @param value
	 *            value of the attribute
	 */
	public void attr(String key, Object value) {
		this.request.setAttribute(key, value);
	}

	/**
	 * Get attribute value from session
	 * 
	 * @param key
	 *            key of the session attribute
	 * @param <T>
	 *            type of the session attribute
	 * @return value for the key
	 */
	@SuppressWarnings("unchecked")
	public <T> T session(String key) {
		return (T) this.session.getAttribute(key);
	}

	/**
	 * Set object to current session
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	public void session(String key, Object value) {
		this.session.setAttribute(key, value);
	}

	/**
	 * Get cookie object.
	 * 
	 * @param name
	 *            name of the cookie
	 * @return value of cookies[name]
	 */
	public String cookie(String name) {
		Cookie cookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(name)) {
					cookie = c;
				}
			}
		}
		return cookie != null ? cookie.getValue() : null;
	}

	/**
	 * Add cookie to response.
	 * 
	 * @param name
	 *            cookie's name
	 * @param value
	 *            cookie's value
	 * @param path
	 *            cookie's path
	 * @param maxAge
	 *            cookie's max age, -1 means forever
	 * @param httpOnly
	 *            flag of httpOnly
	 */
	public void cookie(String name, String value, String path, int maxAge, boolean httpOnly) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
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

	public boolean isAcceptJson() {
		return getAccept().contains(Accept.JSON);
	}

	public boolean isAcceptXml() {
		return getAccept().contains(Accept.XML);
	}

	public boolean isAcceptHtml() {
		return getAccept().contains(Accept.HTML);
	}

	public boolean isAcceptPlain() {
		return getAccept().contains(Accept.PLAIN);
	}

	public boolean isAJAX() {
		return StringUtils.equals("XMLHttpRequest", header("X-Requested-With"));
	}

	public List<Accept> getAccept() {
		List<Accept> accs = new LinkedList<>();
		String reqAccept = this.request.getHeader("Accept");
		if (null != reqAccept) {
			if (reqAccept.contains("text/html"))
				accs.add(Accept.HTML);
			if (reqAccept.contains("text/plain"))
				accs.add(Accept.PLAIN);
			if (reqAccept.contains("application/json"))
				accs.add(Accept.JSON);
			if (reqAccept.contains("application/xml"))
				accs.add(Accept.XML);
		}
		return accs;
	}

	public void addError(String key, String msg) {
		WebContext.get().getErrors().put(key, msg);
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	/**
	 * 检查是否所有数据均通过验证
	 *
	 * @return boolean
	 */
	public boolean isInputValid() {
		return getErrors().isEmpty();
	}

}
