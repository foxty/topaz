package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.anno.Controller;
import com.github.foxty.topaz.controller.anno.EP;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.controller.interceptor.InterceptorChain;

/**
 * Created by itian on 6/13/2017.
 */
@Controller(uri = "/test", interceptors = {TestInterceptor.class})
public class TestController {

    public boolean testGetAccessed = false;
    public boolean testPostAccessed = false;

    @EP
    public void testGet() {
        testGetAccessed = true;
    }

    @EP(uri = "/post", method = HttpMethod.POST, isTransactional = true)
    public void testPost() {
        testPostAccessed = true;
    }
}

class TestInterceptor implements IInterceptor {

    public boolean called;

    @Override
    public void intercept(InterceptorChain chain) {
        called = true;
        chain.proceed();
    }
}
