/**
 * 作者 Foxty
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.github.foxty.topaz.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.common.Config;

/**
 * @author Isaac Tian
 */
public class CoreFilter implements Filter {

	private static Log log = LogFactory.getLog(CoreFilter.class);
	public static final String DEFAULT_CONT_PACKAGE = "com.github.foxty.topaz.controller";
	public static final String DEFAULT_VIEW_BASE = "/WEB-INF/view";

	private String contPackageName = DEFAULT_CONT_PACKAGE;
	private String viewBase = DEFAULT_VIEW_BASE;
	private boolean xssFilterOn = true;

	private ConcurrentHashMap<String, Controller> controllerUriMap = new ConcurrentHashMap<>();

	/*
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String cBase = config.getInitParameter("controllerPackage");
		String vBase = config.getInitParameter("viewBase");
		String cFile = config.getInitParameter("configFile");
		String xssFilterFlag = config.getInitParameter("xssFilterOn");
		if (StringUtils.isNotBlank(cBase)) {
			contPackageName = cBase;
		}
		if (StringUtils.isNotBlank(vBase)) {
			viewBase = vBase;
		}		
		if (StringUtils.isNotBlank(xssFilterFlag)) {
			xssFilterOn = Boolean.valueOf(xssFilterFlag);
		}

		log.info("Start load Config from file " + cFile);
		Config.init(new File(cFile));

		if (StringUtils.isBlank(contPackageName)) {
			log.error("controllerPackage not defined in web.xml");
			throw new ControllerException("controllerPackage not defined in web.xml");
		}
		log.info("Start scan controllers");
		scanControllers(contPackageName);
		log.info("Topaz initialized: contPackageName=" + contPackageName + ", viewBase=" + viewBase);
	}

	/**
	 * Scan controllers in the package list and do the initialization.
	 *
	 * @param contPackageName
	 */
	private void scanControllers(String contPackageName) {
		String packageDirName = contPackageName.replace('.', '/');
		URL url = Thread.currentThread().getContextClassLoader().getResource(packageDirName);
		File pFile = new File(url.getFile());
		if (pFile.exists()) {
			scanControllersInFolder(contPackageName, pFile);
		} else {
			log.warn("Package " + contPackageName + " not exist.");
		}

	}

	private void scanControllersInFolder(String contPackageName, File folder) {
		for (File f : folder.listFiles()) {
			String pName = f.getName();
			if (f.isFile()) {
				if (pName.endsWith(".class")) {
					String clsPath = contPackageName + "." + pName.replace(".class", "");
					Class<?> cls = null;
					try {
						cls = Thread.currentThread().getContextClassLoader().loadClass(clsPath);
					} catch (ClassNotFoundException e) {
						log.error("Can not find class " + clsPath, e);
						continue;
					}

					if (cls.isAnnotationPresent(_Controller.class)) {
						log.info("Init controller " + cls.getName());
						initControllerAndEndpoints(cls);
					}
				}
			} else {
				scanControllersInFolder(contPackageName + "." + f.getName(), f);
			}
		}
	}

	private void initControllerAndEndpoints(Class<?> contClazz) {

		// Instant the controller object.
		Controller controller = null;
		try {
			controller = new Controller(contClazz.newInstance());
			Controller oldValue = controllerUriMap.putIfAbsent(controller.getUri(), controller);
			if (null != oldValue) {
				log.error("Conflict uri mapping between controller " + controller + " and " + oldValue);
			}
		} catch (Exception e) {
			log.error(e);
			throw new ControllerException(e);
		}
		log.info("Controller " + contClazz.getName() + " created with " + controller.getEndpointCount() + " endpoint.");
	}

	/**
	 * Find the endpoint
	 *
	 * @param request
	 *            HTTP Request
	 * @param response
	 *            HTTP Response
	 * @param chain
	 *            Filter Chain
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString(), e);
		}
		resp.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "").toLowerCase();
		log.debug("Current uri = " + uri);

		// if its REST style
		if (uri.indexOf('.') <= 0) {
			HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod());
			// Search endpoint info by requested URI
			Endpoint endpoint = searchEndpoint(uri, httpMethod);
			if (null != endpoint) {
				WebContext ctx = WebContext.create(req, resp, viewBase);
				if (!xssFilterOn) {
					ctx.xssFilterOff();
				}
				endpoint.execute();
				return;
			} else {
				log.warn("Can't find endpoint " + httpMethod + " on" + uri);
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}

		// Execute the rest Filter/Servlet
		chain.doFilter(request, response);
	}

	/**
	 *
	 * @param uri
	 *            Request URI
	 * @param httpMethod
	 *            Request Http Method
	 * @return Endpoint Target Endpoint
	 */
	private Endpoint searchEndpoint(String uri, HttpMethod httpMethod) {
		// Step 1 find the controller
		String[] uriArr = uri.split("[/\\;]");
		String contUri = "/";
		for (String uriPart : uriArr) {
			contUri += uriPart;
			Controller c = controllerUriMap.get(contUri);
			if (c != null) {
				Endpoint ep = c.findEndpoint(uri, httpMethod);
				if (ep == null) {
					continue;
				}
				return ep;
			}
		}
		log.info("Can't mapp uri " + uri + " to any endpoint.");
		return null;
	}

	public void destroy() {
		log.info("CoreFilter has been destroyed!");
	}
}