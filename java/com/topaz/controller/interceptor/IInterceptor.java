package com.topaz.controller.interceptor;


/**
 * Interface for interceptor use chain pattern to intercept requests.
 * @author itian
 *
 */
public interface IInterceptor {
	
	void intercept(InterceptorChain chain);
	
}
