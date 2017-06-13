package com.github.foxty.topaz.controller.interceptor;

import com.github.foxty.topaz.common.TopazException;
import com.github.foxty.topaz.controller.ControllerException;
import com.github.foxty.topaz.controller.HttpMethod;
import com.github.foxty.topaz.controller.WebContext;
import com.github.foxty.topaz.controller.anno.Allow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Final interceptor was last in the chain to handle the resource request.
 *
 * @author itian
 */
public class FinalInterceptor implements IInterceptor {

    private Log log = LogFactory.getLog(FinalInterceptor.class);
    private Object controller;
    private Method targetMethod;

    public FinalInterceptor(Object controller, Method targetMethod) {
        this.controller = controller;
        this.targetMethod = targetMethod;
    }

    public void intercept(InterceptorChain chain) {
        WebContext wc = WebContext.get();

        if (log.isDebugEnabled()) {
            log.debug("Execute method " + controller.getClass() + "." + targetMethod.getName());
        }
        invokeTargetMethod();
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
