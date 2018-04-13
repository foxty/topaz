/**
 * 作者 Foxty
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.github.foxty.topaz.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

import com.github.foxty.topaz.annotation.Launcher;
import com.github.foxty.topaz.annotation.Controller;
import com.github.foxty.topaz.common.Config;

/**
 * @author Isaac Tian
 */
public class CoreFilter implements Filter {

	private static Log log = LogFactory.getLog(CoreFilter.class);
	public static final String DEFAULT_CONT_PACKAGE = ".";
	public static final String DEFAULT_VIEW_BASE = "/WEB-INF/view";

	private String scanPath = DEFAULT_CONT_PACKAGE;
	private String viewBase = DEFAULT_VIEW_BASE;
	private boolean xssFilterOn = true;

	private ConcurrentHashMap<String, com.github.foxty.topaz.controller.Controller> controllerUriMap = new ConcurrentHashMap<>();
	private List<Runnable> launchers = new LinkedList<>();
	private ExecutorService launcherExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "Launcher - " + System.currentTimeMillis());
			if (t.isDaemon())
				t.setDaemon(false);
			return t;
		}
	});

	/*
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String cBase = config.getInitParameter("scanPath");
		String vBase = config.getInitParameter("viewBase");
		String cFile = config.getInitParameter("configFile");
		String xssFilterFlag = config.getInitParameter("xssFilterOn");
		if (StringUtils.isNotBlank(cBase)) {
			scanPath = cBase;
		}
		if (StringUtils.isNotBlank(vBase)) {
			viewBase = vBase;
		}
		if (StringUtils.isNotBlank(xssFilterFlag)) {
			xssFilterOn = Boolean.valueOf(xssFilterFlag);
		}

		if (StringUtils.isNotBlank(cFile)) {
			Config.init(new File(cFile));
		}

		log.info("[Resource Scan]Start ...");
		scanResources(scanPath);
		log.info("[Resource Scan]End: found " + controllerUriMap.size() + " controllers, " + launchers.size()
				+ " launchers!");

		log.info("[Launchers]Start...");
		for (Runnable r : launchers) {
			launcherExecutor.execute(r);
		}
		log.info("[Launchers]End...");
	}

	/**
	 * Scan controllers in the package list and do the initialization.
	 *
	 * @param scanPath
	 */
	private void scanResources(String scanPath) {
		String packageDirName = scanPath.equals(".") ? scanPath : scanPath.replace('.', '/');
		URL url = Thread.currentThread().getContextClassLoader().getResource(packageDirName);
		if (url == null) {
			ControllerException ce = new ControllerException("Invalid scan path " + scanPath);
			log.error(ce);
			throw ce;
		}
		File pFile = new File(url.getFile());
		if (pFile.exists()) {
			scanResourcesInFolder(scanPath, pFile);
		} else {
			log.warn("Package " + scanPath + " not exist.");
		}

	}

	private void scanResourcesInFolder(String pkgPath, File folder) {
		for (File f : folder.listFiles()) {
			String pName = f.getName();
			if (f.isFile()) {
				if (pName.endsWith(".class")) {
					String clsPath = (pkgPath.equals(".") ? "" : pkgPath + ".") + pName.replace(".class", "");
					Class<?> cls = null;
					try {
						cls = Thread.currentThread().getContextClassLoader().loadClass(clsPath);
					} catch (ClassNotFoundException e) {
						log.error("Cannot find class " + clsPath, e);
						continue;
					}

					if (cls.isAnnotationPresent(Controller.class)) {
						log.info("Found controller " + cls.getName());
						initController(cls);
					}

					if (cls.isAnnotationPresent(Launcher.class)) {
						log.info("Found laucher " + cls.getName());
						initLauncher(cls);
					}
				}
			} else {
				scanResourcesInFolder((pkgPath.equals(".") ? "" : pkgPath + ".") + f.getName(), f);
			}
		}
	}

	private void initController(Class<?> contClazz) {
		// Instant the controller object.
		com.github.foxty.topaz.controller.Controller controller = null;
		try {
			controller = new com.github.foxty.topaz.controller.Controller(contClazz.newInstance());
			com.github.foxty.topaz.controller.Controller oldValue = controllerUriMap.putIfAbsent(controller.getUri(), controller);
			if (null != oldValue) {
				log.error("Conflict uri mapping between controller " + controller + " and " + oldValue);
			}
		} catch (Exception e) {
			log.error(e);
			throw new ControllerException(e);
		}
		log.info("Controller " + contClazz.getName() + " created with " + controller.getEndpointCount() + " endpoint.");
	}

	private void initLauncher(Class<?> cls) {
		if (Runnable.class.isAssignableFrom(cls)) {
			Runnable launcher = null;
			try {
				launcher = (Runnable) cls.newInstance();
				launchers.add(launcher);
			} catch (Exception e) {
				log.error(e);
				throw new ControllerException(e);
			}
		} else {
			log.error("Launcher " + cls.getName() + " is not a Runnable.");
		}
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
		String[] uriArr = uri.substring(1).split("[/\\;]");
		String contUri = "";
		for (String uriPart : uriArr) {
			contUri += "/" + uriPart;
			com.github.foxty.topaz.controller.Controller c = controllerUriMap.get(contUri);
			if (c != null) {
				Endpoint ep = c.findEndpoint(uri, httpMethod);
				if (ep == null) {
					continue;
				}
				return ep;
			}
		}
		return null;
	}

	public void destroy() {
		launcherExecutor.shutdownNow();
		log.info("CoreFilter has been destroyed!");
	}
}