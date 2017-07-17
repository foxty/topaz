package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.annotation._Relation;
import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.dao.Model;
import com.github.foxty.topaz.dao.Relation;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by itian on 6/26/2017.
 */
public class RelationMeta extends FieldMeta {
    private _Relation relation;
    private Class<?> baseClazz;

    public RelationMeta(_Relation relation, Class<?> baseClazz, String fieldName, Class<?> fieldClazz, Method readMethod, Method writeMethod) {
        super(fieldClazz, readMethod, writeMethod, fieldName);
        this.relation = relation;
        this.baseClazz = baseClazz;
    }

    public Relation getRelation() {
        return relation.relation();
    }

    public Class<?> getModelClazz() {
        return (relation.model() == Model.class) ? getFieldClazz() : relation.model();
    }

    public String byKey() {
        if (StringUtils.isNotBlank(relation.byKey())) {
            return relation.byKey();
        } else {
            String by = "";
            switch (relation.relation()) {
                case HasMany:
                case HasOne:
                    by = TopazUtil.camel2flat(baseClazz.getSimpleName()) + "_id";
                    break;
                case BelongsTo:
                    by = TopazUtil.camel2flat(getModelClazz().getSimpleName()) + "_id";
                    break;
            }
            return by;
        }
    }
}
