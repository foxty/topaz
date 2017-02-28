package com.topaz.dao;

@FunctionalInterface
public interface ITransVisitor {
	/**
	 * throw exceptions to rollback transaction
	 */
	void visit();
}
