package com.github.foxty.topaz.controller.response;

/**
 * Redirect response
 * 
 * Created by itian on 6/15/2017.
 */
public class Redirect extends Response {

	private String targetUri;

	private Redirect(String u) {
		this.targetUri = u;
	}

	public static Redirect to(String uri) {
		return new Redirect(uri);
	}

	public String getTargetUri() {
		return targetUri;
	}
}
