package com.github.foxty.topaz.controller.interceptor;

import java.util.Iterator;
import java.util.List;

public class IntercepterChain {

	private List<IIntercepter> interceptors;
	private Iterator<IIntercepter> it;

	public IntercepterChain(List<IIntercepter> interceptors) {
		this.interceptors = interceptors;
		this.it = this.interceptors.iterator();
	}

	public void proceed() {
		IIntercepter inter = it.next();
		inter.intercept(this);
	}
}
