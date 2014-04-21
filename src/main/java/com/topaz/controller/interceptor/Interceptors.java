package com.topaz.controller.interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptors {
	Class<? extends IInterceptor>[] interceptors();
}

