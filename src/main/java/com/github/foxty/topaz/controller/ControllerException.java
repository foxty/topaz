/**
 * 创建日期 2008-3-17
 *
 * 作者 Foxty
 * 
 * 描述 框架控制器核心异常。uncheck
 */
package com.github.foxty.topaz.controller;

/**
 * @author gzd1x2
 * 
 * 
 */
public class ControllerException extends com.github.foxty.topaz.common.TopazException {
	/**
	 */
	private static final long serialVersionUID = 1L;

	public ControllerException(String info) {
		super(info);
	}

	public ControllerException(Throwable cause) {
		super(cause);
	}

	public ControllerException(String info, Throwable cause) {
		super(info, cause);
	}
}
