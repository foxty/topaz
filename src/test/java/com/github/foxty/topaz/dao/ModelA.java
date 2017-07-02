package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Model;
import com.github.foxty.topaz.annotation._Relation;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by itian on 6/22/2017.
 */
@_Model
public class ModelA extends Model {

    @_Column
    private String name;

    @_Column
    private Integer score;

    @_Column
    private LocalDateTime bornDate;

    @_Relation
    private ModelB modelb;

    @_Relation(relation = Relation.HasMany, model = ModelC.class)
    private ArrayList<ModelA> modelcList;

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

    public List<ModelA> getModelcList() {
        return modelcList;
    }

    public void setModelcList(ArrayList<ModelA> modelcList) {
        this.modelcList = modelcList;
    }
}
