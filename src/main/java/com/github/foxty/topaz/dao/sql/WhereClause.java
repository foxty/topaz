package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;

/**
 * Created by itian on 6/27/2017.
 */
public class WhereClause extends Clause {

    private static class State {
        /*
         Even number stand for PREDICATE, odd number stand for KEYWORD.
         Status field increase 1 when state change.

         Default value is 1 means its a keyword state.
          */
        private int status = 1;

        public void toPredicate() {
            status = 0;
        }

        public void toKeyword() {
            status = 1;
        }

        public boolean isPredicate() {
            return status ==0;
        }

        public boolean isKeyword() {
            return status == 1;
        }
    }

    private State state = new State();

    public WhereClause(ModelMeta modelMeta, String propName, Operators op, Object value) {
        super(modelMeta);
        clause.append(" WHERE ");
        this.predicate(propName, op, value);
    }

    public WhereClause(ModelMeta modelMeta) {
        super(modelMeta);
        clause.append(" WHERE ");
    }

    public WhereClause predicate(String prop, Object value) {
        return predicate(prop, Operators.EQ, value);
    }

    public WhereClause predicate(String prop, Operators op, Object value) {
        ColumnMeta cm = modelMeta.getColumnMeta(prop);
        clause.append(" " + cm.getTableName() + ".").append(cm.getColumnName())
                .append(op.getValue()).append("? ");
        params.add(value);
        state.toPredicate();
        return this;
    }

    public WhereClause and(IClauseBracket bracket) {
        clause.append(" AND ( ");
        state.toKeyword();
        bracket.predicates(this);
        clause.append(" ) ");
        return this;
    }

    public WhereClause or(IClauseBracket bracket) {
        clause.append(" OR ( ");
        state.toKeyword();
        bracket.predicates(this);
        clause.append(" ) ");
        return this;
    }
    
    public WhereClause and(String p, Object v) {
    	return and(p, Operators.EQ, v);
    }

    public WhereClause and(String prop, Operators op, Object value) {
        if(state.isPredicate()) {
            clause.append(" AND ");
        }
        state.toKeyword();
        predicate(prop, op, value);
        return this;
    }
    
    public WhereClause or(String p, Object v) {
    	return or(p, Operators.EQ, v);
    }

    public WhereClause or(String prop, Operators op, Object value) {
        if(state.isPredicate()) {
            clause.append(" OR ");
        }
        state.toKeyword();
        predicate(prop, op, value);
        return this;
    }
}

