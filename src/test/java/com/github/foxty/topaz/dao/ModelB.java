package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by itian on 6/22/2017.
 */
public class ModelB extends Model {

    @_Column
    private String name;

    @_Column(name = "expired_date_on")
    private LocalDateTime expiredAt;

    @_Column
    private Integer modelaId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Integer getModelaId() {
        return modelaId;
    }

    public void setModelaId(Integer modelaId) {
        this.modelaId = modelaId;
    }
}
