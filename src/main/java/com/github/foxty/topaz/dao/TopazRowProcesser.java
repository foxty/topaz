package com.github.foxty.topaz.dao;

import org.apache.commons.dbutils.BasicRowProcessor;

public class TopazRowProcesser extends BasicRowProcessor {
	
	public TopazRowProcesser() {
		super(new TopazBeanProcesser());
	}

}
