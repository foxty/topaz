package com.topaz.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * 数据验证工具类
 * </p>
 * <p>
 * Name : DataChecker
 * </p>
 * 
 * @author foxty
 * @version 1.0
 */
public class DataChecker {

	/**
	 * 自定义验证方法,利用政则表达式验证字符串
	 * 
	 * @param regex
	 *            政则表达式
	 * @param str
	 *            输入字符串
	 * @return boolean
	 */
	public static boolean regexTest(String regex, String str) {
		boolean result = false;
		if (str != null) {
			result = str.matches(regex);
		}
		return result;
	}

	/**
	 * 字符串验证 验证输入字符串的长度范围以及是否包含非法字符
	 * 
	 * @param src
	 *            需要验证的字符串
	 * @param minLength
	 *            最少长度限制
	 * @param maxLength
	 *            最大长度限制
	 * @param unSafeChars
	 *            不允许包含的字符集合
	 * @return boolean
	 */
	public static boolean isSafeString(String str, int minLength,
			int maxLength, char[] unSafeChars) {
		boolean result = true;
		String v = StringUtils.trimToEmpty(str);
		if (v.length() < minLength || v.length() > maxLength) {
			result = false;
			return result;
		}

		if (unSafeChars != null) {
			for (char c : unSafeChars) {
				if (v.indexOf(c) >= 0) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 验证输入字符串是否为整数类型
	 * 
	 * @param str
	 *            输入字符串
	 * @return boolean
	 */
	public static boolean isInt(String str) {
		boolean result = false;
		if (str != null && !str.trim().equals("")) {
			result = regexTest("^[+-]?\\d+$", str);
		}
		return result;
	}

	public static boolean isFloat(String str) {
		boolean result = false;
		if (StringUtils.isBlank(str))
			return result;
		try {
			Float.parseFloat(str);
			result = true;
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 验证日期格式
	 * 
	 * @param str
	 *            输入日期字符串
	 * @param model
	 *            格式字符串 yyyy - 表示念,MM表示月,dd表示日
	 * @return boolean
	 */
	public static boolean isDate(String str, String model) {
		boolean result = false;
		if (str != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(model);
			try {
				Date d = sdf.parse(str);
				result = sdf.format(d).endsWith(str);
			} catch (ParseException e) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * IP验证方法,验证输入的字符串是否为正确的IPv4
	 * 
	 * @param ip
	 *            输入IP字符串
	 * @return boolean
	 */
	public static boolean isIpv4(String ip) {
		boolean result = false;
		if (null != ip && DataChecker.regexTest("(\\d{1,3}\\.){3}\\d{1,3}", ip)) {
			result = true;
			String[] segs = ip.split("\\.");
			for (String seg : segs) {
				int ipNum = Integer.parseInt(seg);
				if (ipNum <= 0 || ipNum >= 255) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 过滤HTML字符，将<替换成&lt; 将>替换成&rt;并且将换行转换称html
	 * 
	 * @param input
	 * @return String
	 */
	public static String filterHTML(String input) {
		String result = input;
		if (input != null) {
			result = result.replaceAll("<", "&lt;");
			result = result.replaceAll(">", "&gt;");
		}
		return result;
	}

	public static boolean isEmail(String input) {
		boolean re = regexTest("^[\\w.-]+@[\\w.-]+[\\w-]$", input);
		return re;
	}

	public static boolean isCellphone(String input) {
		boolean re = regexTest("^1[358]\\d{9}$", input);
		return re;
	}
}