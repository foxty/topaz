package com.topaz.dao;

public interface ITransVisitor {
	/**
	 * throw exceptions to rollback transaction
	 */
	void visit();
}
