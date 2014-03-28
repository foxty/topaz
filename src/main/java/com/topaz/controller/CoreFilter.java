/**
 * 作者 Foxty 
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.topaz.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;
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

import com.topaz.common.Config;
import com.topaz.controller.interceptor.FinalInterceptor;
import com.topaz.controller.interceptor.IInterceptor;
import com.topaz.controller.interceptor.InterceptorChain;
import com.topaz.controller.interceptor.Interceptors;

/**
 * @author Isaac Tian
 */
public class CoreFilter implements Filter {
	private String controllerBase = "/topaz/controller/";
	private String viewBase = "/WEB-INF/view";
	private String cfgFilePath;
	private boolean xssFilterOn = true;

	private ConcurrentHashMap<String, Controller> controllersCache = new ConcurrentHashMap<String, Controller>();
	private ConcurrentHashMap<Class<IInterceptor>, IInterceptor> interceptorsCache = new ConcurrentHashMap<Class<IInterceptor>, IInterceptor>();
	private ModuleNode rootNode = new ModuleNode("", null);

	private static Log log = LogFactory.getLog(CoreFilter.class);

	/*
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String cBase = config.getInitParameter("controllerBase");
		String vBase = config.getInitParameter("viewBase");
		String cFile = config.getInitParameter("configFile");
		String xssFilterFlag = config.getInitParameter("xssFilterOn");
		if (StringUtils.isNotBlank(cBase)) {
			controllerBase = cBase;
			if (!controllerBase.endsWith("/")) {
				controllerBase += "/";
			}
		}
		if (StringUtils.isNotBlank(vBase)) {
			viewBase = vBase;
		}
		if (StringUtils.isNotBlank(cFile)) {
			cfgFilePath = cFile;
		}
		if (StringUtils.isNotBlank(xssFilterFlag)) {
			xssFilterOn = Boolean.valueOf(xssFilterFlag);
		}

		log.info("Start load Config from file " + cfgFilePath);
		Config.init(new File(cfgFilePath));
		config.getServletContext().setAttribute("contextPath",
				config.getServletContext().getContextPath());

		log.info("Start load controllers");
		URL contUrl = Config.class.getResource(controllerBase);
		if (contUrl == null) {
			log.error("Can't find controller resource under " + controllerBase);
			throw new ControllerException(
					"Can't find controller resource under " + controllerBase);
		} else {
			File cp = new File(contUrl.getFile());
			if (cp != null && cp.isDirectory()) {
				feedNode(rootNode, cp);
			}

			log.info("CoreFilter has inited: controllerBase=" + controllerBase
					+ ", viewBase=" + viewBase);
		}
	}

	private void feedNode(ModuleNode node, File folder) {
		for (File f : folder.listFiles()) {
			String pName = f.getName();
			if (f.isFile()) {
				if (pName.endsWith("Controller.class")) {
					String tmpPath = controllerBase + node.fullPath() + "/"
							+ pName;
					String classPath = tmpPath.replace(".class", "")
							.replaceAll("[/\\\\]+", ".").substring(1);

					ControllerNode cn = new ControllerNode(pName.replace(
							"Controller.class", "").toLowerCase(),
							initController(classPath), node);
					node.addControllerNode(cn);
				}
			} else {
				ModuleNode n = new ModuleNode(pName, node);
				node.addModuleNode(n);
				feedNode(n, f);
			}
		}
	}

	private Controller initController(String fullClassPath) {
		Controller c = null;
		try {
			Class<?> contClazz = Class.forName(fullClassPath);
			c = (Controller) contClazz.newInstance();
			controllersCache.putIfAbsent(fullClassPath, c);
			log.info("New controller " + fullClassPath);

			while (contClazz != Controller.class) {
				for (Class<IInterceptor> clazz : getInterceptors(contClazz)) {
					try {
						interceptorsCache.putIfAbsent(clazz,
								clazz.newInstance());
					} catch (InstantiationException e) {
						log.error("Initialize " + clazz + " failed!");
					} catch (IllegalAccessException e) {
						log.error(e.getMessage(), e);
					}
				}
				contClazz = contClazz.getSuperclass();
			}
		} catch (ClassNotFoundException cnfe) {
			log.error("Controller " + fullClassPath + " not fond! ");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return c;
	}

	private Class[] getInterceptors(Class<?> clazz) {
		Interceptors interAnnotation = clazz.getAnnotation(Interceptors.class);
		return (interAnnotation != null) ? interAnnotation.interceptors()
				: new Class[] {};
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString(), e);
		}
		response.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "")
				.toLowerCase();
		log.debug("Current uri = " + uri);

		// if its REST style
		if (uri.indexOf('.') <= 0 && uri.length() > 1) {
			WebContext ctx = WebContext.create(req, resp, viewBase);
			if (!xssFilterOn) {
				ctx.xssFilterOff();
			}

			String[] uriArr = uri.split("[/;]");
			// First path is root, so we start search from 1
			ControllerNode cn = findControllerNode(rootNode, 1, uriArr);
			Controller c = null;
			if (cn != null) {
				ctx.setModuleName(cn.getParent().fullPath());
				ctx.setControllerName(cn.getNodeName());
				if (cn.getPos() + 1 < uriArr.length) {
					ctx.setMethodName(uriArr[cn.getPos() + 1]);
				}
				c = cn.getController();
			} else {
				cn = rootNode.findControllerNode("root");
				c = (cn != null ? cn.getController() : null);
				ctx.setModuleName(rootNode.fullPath());
				ctx.setControllerName("root");
				// First path is root, so we use 1 as the method name
				ctx.setMethodName(uriArr[1]);
			}
			request.setAttribute("requestResource", ctx.getRequestResource());

			execute(c);
		} else {
			chain.doFilter(request, response);
		}
	}

	private ControllerNode findControllerNode(ModuleNode node, int pos,
			String[] pNames) {
		if (pos >= pNames.length)
			return null;
		String pName = pNames[pos];
		if (node.hasController(pName)) {
			return node.findControllerNode(pName);
		}
		if (node.hasNode(pName)) {
			return findControllerNode(node.findNode(pName), ++pos, pNames);
		}
		return null;
	}

	private void execute(Controller c) {
		WebContext ctx = WebContext.get();

		// Send 404 error to client if both current controller doesn't exist
		if (c == null) {
			try {
				ctx.getResponse().sendError(404);
			} catch (IOException e1) {
			}
			return;
		}

		// get all interceptors from annotation
		LinkedList<IInterceptor> interceptors = new LinkedList<IInterceptor>();
		Class controllerClazz = c.getClass();
		while (controllerClazz != Controller.class) {
			for (Class<IInterceptor> clazz : getInterceptors(controllerClazz)) {
				interceptors.addFirst(interceptorsCache.get(clazz));
			}
			controllerClazz = controllerClazz.getSuperclass();
		}
		if (log.isDebugEnabled()) {
			log.debug(ctx.getControllerName() + " got " + interceptors.size()
					+ " interceptors, as " + interceptors);
		}

		// interceptors chain
		FinalInterceptor fin = new FinalInterceptor(c);
		interceptors.add(fin);
		
		InterceptorChain chain = new InterceptorChain(interceptors);
		chain.proceed();
	}

	public void destroy() {
		log.info("CoreFilter has been destroyed!");
	}
}