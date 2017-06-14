/**
 * Core component of Topaz framework, WebContext represent each reqeust resources and it is not thread safe.
 * 	- You can access request, response, session stuff here
 *  - You can get current controller name, current method and current Id.
 * 
 * @author foxty 
 */
package com.github.foxty.topaz.controller;

import java.util.HashMap;
import java.util.Map;
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
		JSON, XML, HTML, JSONP;
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
	private Endpoint endpoint;

	private static ThreadLocal<WebContext> local = new ThreadLocal<WebContext>();

	public static WebContext get() {
		return local.get();
	}

	public static WebContext create(HttpServletRequest req,
			HttpServletResponse resp, String viewBase, Endpoint endpoint) {
		WebContext ctx = new WebContext(req, resp, viewBase, endpoint);
		local.set(ctx);
		return ctx;
	}

	private WebContext(HttpServletRequest req, HttpServletResponse resp,
			String viewBase, Endpoint endpoint) {

		this.request = req;
		this.response = resp;
		this.session = req.getSession();
		this.application = this.session.getServletContext();
		this.viewBase = StringUtils.isBlank(viewBase) ? "/view/" : (viewBase
				.endsWith("/") ? viewBase : viewBase + "/");
		this.contextPath = request.getContextPath();
		this.endpoint = endpoint;
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

	public Endpoint getEndpoint() {
		return endpoint;
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
	 * @param key key of the parameter
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
	 * @param key key of the attribute
	 * @param <T> type of the attribute
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
	 * @param key key of the attribute
	 * @param value value of the attribute
	 */
	public void attr(String key, Object value) {
		this.request.setAttribute(key, value);
	}

	/**
	 * Get attribute value from session
	 * 
	 * @param key key of the session attribute
	 * @param <T> type of the session attribute
	 * @return value for the key
	 */
	@SuppressWarnings("unchecked")
	public <T> T session(String key) {
		return (T) this.session.getAttribute(key);
	}

	/**
	 * Set object to current session
	 * 
	 * @param key key
	 * @param value value
	 */
	public void session(String key, Object value) {
		this.session.setAttribute(key, value);
	}

	/**
	 * Get cookie object.
	 * 
	 * @param name name of the cookie
	 * @return	value of cookies[name]
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
	 * @param name cookie's name
	 * @param value cookie's value
	 * @param path cookie's path
	 * @param maxAge cookie's max age, -1 means forever
	 * @param httpOnly flag of httpOnly
	 */
	public void cookie(String name, String value, String path, int maxAge,
			boolean httpOnly) {
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

	public Accept getAccept() {
		Accept acc = Accept.HTML;
		String reqAccept = this.request.getHeader("Accept");
		if (reqAccept.contains("application/json"))
			acc = Accept.JSON;
		if (reqAccept.contains("application/xml"))
			acc = Accept.XML;
		return acc;
	}

	public boolean isAcceptJSON() {
		return getAccept() == Accept.JSON;
	}

	public boolean isAcceptXML() {
		return getAccept() == Accept.XML;
	}

	public boolean isAJAX() {
		return StringUtils.equals("XMLHttpRequest", header("X-Requested-With"));
	}

}
