package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.controller.anno.Controller;
import com.github.foxty.topaz.controller.anno.Endpoint;

/**
 * Created by itian on 6/13/2017.
 */
@Controller(uri = "/test")
public class TestController {

    @Endpoint()
    public void get() {

    }
}
