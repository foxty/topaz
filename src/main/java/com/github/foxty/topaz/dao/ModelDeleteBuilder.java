package com.github.foxty.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Not thread safe!
 * @author foxty
 */
public class ModelDeleteBuilder extends ModelSQLBuilder<ModelDeleteBuilder> {

	private static Log log = LogFactory.getLog(ModelDeleteBuilder.class);

	public ModelDeleteBuilder(Class<? extends BaseModel> clazz) {
		super(clazz);
		buildSQL();
	}

	@Override
	public void buildSQL() {
		sql.append("DELETE FROM ").append(baseTableName);
	}

	/**
	 * Update target table via DELETE
	 * 
	 * @return updated records count
	 */
	public int update() {
		log.debug("Delete = " + sql.toString());
		int result = 0;
		DaoManager daoMgr = DaoManager.getInstance();
		result = (Integer) daoMgr.useConnection((Connection conn)-> {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());
		});
		return result;
	}
}