package com.github.foxty.topaz.dao.meta;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.common.TopazUtil;

/**
 * Created by itian on 6/26/2017.
 */
public class ColumnMeta extends FieldMeta {
	private String tableName;
	private _Column column;
	private String columnName;
	private boolean consistent;

	public ColumnMeta(_Column column, String tableName, String fieldName, Class<?> fieldClazz, Method readMethod,
			Method writeMethod) {
		super(fieldClazz, readMethod, writeMethod, fieldName);
		this.tableName = tableName;
		this.column = column;
		this.columnName = StringUtils.isNotBlank(column.name()) ? column.name() : TopazUtil.camel2flat(getFieldName());
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public boolean isNameConsistent() {
		return consistent;
	}
}
