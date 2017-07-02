package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.DaoManager;
import com.github.foxty.topaz.dao.Model;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Not thread safe!
 * @author foxty
 */
public class SQLDelete extends SQLBuilder<SQLDelete> {

	private static Log log = LogFactory.getLog(SQLDelete.class);

	public static class fn {

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

		final static public boolean deleteById(Class<? extends Model> clazz, int id) {
			int c = new SQLDelete(clazz).where("id", id).update();
			return c > 0;
		}

	}

	private SQLDelete(Class<? extends Model> clazz) {
		super(clazz);
		buildSQL();
	}

	@Override
	public void buildSQL() {
		sql.append("DELETE FROM ").append(tableName);
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
		result = (Integer) daoMgr.useConnection(conn-> {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());
		});
		return result;
	}
}