package com.github.foxty.topaz.controller.anno;

import com.github.foxty.topaz.controller.interceptor.IInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the controller class
 * <p>
 * Created by itian on 6/13/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    String uri() default "/";

    Class<? extends IInterceptor>[] interceptors() default {};
}
