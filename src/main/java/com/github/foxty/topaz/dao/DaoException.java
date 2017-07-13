package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.common.TopazException;

public class DaoException extends TopazException {

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
