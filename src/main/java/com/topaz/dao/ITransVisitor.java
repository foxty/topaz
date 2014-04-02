package com.topaz.dao;

public interface ITransVisitor {
	/**
	 * 
	 * @return true-commit, false-rollback.
	 */
	boolean visit();
}
