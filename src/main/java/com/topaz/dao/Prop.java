package com.topaz.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Prop {
	public static enum Type {
		Column,Table
	}
	public static enum Relation {
		HasOne, HasMany, BelongsTo
	}
	Type type() default Prop.Type.Column;
	Relation relation() default Prop.Relation.HasOne;
	String targetName() default "";
}