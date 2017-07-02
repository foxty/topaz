package com.github.foxty.topaz.annotation;

import com.github.foxty.topaz.dao.Model;
import com.github.foxty.topaz.dao.Relation;

import javax.lang.model.element.Element;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate relationship between tables, currently three relation suporte:
 *  - One to One
 *  - One to Many
 *  - Many to Many
 *
 * Created by itian on 6/22/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface _Relation {
    Relation relation() default Relation.HasOne;
    Class<? extends Model> model() default Model.class;
    String byKey() default "";
}
