package com.github.foxty.topaz.controller;

import org.junit.Test;

import java.net.URL;
import java.util.Enumeration;

/**
 * Created by itian on 6/13/2017.
 */
public class ContTest {

    @Test
    public void testControllerAnno() throws Exception {

        URL url = this.getClass().getClassLoader().getResource("com/github/foxty/topaz");
        System.out.print(url);

        System.out.println(HttpMethod.valueOf("GET"));
    }
}
