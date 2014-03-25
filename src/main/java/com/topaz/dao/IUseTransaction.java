package com.topaz.dao;

public interface IUseTransaction {
	/**
	 * 
	 * @return true-commit, false-rollback.
	 */
	boolean transaction();
}
