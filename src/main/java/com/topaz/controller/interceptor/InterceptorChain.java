package com.topaz.controller.interceptor;

import java.util.Iterator;
import java.util.List;

public class InterceptorChain {

	private List<IInterceptor> interceptors;
	private Iterator<IInterceptor> it;

	public InterceptorChain(List<IInterceptor> interceptors) {
		this.interceptors = interceptors;
		this.it = interceptors.iterator();
	}

	public void proceed() {
		IInterceptor inter = it.next();
		inter.intercept(this);
	}
}
