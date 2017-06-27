package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.Models;
import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.meta.RelationMeta;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by itian on 6/27/2017.
 */
public class Clause {

    protected ModelMeta modelMeta;
    protected StringBuffer clause = new StringBuffer();
    protected List<Object> params = new LinkedList<>();

    public Clause(ModelMeta modelMeta) {
        this.modelMeta = modelMeta;
    }

    public StringBuffer getClause() {
        return clause;
    }

    public List<Object> getParams() {
        return params;
    }
}
