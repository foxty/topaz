package com.topaz.dao;

public class DaoException extends RuntimeException {

	/**
	 */
	private static final long serialVersionUID = 1L;
	
	public DaoException(String msg){
		super(msg);
	}
	
	public DaoException(Throwable throwable){
		super(throwable);
	}
	
	public DaoException(String msg, Throwable t){
		super(msg, t);
	}
}
