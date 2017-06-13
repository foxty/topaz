/**
 * 创建日期 2008-3-14
 *
 * 作者 Foxty
 * 
 * 描述 控制器基类
 */
package com.github.foxty.topaz.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

/**
 * @author Isaac Tian
 */
public class BaseController {
	private final static int MIN_PAGESIZE = 1;
	private final static int DEF_PAGESIZE = 10;
	private final static int MAX_PAGESIZE = Integer.MAX_VALUE;
	private static String WEB_ERRORS = "errors";
	private static String LAYOUT_CHILDREN = "children";
	private static Log log = LogFactory.getLog(BaseController.class);

	private String layout = "layout.ftl";

	final protected void setLayout(String layout) {
		this.layout = layout;
	}

	/**
	 * Test a path is a absolute path or relative path.
	 * 
	 * @param resPath
	 * @return
	 */
	final private boolean isAbsolutePath(String resPath) {
		return resPath != null
				&& (resPath.startsWith("/") || resPath.startsWith("\\"));
	}

	protected Pagination genPagination() {
		int pageSize = validInt("pageSize", DEF_PAGESIZE);
		return genPagination(pageSize);
	}

	protected Pagination genPagination(int pageSize) {

		pageSize = pageSize <= 0 ? DEF_PAGESIZE : pageSize;
		int page = validInt("page", 1);
		return new Pagination(pageSize, page);
	}

	protected void renderWithoutLayout(String resName) {
		render(null, resName);
	}

	public void render(String resName) {
		render(layout, resName);
	}

	protected void render(String layoutName, String resourceName) {

		WebContext wc = WebContext.get();
		HttpServletRequest request = wc.getRequest();
		HttpServletResponse response = wc.getResponse();

		request.setAttribute(WEB_ERRORS, wc.getErrors());
		String resPath = isAbsolutePath(resourceName) ? resourceName : wc
				.getModuleName()
				+ "/"
				+ wc.getControllerName()
				+ "/"
				+ resourceName;
		File resFile = new File(wc.getApplication().getRealPath(
				wc.getViewBase() + resPath));
		if (!resFile.exists()) {
			log.error("Can't find resource " + resFile);
			try {
				response.sendError(404);
			} catch (IOException e1) {
			}
			return;
		}
		// Find the layout if exist
		String targetRes = wc.getViewBase() + resPath;
		if (null != layoutName) {
			String layoutResPath = wc.getApplication().getRealPath(
					wc.getViewBase() + layoutName);
			File layoutFile = new File(layoutResPath);
			if (layoutFile.exists()) {
				targetRes = wc.getViewBase() + layoutName;
			} else {
				targetRes = wc.getViewBase() + layout;
				log.warn("Layout " + layoutName
						+ " not exist, now using default layout" + layout);
			}
			request.setAttribute(LAYOUT_CHILDREN, wc.getViewBase() + resPath);
		}

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString(), e);
		}
		if (log.isDebugEnabled())
			log.debug("Render  " + targetRes + ", resource " + resourceName);
		RequestDispatcher rd = wc.getApplication().getRequestDispatcher(
				targetRes);
		try {
			rd.include(request, response);
		} catch (Exception e) {
			throw new ControllerException(e);
		}

		wc.clearFlash();
	}

	protected void renderJSON(Object object) {
		WebContext ctx = WebContext.get();
		HttpServletResponse response = ctx.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		String json = JSON.toJSONString(object);
		try {
			response.getWriter().write(json);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}

	protected void renderText(String text) {
		WebContext ctx = WebContext.get();
		HttpServletResponse response = ctx.getResponse();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		try {
			response.getWriter().write(text);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}

	protected void redirect(String resourcePath) {
		WebContext ctx = WebContext.get();
		HttpServletResponse response = ctx.getResponse();
		try {
			response.sendRedirect(resourcePath);
		} catch (IOException e) {
			log.error(e.toString(), e);
		}
	}

	protected Validation v(String paramKey, String errMsg) {
		return new Validation(paramKey, errMsg);
	}

	protected Validation v(String paramKey) {
		return new Validation(paramKey, paramKey + " is not a valid value!");
	}

	protected void addError(String key, String msg) {
		WebContext.get().getErrors().put(key, msg);
	}

	/**
	 * 检查是否所有数据均通过验证
	 * 
	 * @return boolean
	 */
	protected boolean isInputValid() {
		return WebContext.get().getErrors().isEmpty();
	}

	protected int validInt(String paramKey, int defaultValue) {
		String value = WebContext.get().param(paramKey);
		int v = StringUtils.isNumeric(value) ? Integer.parseInt(value)
				: defaultValue;
		return v;
	}

	protected long validLong(String paramKey, long defaultValue) {
		String value = WebContext.get().param(paramKey);
		long v = StringUtils.isNumeric(value) ? Long.parseLong(value)
				: defaultValue;
		return v;
	}
}