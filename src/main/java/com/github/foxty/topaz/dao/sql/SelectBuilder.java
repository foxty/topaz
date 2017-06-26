package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.*;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.meta.RelationMeta;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Not thread safe
 *
 * @author foxty
 */
public class SelectBuilder extends SQLBuilder<SelectBuilder> {

    private static Log log = LogFactory.getLog(SelectBuilder.class);

    private boolean limited = false;
    private String[] with;
    private List<RelationMeta> hasMany;

    public SelectBuilder(Class<? extends Model> clazz, String... with) {
        super(clazz);
        this.with = with;
        for (String w : with) {
            RelationMeta rm = modelMeta.getRelationMeta(w);
            Objects.requireNonNull(rm);
            Models.getInstance().register(rm.getFieldClazz());
        }
        buildSQL();
    }

    public SelectBuilder(String sql, List<Object> sqlParams) {
        super(sql, sqlParams);
    }

    @Override
    protected void buildSQL() {
        sql.append("SELECT " + tableName + ".* ");
        String fromSeg = " FROM " + tableName;

        for (String w : with) {
            RelationMeta rm = modelMeta.getRelationMeta(w);
            ModelMeta subModelMeta = Models.getInstance().getModelMeta(rm.getFieldClazz());
            for (ColumnMeta cm : subModelMeta.getColumnMetaMap().values()) {
                String cName = cm.getColumnName();
                String colFullName = w + "." + cName;
                sql.append("," + colFullName + " AS '" + colFullName + "'");
            }
            String tblName = subModelMeta.getTableName();
            String byKey = rm.byKey();

            switch (rm.getRelation()) {
                case HasOne:
                    fromSeg += (" JOIN " + tblName + " " + w + " ON "
                            + tableName + ".id=" + w + "." + byKey);
                    break;
                case HasMany:
                    if (hasMany == null) {
                        hasMany = new ArrayList<>();
                    }
                    hasMany.add(rm);
                    break;
                case BelongsTo:
                    fromSeg += (" JOIN " + tblName + " " + w + " ON "
                            + tableName + "." + byKey + "=" + w + ".id");
                    break;
            }
        }
        sql.append(fromSeg);
    }

    private ColumnMeta findWithColumn(String with, String prop) {
        RelationMeta rm = modelMeta.getRelationMeta(with);
        ModelMeta withM = Models.getInstance().getModelMeta(rm.getFieldClazz());
        ColumnMeta cm = withM.getColumnMeta(prop);
        if (cm == null) {
            throw new DaoException("No column mapping found for property "
                    + with + "." + prop + "!");
        }
        return cm;
    }

    public SelectBuilder condition(String with, String prop, Object value) {
        return condition(with, prop, OP.EQ, value);
    }

    public SelectBuilder condition(String with, String prop, OP op, Object value) {
        ColumnMeta pm = findWithColumn(with, prop);
        sql.append(" " + with + ".").append(pm.getColumnName())
                .append(op.getValue()).append("? ");
        sqlParams.add(value);
        return this;
    }

    public SelectBuilder where(String with, String propName, Object value) {
        return where(with, propName, OP.EQ, value);
    }

    public SelectBuilder where(String with, String propName, OP op,
                               Object value) {
        ColumnMeta pm = findWithColumn(with, propName);
        sql.append(" WHERE ").append(with + ".").append(pm.getColumnName())
                .append(op.getValue()).append("? ");
        sqlParams.add(value);
        return this;
    }

    public SelectBuilder and(String with, String prop, Object value) {
        return and(with, prop, OP.EQ, value);
    }

    public SelectBuilder and(String with, String prop, OP op, Object value) {
        and().condition(with, prop, op, value);
        return this;
    }

    public SelectBuilder or(String with, String prop, Object value) {
        return or(with, prop, OP.EQ, value);
    }

    public SelectBuilder or(String with, String prop, OP op, Object value) {
        or().condition(with, prop, op, value);
        return this;
    }

    public SelectBuilder orderBy(String prop, boolean ascending) {
        ColumnMeta pm = getColumnMapping(prop);
        if (null != pm) {
            sql.append(" ORDER BY ").append(tableName).append(".")
                    .append(pm.getColumnName());
            sql.append(ascending ? " asc " : " desc ");
        }
        return this;
    }

    public SelectBuilder orderBy(String with, String propName,
                                 boolean ascending) {
        ColumnMeta pm = findWithColumn(with, propName);
        if (null != pm) {
            sql.append(" ORDER BY ").append(with + ".")
                    .append(pm.getColumnName());
            sql.append(ascending ? " asc " : " desc ");
        }
        return this;
    }

    public SelectBuilder limit(Integer offset, Integer count) {
        if (limited) {
            throw new DaoException("Limit segment already added! SQL:" + sql);
        }
        if (offset != null && count != null) {
            sql.append(" LIMIT ").append(offset).append(",").append(count);
            limited = true;
        }
        return this;
    }

    public <T extends Model> T first() {
        if (!limited) {
            limit(0, 1);
        }
        List<T> result = fetch();
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Get list of objects from table
     *
     * @param <T> type of the object
     * @return list of object
     */
    @SuppressWarnings("unchecked")
    public <T extends Model> List<T> fetch() {
        log.debug("Fetch  - " + sql);
        DaoManager mgr = DaoManager.getInstance();
        List<T> result = (List<T>) mgr.useConnection(new IConnVisitor() {

            public Object visit(Connection conn) throws SQLException {
                QueryRunner runner = new QueryRunner();
                TopazResultSetHandler<T> h = new TopazResultSetHandler<T>(
                        modelClazz);
                List<T> result = runner.query(conn, sql.toString(), h,
                        sqlParams.toArray());

                if (hasMany != null) {
                    for (T re : result) {
                        for (RelationMeta rm : hasMany) {
                            ModelMeta mm = Models.getInstance().getModelMeta(rm.getFieldClazz());
                            String byKey = rm.byKey();
                            String sql = "SELECT * FROM " + mm.getTableName() + " WHERE " + byKey
                                    + "=?";
                            TopazResultSetHandler subHandler = new TopazResultSetHandler(
                                    rm.getFieldClazz());
                            List<Object> subResult = (List<Object>) runner.query(conn, sql, subHandler,
                                    new Object[]{re.getId()});

                            Method write = rm.getWriteMethod();
                            try {
                                write.invoke(re, subResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                throw new DaoException(e);
                            }
                        }
                    }
                }
                return result;
            }
        });
        return result;
    }

    /**
     * Get number of objects via "select count(1)"
     *
     * @return Long count of records
     */
    public long count() {
        Long re = 0L;
        final StringBuffer countSql = new StringBuffer(sql.toString());
        countSql.replace(7, sql.indexOf("FROM"), " COUNT(1) ");
        log.debug("Fetch Count - " + countSql);
        DaoManager mgr = DaoManager.getInstance();
        re = (Long) mgr.useConnection(conn -> {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<Long> h = new ScalarHandler<Long>(1);
            return (Long) runner.query(conn, countSql.toString(), h,
                    sqlParams.toArray());
        });
        return re;
    }
}