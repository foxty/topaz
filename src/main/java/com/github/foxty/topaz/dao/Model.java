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

import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.findById;

@SuppressWarnings("serial")
public class Model implements Serializable {

    private static Log log = LogFactory.getLog(Model.class);

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
        Map<String, ColumnMeta> columns = modelMeta.getColumnMetaMap();

        final StringBuffer insertSql = new StringBuffer("INSERT INTO ");
        final StringBuffer valueSql = new StringBuffer(" VALUES(");
        final List<Object> params = new ArrayList<Object>(columns.size());

        String tblName = modelMeta.getTableName();
        insertSql.append(tblName).append(" (");
        for (Map.Entry<String, ColumnMeta> entry : columns.entrySet()) {
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
        Model newModel = findById(this.getClass(), id);
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

