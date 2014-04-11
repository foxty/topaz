package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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

	@SuppressWarnings("rawtypes") private Class baseModelClazz;
	private final String baseTableName;
	private String[] with;

	private SQLBuilderType type;
	private StringBuffer sql = new StringBuffer();
	private List<Object> sqlParams = new ArrayList<Object>();

	private boolean limited = false;

	public SQLBuilder(Class<? extends BaseModel> clazz, SQLBuilderType type, String... with) {
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
		this.type = type;
		this.with = with;

		buildSQL();
	}

	public SQLBuilder(Class<? extends BaseModel> clazz, String sql, List<Object> sqlParams,
			SQLBuilderType type) {
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
		this.sql.append(sql);
		this.sqlParams.addAll(sqlParams);
		this.type = type;
	}

	private void buildSQL() {
		switch (this.type) {
		case INSERT:
			sql.append("INSERT INTO ").append(baseTableName).append(" ( ");
			break;
		case UPDATE:
			sql.append("UPDATE ").append(baseTableName).append(" SET ");
			break;
		case SELECT:
			Map<String, PropMapping> baseMapping = BaseModel.MODEL_PROPS.get(baseModelClazz);
			sql.append("SELECT " + baseTableName + ".* ");
			String fromSeg = " FROM " + baseTableName;

			for (String w : with) {
				PropMapping tblProp = baseMapping.get(w);
				if (tblProp.isTable()) {
					Map<String, PropMapping> subMapping = BaseModel.MODEL_PROPS.get(tblProp
							.getType());
					for (PropMapping pm : subMapping.values()) {
						String cName = pm.getTargetName();
						String colFullName = w + "." + cName;
						sql.append("," + colFullName + " AS '" + colFullName + "'");
					}

					switch (tblProp.getRelation()) {
					case HasOne:
						fromSeg += (" JOIN " + w + " ON "
								+ baseTableName + ".id=" + w + "." + tblProp.getByKey());
						break;
					case HasMany:
						throw new DaoException("Not support HasMany!");
					case BelongsTo:
						fromSeg += (" JOIN " + w + " ON "
								+ baseTableName + "." + tblProp.getByKey() + "=" + w + ".id");
						break;
					}
				}
			}
			sql.append(fromSeg);
			break;
		case DELETE:
			sql.append("DELETE FROM ").append(baseTableName);
			break;
		default:
			throw new DaoException("Unrecognized SQLBuilderType " + this.type);
		}
	}

	private PropMapping findProp(String prop) {
		Map<String, PropMapping> mapping = BaseModel.MODEL_PROPS.get(baseModelClazz);
		PropMapping pm = mapping.get(prop);
		if (pm == null) {
			throw new DaoException("No column mapping found for property "
					+ prop + "!");
		}
		return pm;
	}

	private PropMapping findProp(String with, String prop) {
		Map<String, PropMapping> mapping = BaseModel.MODEL_PROPS.get(baseModelClazz);
		PropMapping tblPm = mapping.get(with);
		mapping = BaseModel.MODEL_PROPS.get(tblPm.getType());
		PropMapping pm = mapping.get(prop);
		if (pm == null) {
			throw new DaoException("No column mapping found for property " + with + "." +
					prop + "!");
		}
		return pm;
	}

	public SQLBuilder where(String propName, Object value) {
		PropMapping pm = findProp(propName);
		sql.append(" WHERE ").append(baseTableName).append(".").append(pm.getTargetName())
				.append(EQ).append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder where(String with, String propName, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" WHERE ").append(with + ".").append(pm.getTargetName()).append(EQ)
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder and(String prop, Object value) {
		PropMapping pm = findProp(prop);
		sql.append(" AND ").append(pm.getTargetName()).append(EQ).append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder and(String with, String propName, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" AND ").append(with + ".").append(pm.getTargetName()).append(EQ)
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder or(String prop, Object value) {
		PropMapping pm = findProp(prop);
		sql.append(" OR ").append(pm.getTargetName()).append(EQ).append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder or(String with, String propName, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" OR ").append(with + ".").append(pm.getTargetName()).append(EQ)
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public SQLBuilder orderBy(String prop, boolean ascending) {
		if (this.type != SQLBuilderType.SELECT)
			throw new DaoException("Orderby is only supported by SELECT query!");
		PropMapping pm = findProp(prop);
		if (null != pm) {
			sql.append(" ORDER BY ").append(pm.getTargetName());
			sql.append(ascending ? " asc " : " desc ");
		}
		return this;
	}

	public SQLBuilder orderBy(String with, String propName, boolean ascending) {
		if (this.type != SQLBuilderType.SELECT)
			throw new DaoException("Orderby is only supported by SELECT query!");
		PropMapping pm = findProp(with, propName);
		if (null != pm) {
			sql.append(" ORDER BY ").append(with + ".").append(pm.getTargetName());
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
		List<T> result = (List<T>) mgr.accessDB(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				// ResultSetHandler<List<T>> h = new BeanListHandler<T>(clazz,
				// ROW_PROCESSER);
				TopazResultSetHandler<T> h = new TopazResultSetHandler<T>(baseModelClazz);
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
		re = (Long) mgr.accessDB(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				ResultSetHandler<Long> h = new ScalarHandler<Long>(1);
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
			/*
			 * StringBuffer valueSql = new StringBuffer(" VALUES("); for (String
			 * c : columns) { sql.append(c).append(","); }
			 * valueSql.deleteCharAt(valueSql.length() - 1).append("");
			 * sql.deleteCharAt(sql.length() - 1).append(") ");
			 * sql.append(valueSql); break;
			 */
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
		PropMapping pm = findProp(prop);
		String columnName = pm.getTargetName();
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

	@Override
	public String toString()
	{
		return sql.toString();
	}
}