package com.github.foxty.topaz.dao.meta;

import com.github.foxty.topaz.annotation._Relation;
import com.github.foxty.topaz.dao.Relation;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by itian on 6/26/2017.
 */
public class RelationMeta extends FieldMeta {
    private _Relation relation;
    private String defaultBykey;

    public RelationMeta(_Relation relation, String defaultBykey, String fieldName, Class fieldClazz, Method readMethod, Method writeMethod) {
        super(fieldClazz, readMethod, writeMethod, fieldName);
        this.relation = relation;
        this.defaultBykey = defaultBykey;
    }

    public Relation getRelation() {
        return relation.relation();
    }

    public String byKey() {
        if (StringUtils.isNotBlank(relation.byKey())) {
            return relation.byKey();
        } else {
            return defaultBykey;
        }
    }
}
