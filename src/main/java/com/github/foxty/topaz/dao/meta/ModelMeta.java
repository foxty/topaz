package com.github.foxty.topaz.dao.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.github.foxty.topaz.annotation.Column;
import com.github.foxty.topaz.annotation.Model;
import com.github.foxty.topaz.annotation.Relation;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.dao.DaoException;
import com.github.foxty.topaz.dao.Models;

/**
 * Created by itian on 6/26/2017.
 */
public class ModelMeta {

	private Class<?> modelClazz;
	private Model _model;

	// All model's column definitions, key is the filed name and colum name
	private Map<String, ColumnMeta> columnMetaMap;
	private List<ColumnMeta> columns;

	// Current model's relations
	private Map<String, RelationMeta> relationMetaMap;

	public ModelMeta(Class<?> modelClazz) {
		this.modelClazz = modelClazz;
		this._model = modelClazz.getAnnotation(Model.class);

		extractAnnotations();
	}

	private void extractAnnotations() {

		List<Field> allFields = new ArrayList<Field>();
		Map<String, ColumnMeta> columnMap = new HashMap<>();
		Map<String, RelationMeta> relationMap = new HashMap<>();
		List<ColumnMeta> columns = new ArrayList<>();
		Class<?> curClazz = modelClazz;
		while (curClazz != null) {
			allFields.addAll(Arrays.asList(curClazz.getDeclaredFields()));
			curClazz = curClazz.getSuperclass();
		}
		allFields.stream().filter(f -> f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Relation.class))
				.forEach(f -> {
					String fieldName = f.getName();
					String readMethodName = (f.getType() == boolean.class || f.getType() == Boolean.class ? "is"
							: "get") + StringUtils.capitalize(fieldName);
					String writeMethodName = "set" + StringUtils.capitalize(fieldName);

					Method readMethod = null;
					Method writeMethod = null;
					try {
						readMethod = modelClazz.getMethod(readMethodName, new Class[] {});
						writeMethod = modelClazz.getMethod(writeMethodName, f.getType());
					} catch (Exception e) {
						throw new DaoException(e);
					}
					Column column = f.getAnnotation(Column.class);
					if (column != null) {
						ColumnMeta cm = new ColumnMeta(column, getTableName(), fieldName, f.getType(), readMethod,
								writeMethod);
						columnMap.put(cm.getFieldName(), cm);
						if (!cm.isNameConsistent()) {
							columnMap.put(cm.getColumnName(), cm);
						}
						columns.add(cm);
					}
					Relation relation = f.getAnnotation(Relation.class);
					if (relation != null) {
						relationMap.put(fieldName, new RelationMeta(relation, modelClazz, fieldName, f.getType(),
								readMethod, writeMethod));
					}
				});
		columnMetaMap = Collections.unmodifiableMap(columnMap);
		relationMetaMap = Collections.unmodifiableMap(relationMap);
		this.columns = Collections.unmodifiableList(columns);
	}

	public String getTableName() {
		return TopazUtil.camel2flat(modelClazz.getSimpleName());
	}

	public Map<String, ColumnMeta> getColumnMetaMap() {
		return columnMetaMap;
	}

	public List<ColumnMeta> getColumns() {
		return columns;
	}

	/**
	 * Find column meta info from model columns or relation model's columns. It
	 * can be distinguished by the format of key. - if key contains "." then
	 * means should get from relational models - else means should get from
	 * current module's columns.
	 *
	 * @param key
	 *            Should be the field name(by default) or column name(which may
	 *            have case sensitive)
	 * @return ColumnMeta
	 */
	public ColumnMeta findColumnMeta(String key) {
		ColumnMeta cm = null;
		if (key.contains(".")) {
			// This is a predication on relation tables.
			String[] props = key.split("\\.");
			RelationMeta rm = findRealtionMeta(props[0]);
			ModelMeta rmm = Models.getInstance().getModelMeta(rm.getFieldClazz());
			cm = rmm.findColumnMeta(props[1]);
		} else {
			cm = columnMetaMap.get(key);
		}
		return cm;
	}

	public ColumnMeta getColumnMeta(String key) {
		ColumnMeta cm = findColumnMeta(key);
		if (cm == null) {
			throw new DaoException("No definition for column: " + key + " of " + this.getTableName());
		}
		return cm;
	}

	/**
	 * Find the relation metadata, key must be the field name.
	 *
	 * @param key
	 *            field name defined in model
	 * @return RelationMeta
	 */
	public RelationMeta findRealtionMeta(String key) {
		RelationMeta rm = relationMetaMap.get(key);
		if (rm == null) {
			throw new DaoException("No definition for relation: " + key + " of " + this.getTableName());
		}
		return rm;
	}

	public Map<String, RelationMeta> getRelationMetaMap() {
		return relationMetaMap;
	}
}
