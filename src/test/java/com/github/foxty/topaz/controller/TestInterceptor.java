package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.interceptor.IIntercepter;
import com.github.foxty.topaz.controller.interceptor.IntercepterChain;

class TestInterceptor implements IIntercepter {

    public boolean called;

    @Override
    public void intercept(IntercepterChain chain) {
        called = true;
        chain.proceed();
    }
}