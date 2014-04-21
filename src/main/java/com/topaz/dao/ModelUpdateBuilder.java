package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;

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
	public ModelUpdateBuilder inc(String columnName, int step) {
		sql.append(columnName).append(" = ").append(columnName).append(" + ")
				.append(step);
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
		result = (Integer) daoMgr.accessDB(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());

			}
		});
		return result;
	}
}