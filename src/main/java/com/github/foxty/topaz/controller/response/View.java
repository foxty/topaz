package com.github.foxty.topaz.controller.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by itian on 6/15/2017.
 */
public class View extends Response {

	private boolean noLayout;
	private String layout;
	private String name;
	private Map<String, Object> responseData;

	private View(String layout, String name, boolean noLayout) {
		this.layout = layout;
		this.name = name;
		this.noLayout = noLayout;
	}

	public static View create(String layout, String name) {
		return new View(layout, name, false);
	}

	public static View create(String name) {
		return new View(null, name, false);
	}

	public static View createWithoutLayout(String name) {
		return new View(null, name, true);
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getLayout() {
		return layout;
	}

	public String getName() {
		return name;
	}

	public boolean isNoLayout() {
		return noLayout;
	}
}
