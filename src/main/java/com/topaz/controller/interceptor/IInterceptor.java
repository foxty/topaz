package com.topaz.controller.interceptor;

import com.topaz.controller.WebContext;

/**
 * Interface for interceptor use chain pattern to intercept requests.
 * @author itian
 *
 */
public interface IInterceptor {
	
	void intercept(InterceptorChain chain, WebContext wc);
	
}
