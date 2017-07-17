package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.annotation._Column;

import java.time.LocalDateTime;

/**
 * Created by itian on 6/22/2017.
 */
public class ModelB extends Model {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@_Column
    private String name;

    @_Column
    private LocalDateTime expiredDateOn;

    @_Column
    private Integer modelAId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getExpiredDateOn() {
        return expiredDateOn;
    }

    public void setExpiredDateOn(LocalDateTime expiredDateOn) {
        this.expiredDateOn = expiredDateOn;
    }

    public Integer getModelAId() {
        return modelAId;
    }

    public void setModelAId(Integer modelAId) {
        this.modelAId = modelAId;
    }
}
