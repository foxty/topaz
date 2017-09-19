package com.github.foxty.topaz.dao;

import static com.github.foxty.topaz.dao.sql.SQLDelete.fn.deleteById;
import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.findById;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.sql.SQLUpdate;

@SuppressWarnings("serial")
public class Model implements Serializable {

	private static Log log = LogFactory.getLog(Model.class);

	/* Instance Area */
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
		ColumnMeta cm = modelMeta.findColumnMeta(prop);
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
	 * @throws DaoException
	 *             throw DaoException if failed.
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
	 * @throws DaoException
	 *             DaoException will throw if no id attached
	 */
	public boolean saved() {
		if (getId() != null && getId() != 0) {
			return updated();
		}
		List<ColumnMeta> columns = modelMeta.getColumns();

		final StringBuffer insertSql = new StringBuffer("INSERT INTO ");
		final StringBuffer valueSql = new StringBuffer(" VALUES(");
		final List<Object> params = new ArrayList<Object>(columns.size());

		String tblName = modelMeta.getTableName();
		insertSql.append(tblName).append(" (");
		for (ColumnMeta cm : columns) {
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
				statement = conn.prepareStatement(insertSql.toString(), Statement.RETURN_GENERATED_KEYS);
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
		List<ColumnMeta> props = modelMeta.getColumns();
		for (ColumnMeta cm : props) {
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
	 * @throws DaoException
	 *             DaoException will throw if no id attached
	 */
	final public boolean updated() {
		if (getId() == null || getId().longValue() == 0L) {
			throw new DaoException("Invalid id " + id);
		}
		SQLUpdate ub = SQLUpdate.fn.update(this.getClass());

		List<ColumnMeta> columns = modelMeta.getColumns();
		ColumnMeta idMapping = modelMeta.findColumnMeta("id");

		for (ColumnMeta cm : columns) {
			if (cm == idMapping)
				continue;
			Object newValue;
			try {
				newValue = cm.getReadMethod().invoke(this);
			} catch (Exception e) {
				throw new DaoException(e);
			}
			ub.set(cm.getFieldName(), newValue);
		}

		ub.where("id", getId());
		return ub.update() > 0;
	}

	/**
	 * Update methods and throw exception if failed
	 *
	 * @throws DaoException
	 *             exception will throw if update failed.
	 */
	final public void update() {
		if (!updated()) {
			throw new DaoException("Update model failed for " + this);
		}
	}

	final public boolean increase(String prop) {
		SQLUpdate sb = SQLUpdate.fn.update(this.getClass());
		sb.inc(prop, 1).where("id", getId());
		return sb.update() > 0;
	}

	final public boolean decrease(String prop) {
		SQLUpdate sb = SQLUpdate.fn.update(this.getClass());
		sb.dec(prop, 1).where("id", getId());
		return sb.update() > 0;
	}

	public boolean deleted() {
		return deleteById(this.getClass(), id);
	}
}
