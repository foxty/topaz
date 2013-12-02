package com.topaz.controller.interceptor;

import java.util.List;

import com.topaz.controller.WebContext;

public class InterceptorChain {
	
	private List<IInterceptor> interceptors;
	private int pos;

	public InterceptorChain(
			List<IInterceptor> interceptors) {
		this.interceptors = interceptors;
		this.pos = 0;
	}

	public void proceed(WebContext wc) {
		IInterceptor inter = interceptors.get(pos);
		pos++;
		inter.intercept(this, wc);
	}
}
