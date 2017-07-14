package com.github.foxty.topaz.controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.annotation._Endpoint;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.controller.interceptor.IIntercepter;

/**
 * Wrapper of controller
 * 
 * @author itian
 *
 */
public class Controller {

	private static Log log = LogFactory.getLog(Controller.class);
	private Class<?> clazz;
	private _Controller anno;
	private String uri;
	private Object resource;
	private List<IIntercepter> intercepters = new LinkedList<>();
	private Map<String, Endpoints> endpointsMap = new HashMap<>();
	private int endpointCount;

	private String description;

	public Controller(Object resource) {
		this.clazz = resource.getClass();
		this.anno = clazz.getAnnotation(_Controller.class);
		this.uri = TopazUtil.cleanUri(anno.uri());
		this.resource = resource;

		init();

		description = String.format("[Controller: uri=%s, intercepterCount=%d, endpointCount=%d]", uri,
				intercepters.size(), endpointCount);
	}

	private void init() {
		// Init all intercepters
		Class<?> tmpClazz = clazz;
		while (tmpClazz != Object.class) {
			Class<? extends IIntercepter>[] interceptorClazzs = anno.interceptors();
			if (interceptorClazzs != null) {
				for (Class<? extends IIntercepter> interceptorClazz : interceptorClazzs) {
					try {
						IIntercepter inter = interceptorClazz.newInstance();
						intercepters.add(inter);
					} catch (InstantiationException e) {
						log.error("Initialize " + interceptorClazz + " failed!");
					} catch (IllegalAccessException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			tmpClazz = tmpClazz.getSuperclass();
		}

		// Create Endpoints
		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if (m.isAnnotationPresent(_Endpoint.class)) {
				Endpoint ep = new Endpoint(this, m);
				Endpoints eps = endpointsMap.get(ep.getEndpointUri());
				if (eps == null) {
					eps = new Endpoints(ep);
					endpointsMap.putIfAbsent(ep.getEndpointUri(), eps);
				} else {
					eps.addEndpoint(ep);
				}
				endpointCount++;
			}
		}
	}

	public Endpoint findEndpoint(String uri, HttpMethod method) {
		Endpoints eps = endpointsMap.get(uri);
		return eps != null ? eps.findEndpoint(method) : null;
	}

	public String getUri() {
		return uri;
	}

	public String getLayout() {
		return anno.layout();
	}

	public Object getResource() {
		return resource;
	}

	public int getEndpointCount() {
		return endpointCount;
	}

	public List<IIntercepter> getIntercepters() {
		return new LinkedList<IIntercepter>(intercepters);
	}

	public String toString() {
		return description;
	}
}
