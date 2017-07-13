package com.github.foxty.topaz.annotation;

import com.github.foxty.topaz.controller.interceptor.IIntercepter;

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
public @interface _Controller {

    /*
    A URI is a sequence of characters from a very limited set:
    the letters of the basic Latin alphabet, digits, and a few special characters.
     */
    String uri() default "/";

    Class<? extends IIntercepter>[] interceptors() default {};

    /*
    Layout of the controller
     */
    String layout() default "";
}
