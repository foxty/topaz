package com.github.foxty.topaz.dao.sql;

import java.util.List;

import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.DaoManager;
import com.github.foxty.topaz.dao.Model;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
public class SQLUpdate extends SQLBuilder<SQLUpdate> {

	private static Log log = LogFactory.getLog(SQLUpdate.class);

	public static class fn {

		public static int updateBySql(Class<? extends Model> clazz, String sql,
									  List<Object> objects) {
			SQLUpdate ub = new SQLUpdate(clazz, sql, objects);
			return ub.update();
		}

        public static SQLUpdate update(Class<? extends Model> clazz) {
            SQLUpdate ub = new SQLUpdate(clazz);
            return ub;
        }
	}

	public SQLUpdate(Class<? extends Model> clazz) {
		super(clazz);
		buildSQL();
	}

	public SQLUpdate(Class<? extends Model> clazz, String sql,
                     List<Object> params) {
		super(clazz);
		this.sql.append(sql);
		this.sqlParams.addAll(params);
	}

	@Override
	public void buildSQL() {
		sql.append("UPDATE ").append(tableName).append(" SET ");
	}

	public SQLUpdate set(String propName, Object value) {
		ColumnMeta pm = getColumnMeta(propName);
		if (!StringUtils.endsWith(sql.toString(), "SET ")) {
			sql.append("," + pm.getColumnName()).append("=?");
		} else {
			sql.append(pm.getColumnName()).append("=?");
		}
		sqlParams.add(value);
		return this;
	}

	/**
	 * Increase target property by step. Only supported in UPDATE.
	 * 
	 * @param propName property name
	 * @param step	step use for increase
	 * @return UpdateBuilder builder itself
	 */
	public SQLUpdate inc(String propName, int step) {
		ColumnMeta pm = getColumnMeta(propName);
		sql.append(pm.getColumnName()).append(" = ").append(pm.getColumnName())
				.append(" + ").append(step);
		return this;
	}

	/**
	 * Decrease target property by step, only support in UPDATE.
	 * 
	 * @param propName property name
	 * @param step step use for decrease
	 * @return UpdateBuilder build itself
	 */
	public SQLUpdate dec(String propName, int step) {
		ColumnMeta pm = getColumnMeta(propName);
		sql.append(pm.getColumnName()).append(" = ").append(pm.getColumnName())
				.append(" - ").append(step);
		return this;
	}

	/**
	 * Update target table via INSERT, UPDATE, DELETE
	 * 
	 * @return int - how many records affected
	 */
	public int update() {
		log.debug("Update sql = " + sql.toString());
		int result = 0;
		DaoManager daoMgr = DaoManager.getInstance();
		result = (Integer) daoMgr.useConnection(conn -> {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());
		});
		return result;
	}
}