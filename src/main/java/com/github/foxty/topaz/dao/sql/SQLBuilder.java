package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.*;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import jdk.nashorn.internal.runtime.OptimisticReturnFilters;

import java.util.ArrayList;
import java.util.List;

/**
 * Not thread safe.
 *
 * @author foxty
 */
@SuppressWarnings({"unchecked", "rawtypes"})
abstract public class SQLBuilder<T extends SQLBuilder> {

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

    protected ColumnMeta getColumnMeta(String prop) {
        ColumnMeta cm = modelMeta.findColumnMeta(prop);
        if (cm == null) {
            throw new DaoException("No column mapping found for property "
                    + modelClazz.getName() + "."
                    + prop + "!");
        }
        return cm;
    }

    public T where(IUseWhereCaluse useWhere) {
        WhereClause wc = new WhereClause(modelMeta);
        useWhere.where(wc);
        this.sql.append(wc.getClause());
        this.sqlParams.addAll(wc.getParams());
        return (T)this;
    }

    public T where(String prop, Object value) {
        WhereClause wc = new WhereClause(modelMeta, prop, Operators.EQ, value);
        this.sql.append(wc.getClause());
        this.sqlParams.addAll(wc.getParams());
        return (T) this;
    }

    public T where(String prop, Operators op, Object value) {
        WhereClause wc = new WhereClause(modelMeta, prop, op, value);
        this.sql.append(wc.getClause());
        this.sqlParams.addAll(wc.getParams());
        return (T) this;
    }

    @Override
    public String toString() {
        return sql.toString();
    }
}