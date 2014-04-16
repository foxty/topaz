package com.topaz.controller.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.topaz.common.TopazException;
import com.topaz.controller.Controller;
import com.topaz.controller.ControllerException;
import com.topaz.controller.WebContext;
import com.topaz.dao.DaoManager;
import com.topaz.dao.ITransVisitor;
import com.topaz.dao.Transactional;

/**
 * Final interceptor was last in the chain to handle the resource request.
 * 
 * @author itian
 */
public class FinalInterceptor implements IInterceptor {

	private Log log = LogFactory.getLog(FinalInterceptor.class);
	private Controller controller;
	private Method targetMethod;

	public FinalInterceptor(Controller c) {
		controller = c;
	}

	public void intercept(InterceptorChain chain) {
		WebContext wc = WebContext.get();
		String methodName = wc.getMethodName();

		if (log.isDebugEnabled()) {
			log.debug("Execute method " + wc.getControllerName() + "." + methodName);
		}
		Method[] ms = controller.getClass().getMethods();
		boolean founded = false;
		for (Method m : ms) {
			if (m.getName().equals(methodName)) {
				founded = true;
				targetMethod = m;
				break;
			}
		}

		if (founded) {
			if (targetMethod.isAnnotationPresent(Transactional.class)) {
				if (log.isDebugEnabled()) {
					log.debug("Use transaction on method " + wc.getControllerName() + "."
							+ methodName);
				}
				DaoManager.getInstance().transaction(new ITransVisitor() {
					@Override
					public void visit() {
						invokeTargetMethod();
					}
				});

			} else {
				invokeTargetMethod();
			}
		} else {
			controller.render(methodName + ".ftl");
		}

	}

	private void invokeTargetMethod() {
		try {
			targetMethod.invoke(controller);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof TopazException) {
				throw (TopazException) e.getTargetException();
			} else {
				throw new ControllerException(e);
			}
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
}
