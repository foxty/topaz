package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Model;
import com.github.foxty.topaz.annotation._Relation;

import java.time.LocalDateTime;

/**
 * Created by itian on 6/22/2017.
 */
@_Model(tableName = "table_name_a")
public class ModelA extends Model {

    @_Column(name = "aname")
    private String name;

    @_Column
    private Integer score;

    @_Column
    private LocalDateTime bornDate;

    @_Relation
    private ModelB modelb;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getBornDate() {
        return bornDate;
    }

    public void setBornDate(LocalDateTime bornDate) {
        this.bornDate = bornDate;
    }

    public ModelB getModelb() {
        return modelb;
    }

    public void setModelb(ModelB modelb) {
        this.modelb = modelb;
    }

}
