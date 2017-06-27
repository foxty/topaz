package com.github.foxty.topaz.dao.sql;

/**
 * Created by itian on 6/27/2017.
 */
@FunctionalInterface
interface IClauseBracket {
    void predicates(WhereClause clause);
}
