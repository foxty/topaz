/**
 * 作者 Foxty 
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.topaz.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
	private String controllerBase = "com.topaz.controller.";
	private String viewBase = "/WEB_INF/view";
	private String cfgFilePath = "/WEB-INF/config.properties";
	private ConcurrentHashMap<String, Controller> controllers = new ConcurrentHashMap<String, Controller>();

	private static Log log = LogFactory.getLog(CoreFilter.class);

	/*
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String cBase =  config.getInitParameter("controllerBase");
		String vBase = config.getInitParameter("viewBase");
		String cFile = config.getInitParameter("configFile");
		if(StringUtils.isNotBlank(cBase)) {
			controllerBase = cBase;
		}
		if(StringUtils.isNotBlank(vBase)) {
			viewBase = vBase;
		}
		if(StringUtils.isNotBlank(cFile)) {
			cfgFilePath = cFile;
		}
		Config.init(new File(cfgFilePath));

		config.getServletContext().setAttribute("contextPath",
				config.getServletContext().getContextPath());

		log.info("CoreFilter has inited: controllerBase=" + controllerBase
				+ ", viewBase=" + viewBase);
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

		// 如果是REST风格的映射
		if (uri.indexOf('.') <= 0 && uri.length() > 1) {
			createContext(uri, req, resp);
			execute();
		} else {
			chain.doFilter(request, response);
		}
	}

	private void createContext(String uri, HttpServletRequest req,
			HttpServletResponse resp) {
		WebContext.create(req, resp, controllerBase, viewBase);
	}

	private void execute() {
		WebContext ctx = WebContext.get();
		String method = ctx.getMethodName();

		String controllerClassUri = ctx.getControllerClassUri();
		log.debug("Process " + controllerClassUri + "." + method);
		Controller c = getController(controllerClassUri);
		// get all interceptors from annotation
		List<IInterceptor> interceptors = new ArrayList<IInterceptor>();
		Class controllerClazz = c.getClass();
		while (controllerClazz != Controller.class) {
			for (Class<IInterceptor> clazz : getInterceptors(controllerClazz)) {
				try {
					interceptors.add(clazz.newInstance());
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
					throw new ControllerException(e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
					throw new ControllerException(e);
				}
			}
			controllerClazz = controllerClazz.getSuperclass();
		}
		if (log.isDebugEnabled()) {
			log
					.debug(ctx.getControllerClassUri() + " got "
							+ interceptors.size() + " interceptors, as "
							+ interceptors);
		}

		FinalInterceptor fin = new FinalInterceptor(c);
		interceptors.add(fin);

		// start intercepros chain
		InterceptorChain chain = new InterceptorChain(interceptors);
		chain.proceed(ctx);
	}

	private Class[] getInterceptors(Class<?> clazz) {
		Interceptors interAnnotation = clazz.getAnnotation(Interceptors.class);
		return (interAnnotation != null) ? interAnnotation.interceptors()
				: new Class[] {};
	}

	private Controller getController(String fullClassPath) {
		Controller c = controllers.get(fullClassPath);
		if (c == null) {
			try {
				Class<?> clazz = Class.forName(fullClassPath);
				c = (Controller) clazz.newInstance();
				controllers.putIfAbsent(fullClassPath, c);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ControllerException("Error while create controller "
						+ fullClassPath, e);
			}
			log.info("New controller " + fullClassPath);
		}
		return c;
	}

	public void destroy() {
		log.info("CoreFilter has been destroyed!");
	}
}
