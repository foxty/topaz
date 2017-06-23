package com.github.foxty.topaz.annotation;

import com.github.foxty.topaz.controller.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by itian on 6/13/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface _Endpoint {
    String uri() default "";
    HttpMethod method() default HttpMethod.GET;
    boolean isTransactional() default false;
}
