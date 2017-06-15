package com.github.foxty.topaz.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itian on 6/15/2017.
 */
public class View {

    private String layout;
    private String name;
    private Map<String, Object> responseData;

    private View(String layout, String name, Map<String, Object> data) {
        this.layout = layout;
        this.name = name;
        this.responseData = data;
    }

    public static View create(String layout, String name) {
        return new View(layout, name, null);
    }

    public static View create(String name) {
        return  new View(null, name, null);
    }

    public View data(String name, Object value) {
        if(responseData == null) {
            responseData = new HashMap<>();
        }
        responseData.put(name, value);
        return this;
    }

    public String getLayout() {
        return layout;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }
}
