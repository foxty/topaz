package com.github.foxty.topaz.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of endpoint mapping to same URI
 * 
 * @author itian
 *
 */
public class Endpoints {

	private Map<HttpMethod, Endpoint> epMap = new HashMap<>();
	private Endpoint defaultEp;

	public Endpoints(Endpoint ep) {
		addEndpoint(ep);
	}

	public void addEndpoint(Endpoint ep) {
		if (ep.getAllowHttpMethod() == HttpMethod.ANY || defaultEp == null) {
			defaultEp = ep;
		}
		Endpoint oldValue = epMap.putIfAbsent(ep.getAllowHttpMethod(), ep);
		if (null != oldValue) {
			throw new ControllerException("EP URI conflict between " + oldValue + " and " + ep);
		}
	}

	public Endpoint findEndpoint(HttpMethod method) {
		return epMap.getOrDefault(method, defaultEp);
	}
}
