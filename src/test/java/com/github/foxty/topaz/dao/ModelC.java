package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;
import com.github.foxty.topaz.annotation._Relation;

import java.time.LocalDateTime;

/**
 * Created by itian on 6/22/2017.
 */
public class ModelC extends Model {

    @_Column
    private String name;

    @_Column
    private LocalDateTime createdAt;

    @_Column
    private Integer modelAId;

    @_Relation(relation = Relation.BelongsTo)
    private ModelA parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getModelAId() {
        return modelAId;
    }

    public void setModelAId(Integer modelAId) {
        this.modelAId = modelAId;
    }

    public ModelA getParent() {
        return parent;
    }

    public void setParent(ModelA parent) {
        this.parent = parent;
    }
}
