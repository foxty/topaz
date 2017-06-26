package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Model;
import com.github.foxty.topaz.annotation._Relation;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.dao.DaoException;
import com.github.foxty.topaz.dao.Model;
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
    private Map<String, ColumnMeta> columnMetaMap;
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
                    String propName = f.getName();
                    String readMethodName = (f.getType() == boolean.class
                            || f.getType() == Boolean.class ? "is" : "get")
                            + StringUtils.capitalize(propName);
                    String writeMethodName = "set" + StringUtils.capitalize(propName);

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
                        columns.put(propName, new ColumnMeta(column, propName,
                                f.getType(), readMethod, writeMethod));
                    }
                    _Relation relation = f.getAnnotation(_Relation.class);
                    if (relation != null) {
                        relations.put(propName, new RelationMeta(relation, getTableName() + "_id",
                                propName, f.getType(), readMethod, writeMethod));
                    }
                });
        columnMetaMap = Collections.unmodifiableMap(columns);
        relationMetaMap = Collections.unmodifiableMap(relations);
    }

    public String getTableName() {
        if (null == _model || StringUtils.isBlank(_model.tableName())) {
            return TopazUtil.camel2flat(modelClazz.getSimpleName());
        } else {
            return _model.tableName();
        }
    }

    public Map<String, ColumnMeta> getColumnMetaMap() {
        return columnMetaMap;
    }

    public ColumnMeta getColumnMeta(String key) {
        return columnMetaMap.get(key);
    }

    public Map<String, RelationMeta> getRelationMetaMap() {
        return relationMetaMap;
    }

    public RelationMeta getRelationMeta(String propName) {
        return relationMetaMap.get(propName);
    }
}