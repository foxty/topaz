package com.topaz.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	 * 字符串验证 验证输入字符串的长度范围以及是否包含非法字符
	 * 
	 * @param src
	 *            需要验证的字符串
	 * @param minLength
	 *            最少长度限制
	 * @param maxLength
	 *            最大长度限制
	 * @param unSafeStr
	 *            不允许包含的字符集合
	 * @return boolean
	 */
	public static boolean isSafe(String src, int minLength, int maxLength,
			String unSafeStr) {
		boolean result = true;
		if (src != null) {
			if (src.length() < minLength || src.length() > maxLength) {
				result = false;
				return result;
			}

			if (unSafeStr != null) {
				for (int i = 0; i < unSafeStr.length(); i++) {
					String unSafeChar = unSafeStr.substring(i, i + 1);
					for (int j = 0; j < src.length(); j++) {
						if (src.indexOf(unSafeChar) >= 0) {
							result = false;
							break;
						}
					}
				}
			}
		} else {
			result = false;
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
		if (str != null && !str.trim().equalsIgnoreCase("")) {
			if (str.startsWith("-"))
				str = str.substring(1);
			result = chkStr("\\d+", str);
		}
		return result;
	}

	/**
	 * 自定义验证方法,利用政则表达式验证字符串
	 * 
	 * @param exp
	 *            政则表达式
	 * @param str
	 *            输入字符串
	 * @return boolean
	 */
	public static boolean chkStr(String exp, String str) {
		boolean result = false;
		if (str != null) {
			result = str.matches(exp);
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
	 * IP验证方法,验证输入的字符串是否为正确的IP
	 * 
	 * @param ip
	 *            输入IP字符串
	 * @return boolean
	 */
	public static boolean isIp(String ip) {
		boolean result = false;
		if (null != ip) {
			result = DataChecker.chkStr("(\\d{1,3}\\.){3}\\d{1,3}", ip);
		}
		return result;
	}

	/**
	 * 字符串转码
	 * 
	 * @param str
	 * @param src
	 * @param dest
	 * @return String
	 */
	public static String convertCode(String str, String src, String dest) {
		try {
			str = new String(str.getBytes(src), dest);
		} catch (UnsupportedEncodingException e) {
		}
		return str;
	}

	/**
	 * 过滤HTML字符，将<替换成&lt; 将>替换成&rt;并且将换行转换称html
	 * 
	 * @param input
	 * @return String
	 */
	public static String filteHTML(String input) {
		String result = input;
		if (input != null) {
			result = result.replaceAll("<", "&lt;");
			result = result.replaceAll(">", "&gt;");
			// result = result.replaceAll("\\r\\n", "<br>");
		}
		return result;
	}

	public static boolean isEmail(String input) {
		boolean re =  chkStr("^[\\w.]+@[\\w.]+[\\w]$", input);
		return re;
	}

	public static void main(String args[]) {
		//System.out.println(DataChecker.isInt("-100a0"));
		System.out.println( DataChecker.isEmail("foxty@sina.com"));
	}
}