package com.topaz.controller;

import java.util.HashMap;
import java.util.Map;

public class JSONResult {

	private boolean success;
	private Map<String, Object> data;
	private Map<String, String> errors;

	public JSONResult() {
		this(false);
	}

	public JSONResult(boolean succ) {
		this.success = succ;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void addData(String key, Object d) {
		if (data == null)
			this.data = new HashMap<String, Object>();
		this.data.put(key, d);
	}

	public void addError(String code, String message) {
		if (errors == null) {
			errors = new HashMap<String, String>();
		}
		errors.put(code, message);
	}

	public void addErrors(Map<String, String> errors) {
		if (this.errors == null) {
			this.errors = new HashMap<String, String>();
		}
		this.errors.putAll(errors);
	}

	public Map<String, String> getErrors() {
		return errors;
	}
}
