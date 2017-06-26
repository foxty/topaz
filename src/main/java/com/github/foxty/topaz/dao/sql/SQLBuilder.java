package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.*;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Not thread safe!
 *
 * @author foxty
 */
@SuppressWarnings({"unchecked", "rawtypes"})
abstract public class SQLBuilder<T extends SQLBuilder> {

    public static enum OP {
        EQ(" = "), NE(" != "), LT(" < "), GT(" > "), LE(" <= "), GE(" >= "), IN(" in "), LK(
                " like "), IS(" is ");

        private String value;

        private OP(String v) {
            this.value = v;
        }

        public String getValue() {
            return value;
        }

    }

    protected Class modelClazz;
    protected ModelMeta modelMeta;
    protected String tableName;

    protected StringBuffer sql = new StringBuffer();
    protected List<Object> sqlParams = new ArrayList<Object>();

    public SQLBuilder(Class<? extends Model> clazz) {
        Models.getInstance().register(clazz);
        modelClazz = clazz;
        modelMeta = Models.getInstance().getModelMeta(clazz);
        tableName = modelMeta.getTableName();
    }

    public SQLBuilder(String sql, List<Object> sqlParams) {
        this.sql.append(sql);
        this.sqlParams.addAll(sqlParams);
    }

    abstract protected void buildSQL();

    protected ColumnMeta getColumnMapping(String prop) {
        ColumnMeta cm = modelMeta.getColumnMetaMap().get(prop);
        if (cm == null) {
            throw new DaoException("No column mapping found for property "
                    + modelClazz.getName() + "."
                    + prop + "!");
        }
        return cm;
    }

    public T condition(String prop, Object value) {
        return condition(prop, OP.EQ, value);
    }

    public T condition(String prop, OP op, Object value) {
        ColumnMeta pm = getColumnMapping(prop);
        sql.append(" " + tableName + ".").append(pm.getColumnName())
                .append(op.getValue()).append("? ");
        sqlParams.add(value);
        return (T) this;
    }

    public T and() {
        sql.append(" AND ");
        return (T) this;
    }

    public T or() {
        sql.append(" OR ");
        return (T) this;
    }

    public T bracketStart() {
        sql.append(" ( ");
        return (T) this;
    }

    public T bracketEnd() {
        sql.append(" ) ");
        return (T) this;
    }

    public T where(String propName, Object value) {
        return where(propName, OP.EQ, value);
    }

    public T where(String propName, OP op, Object value) {
        ColumnMeta pm = getColumnMapping(propName);
        sql.append(" WHERE ").append(tableName + ".").append(pm.getColumnName())
                .append(op.getValue()).append("? ");
        sqlParams.add(value);
        return (T) this;
    }

    public T and(String prop, Object value) {
        return and(prop, OP.EQ, value);
    }

    public T and(String prop, OP op, Object value) {
        and().condition(prop, op, value);
        return (T) this;
    }

    public T or(String prop, Object value) {
        return or(prop, OP.EQ, value);
    }

    public T or(String prop, OP op, Object value) {
        or().condition(prop, op, value);
        return (T) this;
    }

    @Override
    public String toString() {
        return sql.toString();
    }
}