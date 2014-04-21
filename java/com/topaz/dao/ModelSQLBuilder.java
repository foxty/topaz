package com.topaz.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.topaz.common.TopazUtil;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
abstract public class ModelSQLBuilder<T> {

	public static enum OP {
		EQ(" = "), NE(" != "), LT(" < "), GT(" > "), LE(" <= "), GE(" >= "), LK(" like ");

		private String value;

		private OP(String v) {
			this.value = v;
		}

		public String getValue() {
			return value;
		}

	}

	@SuppressWarnings("rawtypes") protected Class baseModelClazz;
	protected final String baseTableName;

	protected StringBuffer sql = new StringBuffer();
	protected List<Object> sqlParams = new ArrayList<Object>();

	public ModelSQLBuilder(Class<? extends BaseModel> clazz) {
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
	}

	public ModelSQLBuilder(Class<? extends BaseModel> clazz, String sql, List<Object> sqlParams,
			SQLBuilderType type) {
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
		this.sqlParams.addAll(sqlParams);
	}

	abstract protected void buildSQL();

	protected PropMapping findProp(String prop) {
		Map<String, PropMapping> mapping = BaseModel.MODEL_PROPS.get(baseModelClazz);
		PropMapping pm = mapping.get(prop);
		if (pm == null) {
			throw new DaoException("No column mapping found for property "
					+ prop + "!");
		}
		return pm;
	}

	public T where(String propName, Object value) {
		return where(propName, OP.EQ, value);
	}

	public T where(String propName, OP op, Object value) {
		PropMapping pm = findProp(propName);
		sql.append(" WHERE ").append(baseTableName + ".").append(pm.getTargetName())
				.append(op.getValue()).append("? ");
		sqlParams.add(value);
		return (T) this;
	}

	public T and(String prop, Object value) {
		return and(prop, OP.EQ, value);
	}

	public T and(String prop, OP op, Object value) {
		PropMapping pm = findProp(prop);
		sql.append(" AND ").append(baseTableName).append(".").append(pm.getTargetName())
				.append(op.getValue())
				.append("? ");
		sqlParams.add(value);
		return (T) this;
	}

	public T or(String prop, Object value) {
		return or(prop, OP.EQ, value);
	}

	public T or(String prop, OP op, Object value) {
		PropMapping pm = findProp(prop);
		sql.append(" OR ").append(baseTableName).append(".").append(pm.getTargetName())
				.append(op.getValue())
				.append("? ");
		sqlParams.add(value);
		return (T) this;
	}

	@Override
	public String toString()
	{
		return sql.toString();
	}
}