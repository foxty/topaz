package com.github.foxty.topaz.controller.interceptor;

/**
 * Interface for intercepter use chain pattern to intercept requests.
 * 
 * @author itian
 *
 */
public interface IIntercepter {

	void intercept(IntercepterChain chain);

}
