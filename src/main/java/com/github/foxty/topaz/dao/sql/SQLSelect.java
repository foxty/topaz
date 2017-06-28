package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.*;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.meta.RelationMeta;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Not thread safe
 *
 * @author foxty
 */
public class SQLSelect extends SQLBuilder<SQLSelect> {

    private static Log log = LogFactory.getLog(SQLSelect.class);

    public static class fn {

        public static <T extends Model> T  findById(Class<? extends Model> clazz, int id) {
            return find(clazz).where("id", id).first();
        }

        public static SQLSelect find(Class<? extends Model> clazz, String... with) {
            return new SQLSelect(clazz, with);
        }

        final static public SQLSelect findBySql(Class<? extends Model> clazz,
                                                String sql, Object... sqlParams) {
            return new SQLSelect(sql, Arrays.asList(sqlParams));
        }

        final static public List<Map<String, Object>> findBySql(final String sql,
                                                                final Object... sqlParams) {
            DaoManager mgr = DaoManager.getInstance();
            List<Map<String, Object>> result = mgr.useConnection(conn -> {
                QueryRunner runner = new QueryRunner();
                MapListHandler h = new MapListHandler();
                return runner.query(conn, sql, h, sqlParams);
            });
            return result;
        }
    }


    private boolean limited = false;
    private String[] with;
    private List<RelationMeta> hasMany;

    public SQLSelect(Class<? extends Model> clazz, String... with) {
        super(clazz);
        this.with = with;
        for (String w : with) {
            RelationMeta rm = modelMeta.getRelationMeta(w);
            Objects.requireNonNull(rm);
            Models.getInstance().register(rm.getFieldClazz());
        }
        buildSQL();
    }

    public SQLSelect(String sql, List<Object> sqlParams) {
        super(sql, sqlParams);
    }

    @Override
    protected void buildSQL() {
        sql.append("SELECT * ");
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

    public SQLSelect orderBy(String prop, boolean ascending) {
        ColumnMeta pm = getColumnMeta(prop);
        if (null != pm) {
            sql.append(" ORDER BY ").append(pm.getTableName()).append(".")
                    .append(pm.getColumnName());
            sql.append(ascending ? " ASC " : " DESC ");
        }
        return this;
    }


    public SQLSelect limit(Integer offset, Integer count) {
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