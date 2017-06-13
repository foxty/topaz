package com.github.foxty.topaz.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.foxty.topaz.common.TopazUtil;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
abstract public class ModelSQLBuilder<T extends ModelSQLBuilder> {

	public static enum OP {
		EQ(" = "), NE(" != "), LT(" < "), GT(" > "), LE(" <= "), GE(" >= "), IN(" in "), LK(
				" like "), IS(" is ");

		private String value;

		private OP(String v) {
			this.value = v;
		}

		public String getValue() {
			return value;
		}

	}

	protected Class baseModelClazz;
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
					+ baseModelClazz.getName() + "."
					+ prop + "!");
		}
		return pm;
	}

	public T c(String prop, Object value) {
		return c(prop, OP.EQ, value);
	}

	public T c(String prop, OP op, Object value) {
		PropMapping pm = findProp(prop);
		sql.append(" " + baseTableName + ".").append(pm.getTargetName())
				.append(op.getValue()).append("? ");
		sqlParams.add(value);
		return (T) this;
	}

	public T and() {
		sql.append(" AND ");
		return (T) this;
	}

	public T or() {
		sql.append(" OR ");
		return (T) this;
	}

	public T bracketStart() {
		sql.append(" ( ");
		return (T) this;
	}

	public T bracketEnd() {
		sql.append(" ) ");
		return (T) this;
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
		and().c(prop, op, value);
		return (T) this;
	}

	public T or(String prop, Object value) {
		return or(prop, OP.EQ, value);
	}

	public T or(String prop, OP op, Object value) {
		or().c(prop, op, value);
		return (T) this;
	}

	@Override
	public String toString()
	{
		return sql.toString();
	}
}