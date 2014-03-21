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
import com.topaz.common.DataChecker;

/**
 * @author Isaac Tian
 */
public class Controller {
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
				.getModuleName()
				+ "/"
				+ ctx.getControllerName()
				+ "/"
				+ resourceName;
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
			} else {
				targetRes = ctx.getViewBase() + DEF_LAYOUT;
				log.warn("Layout " + layoutName
						+ " not exist, now using default layout" + DEF_LAYOUT);
			}
			request.setAttribute(LAYOUT_CHILDREN, ctx.getViewBase() + resPath);
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
	 * Get request parameter by paramKey and validate by regex.
	 * 
	 * @param paramKey
	 * @param regex
	 * @param errMsg
	 */
	protected void vRegex(String paramKey, String regex, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.regexTest(regex, value)) {
			addError(paramKey, errMsg);
		}
	}

	/**
	 * Get request parameter by paramKey and validate by range of length.
	 * Inclusion.
	 * 
	 * @param paramKey
	 * @param minLength
	 * @param maxLength
	 * @param errMsg
	 */
	protected void vRangeLength(String paramKey, int minLength, int maxLength,
			String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.isSafeString(value, minLength, maxLength, null)) {
			addError(paramKey, errMsg);
		}
	}

	protected void vMinLength(String paramKey, int minlength, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (StringUtils.trimToEmpty(value).length() < minlength) {
			addError(paramKey, errMsg);
		}
	}

	protected void vMaxLength(String paramKey, int maxlength, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (StringUtils.trimToEmpty(value).length() > maxlength) {
			addError(paramKey, errMsg);
		}
	}

	protected void vInt(String paramKey, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.isInt(value)) {
			addError(paramKey, errMsg);
		}
	}

	protected void vDate(String paramKey, String format, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.isDate(value, format)) {
			addError(paramKey, errMsg);
		}
	}

	protected void vEmail(String paramKey, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.isEmail(value)) {
			addError(paramKey, errMsg);
		}
	}

	protected void vCellphone(String paramKey, String errMsg) {
		String value = WebContext.get().parameter(paramKey);
		if (!DataChecker.isCellphone(value)) {
			addError(paramKey, errMsg);
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
	protected boolean isInputValid() {
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