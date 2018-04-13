package com.github.foxty.topaz.controller;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.controller.interceptor.FinalIntercepter;
import com.github.foxty.topaz.controller.interceptor.IIntercepter;
import com.github.foxty.topaz.controller.interceptor.IntercepterChain;
import com.github.foxty.topaz.dao.DaoManager;

/**
 * Endpoint stand for an access point, which target to a method of a controller.
 * It has interceptors, baseUri mapping, controller instance and method
 * instance.
 * <p>
 * Created by itian on 6/13/2017.
 */
public class Endpoint {

	private static Log log = LogFactory.getLog(Endpoint.class);

	private String baseUri;
	private String methodUri;
	private List<IIntercepter> interceptorList;
	private Controller controller;
	private Method method;
	private HttpMethod allowHttpMethod;
	private boolean isTransactional;

	public Endpoint(Controller controller, Method method) {
		this.baseUri = controller.getUri();
		this.interceptorList = controller.getIntercepters();
		this.controller = controller;
		this.method = method;

		init();
	}

	private void init() {
		Objects.requireNonNull(interceptorList, "InterceptorList should not be null.");
		com.github.foxty.topaz.annotation.Endpoint _endpoint = method.getAnnotation(com.github.foxty.topaz.annotation.Endpoint.class);
		Objects.requireNonNull(_endpoint, "@EP should not be null.");
		methodUri = _endpoint.uri();
		allowHttpMethod = _endpoint.method();
		isTransactional = _endpoint.isTransactional();

		FinalIntercepter fin = new FinalIntercepter(controller, method);
		interceptorList.add(fin);

		baseUri = TopazUtil.cleanUri(baseUri);
		methodUri = TopazUtil.cleanUri(methodUri);
	}

	public HttpMethod getAllowHttpMethod() {
		return allowHttpMethod;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getEndpointUri() {
		return TopazUtil.cleanUri(baseUri + "/" + methodUri);
	}

	public void execute() {
		WebContext wc = WebContext.get();
		HttpMethod requestMethod = HttpMethod.valueOf(wc.getRequest().getMethod());
		if (log.isDebugEnabled()) {
			log.debug("Access endpoint: " + this);
		}

		if (allowHttpMethod != HttpMethod.ANY && allowHttpMethod != requestMethod) {
			wc.getResponse().setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			log.warn("Endpoint " + this + " is not support method " + requestMethod);
			return;
		}
		// intercepter chain
		IntercepterChain chain = new IntercepterChain(interceptorList);
		if (isTransactional) {
			if (log.isDebugEnabled()) {
				log.debug("Wrap transaction on " + this.toString() + ".");
			}
			DaoManager.getInstance().useTransaction(() -> {
				chain.proceed();
			});
		} else {
			chain.proceed();
		}
	}

	public String toString() {
		return "Endpoint: " + this.getEndpointUri() + " on " + controller.getResource().getClass().getName() + "."
				+ method.getName();
	}
}
