package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.sql.SQLDelete;
import com.github.foxty.topaz.dao.sql.SQLSelect;
import com.github.foxty.topaz.dao.sql.SQLUpdate;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class Model implements Serializable {

    private static Log log = LogFactory.getLog(Model.class);
    /*Static Area*/
    /**
     * Read methods
     *
     * @param clazz class of model want to fetch
     * @param with  sub model
     * @return builder itself
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    final static public SQLSelect find(Class<? extends Model> clazz, String... with) {
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

    final static public <T extends Model> T findById(Class<T> clazz, Integer id,
                                                     String... withs) {
        SQLSelect ms = find(clazz, withs).where("id", id);
        return ms.first();
    }

    public static SQLUpdate update(Class<? extends Model> clazz) {
        SQLUpdate ub = new SQLUpdate(clazz);
        return ub;
    }

    public static int updateBySql(Class<? extends Model> clazz, String sql,
                                  List<Object> objects) {
        SQLUpdate ub = new SQLUpdate(clazz, sql, objects);
        return ub.update();
    }

    /**
     * Deletion methods
     *
     * @param clazz model class
     * @return SQLBuilder builder itself
     */
    final static public SQLDelete delete(Class<? extends Model> clazz) {
        SQLDelete sb = new SQLDelete(clazz);
        return sb;
    }


    /*Instance Area*/
    protected ModelMeta modelMeta;
    // Primary Key
    @_Column
    protected Integer id;

    public Model() {
        Models.getInstance().register(getClass());
        this.modelMeta = Models.getInstance().getModelMeta(getClass());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    final public void set(String prop, Object newValue) {
        Map<String, ColumnMeta> mapping = modelMeta.getColumnMetaMap();
        ColumnMeta cm = mapping.get(prop);
        if (null != cm) {
            try {
                cm.getWriteMethod().invoke(this, newValue);
            } catch (Exception e) {
                throw new DaoException(e);
            }
        } else {
            throw new DaoException("No column mapping for " + prop);
        }
    }

    /**
     * Save model and throw exception if failed.
     *
     * @throws DaoException throw DaoException if failed.
     */
    public void save() {
        if (!saved()) {
            throw new DaoException("Save model failed for " + this);
        }
    }

    /**
     * Save model and return true/false.
     *
     * @return boolean success or not
     * @throws DaoException DaoException will throw if no id attached
     */
    public boolean saved() {
        boolean result = false;
        if (getId() != null && getId() != 0) {
            return updated();
        }
        Map<String, ColumnMeta> mapping = modelMeta.getColumnMetaMap();

        final StringBuffer insertSql = new StringBuffer("INSERT INTO ");
        final StringBuffer valueSql = new StringBuffer(" VALUES(");
        final List<Object> params = new ArrayList<Object>(mapping.size());

        String tblName = TopazUtil.camel2flat(getClass().getSimpleName());
        insertSql.append(tblName).append(" (");
        for (Map.Entry<String, ColumnMeta> entry : mapping.entrySet()) {
            ColumnMeta cm = entry.getValue();
            Object propValue;
            try {
                propValue = cm.getReadMethod().invoke(this);
            } catch (Exception e) {
                throw new DaoException(e);
            }
            if (propValue != null) {
                insertSql.append(cm.getColumnName()).append(",");
                valueSql.append("?,");
                params.add(propValue);
            }
        }
        insertSql.replace(insertSql.length() - 1, insertSql.length(), ")");
        valueSql.replace(valueSql.length() - 1, valueSql.length(), ")");
        insertSql.append(valueSql);

        return (Boolean) DaoManager.getInstance().useConnection((Connection conn) -> {
                    Boolean re = false;
                    PreparedStatement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = conn.prepareStatement(
                                insertSql.toString(),
                                Statement.RETURN_GENERATED_KEYS);
                        for (int i = 0; i < params.size(); i++) {
                            statement.setObject(i + 1, params.get(i));
                        }
                        re = statement.executeUpdate() == 1;
                        resultSet = statement.getGeneratedKeys();
                        if (resultSet.next()) {
                            id = resultSet.getInt(1);
                        }
                        return re;
                    } catch (SQLException e) {
                        throw new DaoException(e);
                    } finally {
                        try {
                            DbUtils.close(resultSet);
                        } catch (SQLException e) {
                            log.error(e.getMessage(), e);
                        }
                        try {
                            DbUtils.close(statement);
                        } catch (SQLException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }

        );
    }


    final public void refresh() {
        Model newModel = find(this.getClass()).where("id", id).first();
        if (newModel == null) {
            throw new DaoException("Can't find entity by id " + id);
        }
        Map<String, ColumnMeta> props = modelMeta.getColumnMetaMap();
        for (ColumnMeta cm : props.values()) {
            Method rm = cm.getReadMethod();
            Method wm = cm.getWriteMethod();
            Object v;
            try {
                v = rm.invoke(newModel);
                wm.invoke(this, v);
            } catch (Exception e) {
                throw new DaoException("Refresh model failed!", e);
            }
        }
    }

    /**
     * Update model and return the status.
     *
     * @return boolean success or not
     * @throws DaoException DaoException will throw if no id attached
     */
    final public boolean updated() {
        if (getId() == null || getId().longValue() == 0L) {
            throw new DaoException("Invalid id " + id);
        }
        SQLUpdate ub = new SQLUpdate(this.getClass());

        Map<String, ColumnMeta> mapping = modelMeta.getColumnMetaMap();
        ColumnMeta idMapping = mapping.get("id");

        for (Entry<String, ColumnMeta> entry : mapping.entrySet()) {
            ColumnMeta cm = entry.getValue();
            if (cm == idMapping)
                continue;
            Object newValue;
            try {
                newValue = cm.getReadMethod().invoke(this);
            } catch (Exception e) {
                throw new DaoException(e);
            }
            ub.set(cm.getColumnName(), newValue);
        }

        ub.where("id", getId());
        return ub.update() > 0;
    }

    /**
     * Update methods and throw exception if failed
     *
     * @throws DaoException exception will throw if update failed.
     */
    final public void update() {
        if (!updated()) {
            throw new DaoException("Update model failed for " + this);
        }
    }

    final public boolean increase(String prop) {
        SQLUpdate sb = new SQLUpdate(this.getClass());
        sb.inc(prop, 1).where("id", getId());
        return sb.update() > 0;
    }

    final public boolean decrease(String prop) {
        SQLUpdate sb = new SQLUpdate(this.getClass());
        sb.dec(prop, 1).where("id", getId());
        return sb.update() > 0;
    }

    public boolean deleted() {
        SQLDelete db = new SQLDelete(this.getClass());
        db.where("id", id);
        return db.update() > 0;
    }

}

