package com.topaz.controller.interceptor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.topaz.controller.Controller;
import com.topaz.controller.ControllerException;
import com.topaz.controller.WebContext;
import com.topaz.dao.DaoException;

/**
 * Final interceptor will add to interceptor chain's last position 
 * to handle the resource request.
 * 
 * @author itian
 */
public class FinalInterceptor implements IInterceptor {

	private Log log = LogFactory.getLog(FinalInterceptor.class);
	private Controller controller;

	public FinalInterceptor(Controller c) {
		controller = c;
	}

	public void intercept(InterceptorChain chain, WebContext wc) {
		String method = wc.getMethodName();
		try {
			if(log.isDebugEnabled()) {
				log.debug("Execute method " + method + " on " + wc.getControllerClassUri());
			}
			Method m = controller.getClass().getMethod(method);
			m.invoke(controller);
		} catch (NoSuchMethodException e) {
			log.error("Resource " + method + " not exist in "
					+ wc.getControllerClassUri(), e);
			try {
				wc.getResponse().sendError(404);
			} catch (IOException e1) {
			}
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof ControllerException
					|| e.getTargetException() instanceof DaoException) {
				throw (RuntimeException) e.getTargetException();
			} else {
				log.error(e.getMessage(), e);
				throw new ControllerException(e);
			}
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			throw new ControllerException(e);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
			throw new ControllerException(e);
		}
	}
}
