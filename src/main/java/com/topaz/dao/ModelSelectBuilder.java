package com.topaz.dao;

import java.lang.reflect.Method;
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

/**
 * Not thread safe!
 * 
 * @author foxty
 */
public class ModelSelectBuilder extends ModelSQLBuilder<ModelSelectBuilder> {

	private static Log log = LogFactory.getLog(ModelSelectBuilder.class);

	private String[] with;
	private List<PropMapping> hasMany;

	private boolean limited = false;

	public ModelSelectBuilder(Class<? extends BaseModel> clazz, String... with) {
		super(clazz);
		this.with = with;

		buildSQL();
	}

	public ModelSelectBuilder(Class<? extends BaseModel> clazz, String sql,
			List<Object> sqlParams) {
		super(clazz);
		this.sql.append(sql);
		this.sqlParams.addAll(sqlParams);
	}

	@Override
	public void buildSQL() {

		Map<String, PropMapping> baseMapping = BaseModel.MODEL_PROPS
				.get(baseModelClazz);
		sql.append("SELECT " + baseTableName + ".* ");
		String fromSeg = " FROM " + baseTableName;

		for (String w : with) {
			PropMapping tblProp = baseMapping.get(w);
			if (tblProp.isTable()) {

				// Get target type and column names
				BaseModel.prepareModel(tblProp.getTargetType());
				Map<String, PropMapping> subMapping = BaseModel.MODEL_PROPS
						.get(tblProp.getTargetType());
				for (PropMapping pm : subMapping.values()) {
					if (pm.isTable())
						continue;
					String cName = pm.getTargetName();
					String colFullName = w + "." + cName;
					sql.append("," + colFullName + " AS '" + colFullName + "'");
				}
				String tblName = tblProp.getTargetName();
				String byKey = tblProp.getByKey();

				switch (tblProp.getRelation()) {
				case HasOne:
					fromSeg += (" JOIN " + tblName + " " + w + " ON "
							+ baseTableName + ".id=" + w + "." + byKey);
					break;
				case HasMany:
					if (hasMany == null) {
						hasMany = new ArrayList<PropMapping>();
					}
					hasMany.add(tblProp);
					break;
				case BelongsTo:
					fromSeg += (" JOIN " + tblName + " " + w + " ON "
							+ baseTableName + "." + byKey + "=" + w + ".id");
					break;
				}
			}
		}
		sql.append(fromSeg);
	}

	private PropMapping findProp(String with, String prop) {
		Map<String, PropMapping> mapping = BaseModel.MODEL_PROPS
				.get(baseModelClazz);
		PropMapping tblPm = mapping.get(with);
		mapping = BaseModel.MODEL_PROPS.get(tblPm.getTargetType());
		PropMapping pm = mapping.get(prop);
		if (pm == null) {
			throw new DaoException("No column mapping found for property "
					+ with + "." + prop + "!");
		}
		return pm;
	}

	public ModelSelectBuilder c(String with, String prop, Object value) {
		return c(with, prop, OP.EQ, value);
	}

	public ModelSelectBuilder c(String with, String prop, OP op, Object value) {
		PropMapping pm = findProp(with, prop);
		sql.append(" " + with + ".").append(pm.getTargetName())
				.append(op.getValue()).append("? ");
		sqlParams.add(value);
		return this;
	}

	public ModelSelectBuilder where(String with, String propName, Object value) {
		return where(with, propName, OP.EQ, value);
	}

	public ModelSelectBuilder where(String with, String propName, OP op,
			Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" WHERE ").append(with + ".").append(pm.getTargetName())
				.append(op.getValue()).append("? ");
		sqlParams.add(value);
		return this;
	}

	public ModelSelectBuilder and(String with, String prop, Object value) {
		return and(with, prop, OP.EQ, value);
	}

	public ModelSelectBuilder and(String with, String prop, OP op, Object value) {
		and().c(with, prop, op, value);
		return this;
	}

	public ModelSelectBuilder or(String with, String prop, Object value) {
		return or(with, prop, OP.EQ, value);
	}

	public ModelSelectBuilder or(String with, String prop, OP op, Object value) {
		or().c(with, prop, op, value);
		return this;
	}

	public ModelSelectBuilder orderBy(String prop, boolean ascending) {
		PropMapping pm = findProp(prop);
		if (null != pm) {
			sql.append(" ORDER BY ").append(baseTableName).append(".")
					.append(pm.getTargetName());
			sql.append(ascending ? " asc " : " desc ");
		}
		return this;
	}

	public ModelSelectBuilder orderBy(String with, String propName,
			boolean ascending) {
		PropMapping pm = findProp(with, propName);
		if (null != pm) {
			sql.append(" ORDER BY ").append(with + ".")
					.append(pm.getTargetName());
			sql.append(ascending ? " asc " : " desc ");
		}
		return this;
	}

	public ModelSelectBuilder limit(Integer offset, Integer count) {
		if (limited) {
			throw new DaoException("Limit segment already added! SQL:" + sql);
		}
		if (offset != null && count != null) {
			sql.append(" LIMIT ").append(offset).append(",").append(count);
			limited = true;
		}
		return this;
	}

	public <T extends BaseModel> T first() {
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
	@SuppressWarnings("unchecked")
	public <T extends BaseModel> List<T> fetch() {
		log.debug("Fetch  - " + sql);
		DaoManager mgr = DaoManager.getInstance();
		List<T> result = (List<T>) mgr.useConnection(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				TopazResultSetHandler<T> h = new TopazResultSetHandler<T>(
						baseModelClazz);
				List<T> result = runner.query(conn, sql.toString(), h,
						sqlParams.toArray());

				if (hasMany != null) {
					for (T re : result) {
						for (PropMapping pm : hasMany) {
							String byKey = pm.getByKey();
							String sql = "SELECT * FROM " + pm.getTargetName() + " WHERE " + byKey
									+ "=?";
							TopazResultSetHandler subHandler = new TopazResultSetHandler(
									pm.getTargetType());
							List<Object> subResult = (List<Object>) runner.query(conn, sql, subHandler,
									new Object[] { re.getId() });

							Method write = pm.getWriteMethod();
							try {
								write.invoke(re, subResult);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
								throw new DaoException(e);
							}
						}
					}
				}

				return result;
			}
		});
		return result;
	}

	/**
	 * Get number of objects via "select count(1)"
	 * 
	 * @return Long
	 */
	public long count() {
		Long re = 0L;
		final StringBuffer countSql = new StringBuffer(sql.toString());
		countSql.replace(7, sql.indexOf("FROM"), " COUNT(1) ");
		log.debug("Fetch Count - " + countSql);
		DaoManager mgr = DaoManager.getInstance();
		re = (Long) mgr.useConnection(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
				ResultSetHandler<Long> h = new ScalarHandler<Long>(1);
				return (Long) runner.query(conn, countSql.toString(), h,
						sqlParams.toArray());
			}
		});
		return re;
	}
}