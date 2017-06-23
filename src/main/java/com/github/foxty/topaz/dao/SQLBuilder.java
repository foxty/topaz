package com.github.foxty.topaz.dao;

import java.util.ArrayList;
import java.util.List;

import com.github.foxty.topaz.common.TopazUtil;

/**
 * Not thread safe!
 * 
 * @author foxty
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
abstract public class SQLBuilder<T extends SQLBuilder> {

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
	protected ModelMeta modelMeta;

	protected StringBuffer sql = new StringBuffer();
	protected List<Object> sqlParams = new ArrayList<Object>();

	public SQLBuilder(Class<? extends Model> clazz) {
		Models.getInstance().register(clazz);
		modelMeta = Models.getInstance().getModelMeta(clazz);
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
	}

	public SQLBuilder(Class<? extends Model> clazz, String sql, List<Object> sqlParams) {
		Models.getInstance().register(clazz);
		modelMeta = Models.getInstance().getModelMeta(clazz);
		this.baseModelClazz = clazz;
		baseTableName = TopazUtil.camel2flat(baseModelClazz.getSimpleName());
		this.sqlParams.addAll(sqlParams);
	}

	abstract protected void buildSQL();

	protected ColumnMeta getColumnMapping(String prop) {
		ColumnMeta cm = modelMeta.getColumnMetaMap().get(prop);
		if (cm == null) {
			throw new DaoException("No column mapping found for property "
					+ baseModelClazz.getName() + "."
					+ prop + "!");
		}
		return cm;
	}

	public T c(String prop, Object value) {
		return c(prop, OP.EQ, value);
	}

	public T c(String prop, OP op, Object value) {
		ColumnMeta pm = getColumnMapping(prop);
		sql.append(" " + baseTableName + ".").append(pm.getColumnName())
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
		ColumnMeta pm = getColumnMapping(propName);
		sql.append(" WHERE ").append(baseTableName + ".").append(pm.getColumnName())
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