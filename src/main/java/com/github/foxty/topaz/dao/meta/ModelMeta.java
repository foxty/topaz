package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Model;
import com.github.foxty.topaz.annotation._Relation;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.dao.DaoException;
import com.github.foxty.topaz.dao.Model;
import com.github.foxty.topaz.dao.Models;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by itian on 6/26/2017.
 */
public class ModelMeta {

    private Class<? extends Model> modelClazz;
    private _Model _model;

    // All model's column definitions, key is the filed name
    private Map<String, ColumnMeta> columnMetaMap;

    // Current model's relations
    private Map<String, RelationMeta> relationMetaMap;

    public ModelMeta(Class<? extends Model> modelClazz) {
        this.modelClazz = modelClazz;
        this._model = modelClazz.getAnnotation(_Model.class);

        extractAnnotations();
    }

    private void extractAnnotations() {

        List<Field> allFields = new ArrayList<Field>();
        Map<String, ColumnMeta> columns = new HashMap<>();
        Map<String, RelationMeta> relations = new HashMap<>();
        Class<?> curClazz = modelClazz;
        while (curClazz != null) {
            allFields.addAll(Arrays.asList(curClazz.getDeclaredFields()));
            curClazz = curClazz.getSuperclass();
        }
        allFields.stream().filter(f -> f.isAnnotationPresent(_Column.class) || f.isAnnotationPresent(_Relation.class))
                .forEach(f -> {
                    String fieldName = f.getName();
                    String readMethodName = (f.getType() == boolean.class
                            || f.getType() == Boolean.class ? "is" : "get")
                            + StringUtils.capitalize(fieldName);
                    String writeMethodName = "set" + StringUtils.capitalize(fieldName);

                    Method readMethod = null;
                    Method writeMethod = null;
                    try {
                        readMethod = modelClazz.getMethod(readMethodName, new Class[]{});
                        writeMethod = modelClazz.getMethod(writeMethodName, f.getType());
                    } catch (Exception e) {
                        throw new DaoException(e);
                    }
                    _Column column = f.getAnnotation(_Column.class);
                    if (column != null) {
                        columns.put(fieldName,
                                new ColumnMeta(column, getTableName(),
                                        fieldName, f.getType(),
                                        readMethod, writeMethod));
                    }
                    _Relation relation = f.getAnnotation(_Relation.class);
                    if (relation != null) {
                        relations.put(fieldName,
                                new RelationMeta(relation, modelClazz,
                                        fieldName, f.getType(),
                                        readMethod, writeMethod));
                    }
                });
        columnMetaMap = Collections.unmodifiableMap(columns);
        relationMetaMap = Collections.unmodifiableMap(relations);
    }

    public String getTableName() {
        return TopazUtil.camel2flat(modelClazz.getSimpleName());
    }

    public Map<String, ColumnMeta> getColumnMetaMap() {
        return columnMetaMap;
    }

    /**
     * Find column meta info from model columns or relation model's columns.
     * It can be distinguished by the format of key.
     * - if key contains "." then means should get from relational models
     * - else means should get from current module's columns.
     *
     * @param key Should be the field name(by default) or
     *            column name(which may have case sensitive)
     * @return
     */
    public ColumnMeta findColumnMeta(String key) {
        ColumnMeta cm = null;
        if (key.contains(".")) {
            //This is a predication on relation tables.
            String[] props = key.split("\\.");
            RelationMeta rm = findRealtionMega(props[0]);
            ModelMeta rmm = Models.getInstance().getModelMeta(rm.getFieldClazz());
            cm = rmm.findColumnMeta(props[1]);
        } else {
            cm = columnMetaMap.get(key);
        }
        if (cm == null) {
            throw new DaoException("No definition for column: " + key + " of " + this.getTableName());
        }
        return cm;
    }

    /**
     * Find the relation metadata, key must be the field name or table name.
     *
     * @param key
     * @return
     */
    public RelationMeta findRealtionMega(String key) {
        RelationMeta rm =  relationMetaMap.get(key);
        if (rm == null) {
            throw new DaoException("No definition for relation: " + key + " of " + this.getTableName());
        }
        return rm;
    }

    public Map<String, RelationMeta> getRelationMetaMap() {
        return relationMetaMap;
    }
}
