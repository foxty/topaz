package com.github.foxty.topaz.dao.sql;

/**
 * Created by itian on 6/27/2017.
 */
public enum Operators {
    EQ(" = "), NE(" <> "), LT(" < "), GT(" > "), LE(" <= "), GE(" >= "), IN(" in "), LK(
            " like "), IS(" is ");

    private String value;

    private Operators(String v) {
        this.value = v;
    }

    public String getValue() {
        return value;
    }

}
