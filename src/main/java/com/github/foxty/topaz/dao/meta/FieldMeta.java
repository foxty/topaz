package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.dao.Model;

import java.lang.reflect.Method;

/**
 * Created by itian on 6/26/2017.
 */
public class FieldMeta {

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

    public Class<? extends Model> getFieldClazz() {
        return fieldClazz;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }
}
