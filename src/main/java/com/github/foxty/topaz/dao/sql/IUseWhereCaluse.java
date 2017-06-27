package com.github.foxty.topaz.dao.sql;

/**
 * Created by itian on 6/27/2017.
 */
@FunctionalInterface
public interface IUseWhereCaluse {
    void where(WhereClause where);
}
