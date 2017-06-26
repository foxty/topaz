package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.common.TopazUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by itian on 6/26/2017.
 */
public class ColumnMeta extends FieldMeta {
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
