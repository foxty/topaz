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
import java.util.Arrays;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.topaz.common.DataChecker;
import com.topaz.common.TopazUtil;

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

	protected void renderWithoutLayout(String resName) {
		render(null, resName);
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
				targetRes = wc.getViewBase() + DEF_LAYOUT;
				log.warn("Layout " + layoutName
						+ " not exist, now using default layout" + DEF_LAYOUT);
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
			log.error(e.toString(), e);
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

	/**
	 * Get request parameter by paramKey and validate by regex.
	 * 
	 * @param paramKey
	 * @param regex
	 * @param errMsg
	 */
	protected String vRegex(String paramKey, String regex, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.regexTest(regex, value)) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}

	protected String vRegex(String pk, String regex) {
		return vRegex(pk, regex, pk + "'s format shold follow /" + regex + "/!");
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
	protected String vRangeLength(String paramKey, int minLength,
			int maxLength, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isSafeString(value, minLength, maxLength, null)) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}

	protected String vRangeLength(String pk, int minLen, int maxLen) {
		return vRangeLength(pk, minLen, maxLen, pk
				+ "'s length should between " + minLen + " and " + maxLen + "!");
	}

	protected String vMinLength(String paramKey, int minlength, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (StringUtils.trimToEmpty(value).length() < minlength) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}

	protected String vMinLength(String pk, int minLen) {
		return vMinLength(pk, minLen, pk + "'s length should longer than "
				+ minLen + "!");
	}

	protected String vMaxLength(String paramKey, int maxlength, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (StringUtils.trimToEmpty(value).length() > maxlength) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}

	protected Integer vInt(String paramKey, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isInt(value)) {
			addError(paramKey, errMsg);
			return null;
		}
		return Integer.parseInt(value);
	}

	protected Integer vInt(String pk) {
		return vInt(pk, pk + " should be a Integer!");
	}

	protected Float vFloat(String paramKey, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isFloat(value)) {
			addError(paramKey, errMsg);
			return null;
		}
		return Float.parseFloat(value);
	}

	protected Float vFloat(String pk) {
		return vFloat(pk, pk + " should be a Float number!");
	}

	protected Integer vIntInclude(String paramKey, int[] values, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (DataChecker.isInt(value)) {
			int re = Integer.parseInt(value);
			Arrays.sort(values);
			if (Arrays.binarySearch(values, re) > 0) {
				return re;
			}
		}
		addError(paramKey, errMsg);
		return null;
	}

	protected Integer vIntInclude(String pk, int[] values) {
		return vIntInclude(pk, values, pk + " should in " + values + "!");
	}

	protected String vStringInclude(String paramKey, String[] values,
			String errMsg) {
		String value = WebContext.get().param(paramKey);
		Arrays.sort(values);
		if (Arrays.binarySearch(values, value) > 0) {
			return value;
		} else {
			addError(paramKey, errMsg);
			return null;
		}
	}

	protected String vStringInclude(String pk, String[] values) {
		return vStringInclude(pk, values, pk + " should in " + values + "!");
	}

	protected Date vDate(String paramKey, String format, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isDate(value, format)) {
			addError(paramKey, errMsg);
			return null;
		}
		return TopazUtil.parseDate(value, format);
	}
	
	protected Date vDate(String pk, String format) {
		return vDate(pk, format, pk + " should be a Date as format " + format + "!");
	}

	protected String vEmail(String paramKey, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isEmail(value)) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}
	
	protected String vEmail(String pk ) {
		return vEmail(pk, pk + " should be a Email address!");
	}

	protected String vCellphone(String paramKey, String errMsg) {
		String value = WebContext.get().param(paramKey);
		if (!DataChecker.isCellphone(value)) {
			addError(paramKey, errMsg);
			value = null;
		}
		return value;
	}
	
	protected String vCellphone(String pk) {
		return vCellphone(pk, pk + " should be a cellphone nubmer!");
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