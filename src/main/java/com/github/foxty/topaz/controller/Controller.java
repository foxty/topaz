package com.github.foxty.topaz.controller;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;

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
	private Object resource;
	private List<IInterceptor> interceptors = new LinkedList<>();

	public Controller(Object resource) {
		this.clazz = resource.getClass();
		this.anno = clazz.getAnnotation(_Controller.class);
		this.resource = resource;

		init();
	}

	private void init() {
		// Init all interceptors
		Class tmpClazz = clazz;
		while (tmpClazz != Object.class) {
			Class<? extends IInterceptor>[] interceptorClazzs = anno.interceptors();
			if (interceptorClazzs != null) {
				for (Class<? extends IInterceptor> interceptorClazz : interceptorClazzs) {
					try {
						IInterceptor inter = interceptorClazz.newInstance();
						interceptors.add(inter);
					} catch (InstantiationException e) {
						log.error("Initialize " + interceptorClazz + " failed!");
					} catch (IllegalAccessException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			tmpClazz = tmpClazz.getSuperclass();
		}
	}

	public String getUri() {
		return anno.uri();
	}

	public String getLayout() {
		return anno.layout();
	}

	public _Controller getAnno() {
		return anno;
	}

	public Object getResource() {
		return resource;
	}

	public List<IInterceptor> getInterceptors() {
		return new LinkedList<IInterceptor>(interceptors);
	}
}
