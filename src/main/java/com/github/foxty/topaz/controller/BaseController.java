/**
 * 创建日期 2008-3-14
 *
 * 作者 Foxty
 * 
 * 描述 控制器基类
 */
package com.github.foxty.topaz.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Isaac Tian
 */
public class BaseController {
	private final static int MIN_PAGESIZE = 1;
	private final static int DEF_PAGESIZE = 10;
	private final static int MAX_PAGESIZE = Integer.MAX_VALUE;

	private static Log log = LogFactory.getLog(BaseController.class);

	private String layout = "layout.ftl";

	final protected void setLayout(String layout) {
		this.layout = layout;
	}
}