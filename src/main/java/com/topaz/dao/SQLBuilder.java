package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.topaz.common.TopazUtil;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
@SuppressWarnings("unchecked")
public class SQLBuilder {

	private static Log log = LogFactory.getLog(SQLBuilder.class);

	public final static String EQ = " = ";
	public final static String LT = " < ";
	public final static String GT = " > ";
	public final static String LE = " <= ";
	public final static String GE = " >= ";
	public final static String LIKE = " like ";

	private Class clazz;
	private String tableName;
	private Map<String, PropertyMapping> mappings;
	private List<Object> sqlParams = new ArrayList<Object>();
	private SQLBuilderType type;
	private StringBuffer sql = new StringBuffer();

	private boolean limited = false;

	public SQLBuilder(Class clazz, Map<String, PropertyMapping> mappings,
			SQLBuilderType type) {
		this.clazz = clazz;
		this.tableName = TopazUtil.camel2flat(clazz.getSimpleName());
		this.mappings = mappings;
		this.type = type;

		buildSQL();
	}

	private void buildSQL() {
		switch (this.type) {
		case INSERT:
			sql.append("INSERT INTO ").append(this.tableName).append(" ( ");
			break;
		case UPDATE:
			sql.append("UPDATE ").append(this.tableName).append(" SET ");
			break;
		case SELECT:
			sql.append("SELECT ");
			for (PropertyMapping pm : mappings.values()) {
				sql.append(pm.getColumnName()).append(" AS ").append(
						pm.getPropertyName()).append(",");
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(" FROM ").append(this.tableName);
			break;
		case DELETE:
			sql.append("DELETE FROM ").append(this.tableName);
			break;
		default:
			throw new DaoException("Unrecognized SQLBuilderType " + this.type);
		}
	}

	private PropertyMapping findProp(String prop) {
		PropertyMapping pm = mappings.get(prop);
		if (pm == null) {
			throw new DaoException("No column mapping found for property "
					+ prop + "!");
		}
		return pm;
	}

	public SQLBuilder where(String prop, Object value) {
		return where(prop, EQ, value);
	}

	public SQLBuilder where(String prop, String op, Object value) {
		PropertyMapping pm = findProp(prop);
		sql.append(" WHERE ").append(pm.getColumnName()).append(op)
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder and(String prop, Object value) {
		return and(prop, EQ, value);
	}

	public SQLBuilder and(String prop, String op, Object value) {
		PropertyMapping pm = findProp(prop);
		sql.append(" AND ").append(pm.getColumnName()).append(op).append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder or(String prop, Object value) {
		return or(prop, EQ, value);
	}

	public SQLBuilder or(String prop, String op, Object value) {
		PropertyMapping pm = findProp(prop);
		sql.append(" OR ").append(pm.getColumnName()).append(op).append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder orderBy(String prop, boolean ascending) {
		if (this.type != SQLBuilderType.SELECT)
			throw new DaoException("Orderby is only supported by SELECT query!");
		PropertyMapping pm = mappings.get(prop);
		if (null != pm) {
			sql.append(" ORDER BY ").append(pm.getColumnName());
			sql.append(ascending ? " asc " : " desc ");
		}
		return this;
	}

	public SQLBuilder limit(Integer offset, Integer count) {
		if (this.type != SQLBuilderType.SELECT) {
			throw new DaoException("Limit is only supported by SELECT query!");
		}
		if (limited) {
			throw new DaoException("Limit segment already added! SQL:" + sql);
		}
		if (offset != null && count != null) {
			sql.append(" LIMIT ").append(offset).append(",").append(count);
			limited = true;
		}
		return this;
	}

	public <T> T fetchFirst() {
		if (!limited) {
			limit(0, 1);
		}
		List<T> result = fetch();
		return result.isEmpty() ? null : result.get(0);
	}

	/**
	 * Get list of objects from table
	 * 
	 * @return List
	 */
	public <T> List<T> fetch() {
		if (this.type != SQLBuilderType.SELECT)
			throw new DaoException("Fetch is only supported by SELECT query!");
		log.debug("Fetch  - " + sql);
		DaoManager mgr = DaoManager.getInstance();
		List<T> result = (List<T>) mgr.accessDB(new IAccessDB() {

			public Object useDB(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				ResultSetHandler<List<T>> h = new BeanListHandler<T>(clazz);
				return runner.query(conn, sql.toString(), h, sqlParams
						.toArray());
			}
		});
		return result;
	}

	/**
	 * Get number of objects via "select count(id)"
	 * 
	 * @return Long
	 */
	public long fetchCount() {
		if (this.type != SQLBuilderType.SELECT)
			throw new DaoException("Fecth is only supported by SELECT query!");
		Long re = 0L;
		sql.replace(7, sql.indexOf("FROM"), " COUNT(id) ");
		log.debug("Fetch Count - " + sql);
		DaoManager mgr = DaoManager.getInstance();
		re = (Long) mgr.accessDB(new IAccessDB() {

			public Object useDB(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				ResultSetHandler h = new ScalarHandler(1);
				return (Long) runner.query(conn, sql.toString(), h, sqlParams
						.toArray());

			}
		});
		return re;
	}

	/**
	 * Set new value for UPDATE or INSERT
	 * 
	 * @param values
	 */
	public SQLBuilder set(List<String> columns, List<Object> values) {
		switch (this.type) {
		case UPDATE:
			for (String c : columns) {
				sql.append(c).append("=?, ");
			}
			sql.deleteCharAt(sql.length() - 2);
			break;
		case INSERT:
			if (true)
				throw new DaoException("Not suported yet!");
			StringBuffer valueSql = new StringBuffer(" VALUES(");
			for (String c : columns) {
				sql.append(c).append(",");
			}
			valueSql.deleteCharAt(valueSql.length() - 1).append("");
			sql.deleteCharAt(sql.length() - 1).append(") ");
			sql.append(valueSql);
			break;
		default:
			throw new DaoException(
					"SQLBuilder.set only support UPDATE or INSERT!");
		}

		sqlParams.addAll(values);
		return this;
	}

	/**
	 * Increase target property by step. Only supported in UPDATE.
	 * 
	 * @param prop
	 * @param step
	 * @return
	 */
	public SQLBuilder inc(String prop, int step) {
		PropertyMapping pm = mappings.get(prop);
		String columnName = pm.getColumnName();
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
		result = (Integer) daoMgr.accessDB(new IAccessDB() {

			public Object useDB(Connection conn) throws SQLException {
				QueryRunner qr = new QueryRunner();
				return qr.update(conn, sql.toString(), sqlParams.toArray());

			}
		});
		return result;
	}
	
	@Override
	public String toString()
	{
		return sql.toString();		
	}
}