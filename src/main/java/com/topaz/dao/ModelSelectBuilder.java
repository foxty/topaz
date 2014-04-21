package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;
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

	private boolean limited = false;

	public ModelSelectBuilder(Class<? extends BaseModel> clazz, String... with) {
		super(clazz);
		this.with = with;

		buildSQL();
	}

	public ModelSelectBuilder(Class<? extends BaseModel> clazz, String sql, List<Object> sqlParams) {
		super(clazz);
		this.sql.append(sql);
		this.sqlParams.addAll(sqlParams);
	}

	@Override
	public void buildSQL() {

		Map<String, PropMapping> baseMapping = BaseModel.MODEL_PROPS.get(baseModelClazz);
		sql.append("SELECT " + baseTableName + ".* ");
		String fromSeg = " FROM " + baseTableName;

		for (String w : with) {
			PropMapping tblProp = baseMapping.get(w);
			if (tblProp.isTable()) {
				BaseModel.prepareModel(tblProp.getType());
				Map<String, PropMapping> subMapping = BaseModel.MODEL_PROPS.get(tblProp
						.getType());
				for (PropMapping pm : subMapping.values()) {
					if (pm.isTable()) continue;
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
					throw new DaoException("Not support HasMany!");
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

	public ModelSelectBuilder where(String with, String propName, Object value) {
		return where(with, propName, OP.EQ, value);
	}

	public ModelSelectBuilder where(String with, String propName, OP op, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" WHERE ").append(with + ".").append(pm.getTargetName())
				.append(op.getValue())
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public ModelSelectBuilder and(String with, String propName, Object value) {
		return and(with, propName, OP.EQ, value);
	}

	public ModelSelectBuilder and(String with, String propName, OP op, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" AND ").append(with + ".").append(pm.getTargetName()).append(op.getValue())
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public ModelSelectBuilder or(String with, String propName, Object value) {
		return or(with, propName, OP.EQ, value);
	}

	public ModelSelectBuilder or(String with, String propName, OP op, Object value) {
		PropMapping pm = findProp(with, propName);
		sql.append(" OR ").append(with + ".").append(pm.getTargetName()).append(op.getValue())
				.append("? ");
		sqlParams.add(value);
		return this;
	}

	public ModelSelectBuilder orderBy(String prop, boolean ascending) {
		PropMapping pm = findProp(prop);
		if (null != pm) {
			sql.append(" ORDER BY ").append(baseTableName).append(".").append(pm.getTargetName());
			sql.append(ascending ? " asc " : " desc ");
		}
		return this;
	}

	public ModelSelectBuilder orderBy(String with, String propName, boolean ascending) {
		PropMapping pm = findProp(with, propName);
		if (null != pm) {
			sql.append(" ORDER BY ").append(with + ".").append(pm.getTargetName());
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
	@SuppressWarnings("unchecked")
	public <T> List<T> fetch() {
		log.debug("Fetch  - " + sql);
		DaoManager mgr = DaoManager.getInstance();
		List<T> result = (List<T>) mgr.accessDB(new IConnVisitor() {

			public Object visit(Connection conn) throws SQLException {
				QueryRunner runner = new QueryRunner();
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
}