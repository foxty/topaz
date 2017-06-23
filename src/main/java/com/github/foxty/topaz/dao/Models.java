package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Model;
import com.github.foxty.topaz.annotation._Relation;
import com.github.foxty.topaz.common.TopazUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model registration and cache model mappings(Column mapping and Relation mapping)
 * <p>
 * Created by itian on 6/22/2017.
 */
public class Models {

    private static Log log = LogFactory.getLog(Models.class);
    private static Models INSTANCE = new Models();

    public static Models getInstance() {
        return INSTANCE;
    }

    private Map<String, ModelMeta> modelMetaMap = new ConcurrentHashMap<>();

    private Models() {
    }

    public void register(Class... modelClazzs) {
        for (Class modelClazz : modelClazzs) {
            String key = modelClazz.getName();
            if (!modelMetaMap.containsKey(key)) {
                ModelMeta existMapping = modelMetaMap.putIfAbsent(modelClazz.getName(), new ModelMeta(modelClazz));
                if (null != existMapping) {
                    log.warn("Duplicate registration for model " + modelClazz);
                }
            }
        }
    }

    public ModelMeta getModelMeta(Class modelClazz) {
        if (!modelMetaMap.containsKey(modelClazz.getName())) {
            register(modelClazz);
        }
        return modelMetaMap.get(modelClazz.getName());
    }
}

class ModelMeta {

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
                        relations.put(propName, new RelationMeta(relation, propName,
                                f.getType(), readMethod, writeMethod));
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

class FieldMeta {

    protected String fieldName;
    protected Class fieldClazz;
    protected Method readMethod;
    protected Method writeMethod;

    public FieldMeta(Class fieldClazz, Method readMethod, Method writeMethod, String fieldName) {
        this.fieldClazz = fieldClazz;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<? extends  Model> getFieldClazz() {
        return fieldClazz;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }
}


class ColumnMeta extends FieldMeta {
    private _Column column;

    public ColumnMeta(_Column column, String fieldName, Class fieldClazz,
                      Method readMethod, Method writeMethod) {
        super(fieldClazz, readMethod, writeMethod, fieldName);
        this.column = column;
    }

    public String getColumnName() {
        if (StringUtils.isBlank(column.name())) {
            return TopazUtil.camel2flat(getFieldName());
        } else {
            return column.name();
        }
    }
}

class RelationMeta extends FieldMeta {
    private _Relation relation;

    public RelationMeta(_Relation relation, String fieldName, Class fieldClazz, Method readMethod, Method writeMethod) {
        super(fieldClazz, readMethod, writeMethod, fieldName);
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation.relation();
    }

    public String byKey() {
        return null;
    }
}
