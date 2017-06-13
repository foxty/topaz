package com.github.foxty.topaz.common;

public class TopazException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TopazException(String message) {
		super(message);
	}

	public TopazException(Throwable cause) {
		super(cause);
	}

	public TopazException(String message, Throwable cause) {
		super(message, cause);
	}

}
