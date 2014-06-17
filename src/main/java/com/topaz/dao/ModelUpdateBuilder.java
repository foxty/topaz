package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
public class ModelUpdateBuilder extends ModelSQLBuilder<ModelUpdateBuilder> {

	private static Log log = LogFactory.getLog(ModelUpdateBuilder.class);

	public ModelUpdateBuilder(Class<? extends BaseModel> clazz) {
		super(clazz);
		buildSQL();
	}

	public ModelUpdateBuilder(Class<? extends BaseModel> clazz, String sql,
			List<Object> params) {
		super(clazz);
		this.sql.append(sql);
		this.sqlParams.addAll(params);
	}

	@Override
	public void buildSQL() {
		sql.append("UPDATE ").append(baseTableName).append(" SET ");
	}

	public ModelUpdateBuilder set(String propName, Object value) {
		PropMapping pm = findProp(propName);
		if (!StringUtils.endsWith(sql.toString(), "SET ")) {
			sql.append("," + pm.getTargetName()).append("=?");
		} else {
			sql.append(pm.getTargetName()).append("=?");
		}
		sqlParams.add(value);
		return this;
	}

	/**
	 * Increase target property by step. Only supported in UPDATE.
	 * 
	 * @param prop
	 * @param step
	 * @return
	 */
	public ModelUpdateBuilder inc(String propName, int step) {
		PropMapping pm = findProp(propName);
		sql.append(pm.getTargetName()).append(" = ").append(pm.getTargetName())
				.append(" + ").append(step);
		return this;
	}

	/**
	 * Decrease target property by step, only support in UPDATE.
	 * 
	 * @param propName
	 * @param step
	 * @return
	 */
	public ModelUpdateBuilder dec(String propName, int step) {
		PropMapping pm = findProp(propName);
		sql.append(pm.getTargetName()).append(" = ").append(pm.getTargetName())
				.append(" - ").append(step);
		return this;
	}

	/**
	 * Update target table via INSERT, UPDATE, DELETE
	 * 
	 * @return
	 */
	public int update() {
		log.debug("Update sql = " + sql.toString());
		int result = 0;
		DaoManager daoMgr = DaoManager.getInstance();
		result = (Integer) daoMgr.useConnection(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());

			}
		});
		return result;
	}
}