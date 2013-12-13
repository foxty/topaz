/**
 * 创建日期 2008-3-14
 *
 * 作者 Foxty
 * 
 * 描述 控制器基类
 */
package com.topaz.controller;

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
public class Controller {
	protected static final int V_REGEX = 1;
	protected static final int V_LENGTH = 2;
	protected static final int V_INCLUEITION = 3;
	protected static final int V_EXCLUETION = 4;
	protected static final int V_EMAIL = 5;

	private static String WEB_ERRORS = "errors";
	private static String DEF_LAYOUT = "layout.ftl";
	private static String LAYOUT_CHILDREN = "children";

	private static Log log = LogFactory.getLog(Controller.class);

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

	public void render(String resName) {
		render(DEF_LAYOUT, resName);
	}

	public void renderWithoutLayout(String resName) {
		render(null, resName);
	}

	protected void render(String layoutName, String resourceName) {

		WebContext ctx = WebContext.get();
		HttpServletRequest request = ctx.getRequest();
		HttpServletResponse response = ctx.getResponse();

		request.setAttribute(WEB_ERRORS, ctx.getErrors());
		String resPath = isAbsolutePath(resourceName) ? resourceName : ctx
				.getControllerName() + "/" + resourceName;
		File resFile = new File(ctx.getApplication().getRealPath(
				ctx.getViewBase() + resPath));
		if (!resFile.exists()) {
			log.error("Can't find resource " + resFile);
			try {
				response.sendError(404);
			} catch (IOException e1) {
			}
			return;
		}
		// Find the layout if exist
		String targetRes = ctx.getViewBase() + resPath;
		if (null != layoutName) {
			String layoutResPath = ctx.getApplication().getRealPath(
					ctx.getViewBase() + layoutName);
			File layoutFile = new File(layoutResPath);
			if (layoutFile.exists()) {
				targetRes = ctx.getViewBase() + layoutName;
				request.setAttribute(LAYOUT_CHILDREN, resPath);
			}
		}

		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.toString(), e);
		}
		if (log.isDebugEnabled())
			log.debug("Render  " + targetRes + ", resource " + resourceName);
		RequestDispatcher rd = ctx.getApplication().getRequestDispatcher(
				targetRes);
		try {
			rd.include(request, response);
		} catch (Exception e) {
			log.error(e.toString(), e);
			throw new ControllerException(e);
		}
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

	/**
	 * 验证方法
	 * 
	 * @param data
	 * @param params
	 * @param errMsg
	 */
	protected void validate(int type, Object[] params, String key, String errMsg) {
		switch (type) {
		case V_REGEX:
			if (!DataChecker.chkStr((String) params[0], (String) params[1])) {
				addError(key, errMsg);
			}
			break;
		case V_LENGTH:
			if (!DataChecker.isSafe((String) params[0], (Integer) params[1],
					(Integer) params[2], null)) {
				addError(key, errMsg);
			}
			break;
		case V_INCLUEITION:
			Object[] arr1 = (Object[]) params[0];
			boolean res1 = false;
			for (Object obj : arr1) {
				if (obj.equals(params[1])) {
					res1 = true;
				}
			}
			if (!res1) {
				addError(key, errMsg);
			}
			break;
		case V_EXCLUETION:
			Object[] arr2 = (Object[]) params[0];
			boolean res2 = true;
			for (Object obj : arr2) {
				if (obj.equals(params[1])) {
					res2 = false;
				}
			}
			if (!res2) {
				addError(key, errMsg);
			}
			break;
		case V_EMAIL:
			boolean re = DataChecker.isEmail((String) params[0]);
			if (!re) {
				addError(key, errMsg);
			}
			break;
		}
	}

	protected void addError(String key, String msg) {
		WebContext.get().getErrors().put(key, msg);
	}

	/**
	 * 检查是否所有数据均通过验证
	 * 
	 * @return boolean
	 */
	protected boolean isValid() {
		return WebContext.get().getErrors().isEmpty();
	}

	protected int validateInt(String strInt, int defaultValue) {
		int v = StringUtils.isNumeric(strInt) ? Integer.parseInt(strInt)
				: defaultValue;
		return v;
	}

	protected long validateLong(String strLong, long defaultValue) {
		long v = StringUtils.isNumeric(strLong) ? Long.parseLong(strLong)
				: defaultValue;
		return v;
	}
}