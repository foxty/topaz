package com.github.foxty.topaz.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 *  DataChecker to check input data.
 *
 * @author foxty
 * @version 1.0
 */
public class DataChecker {

	/**
	 * Check input str with regex.
	 * 
	 * @param regex regex string
	 * @param str str need check
	 * @return true if is matches the regex, false if not match
	 */
	public static boolean regexTest(String regex, String str) {
		boolean result = false;
		if (str != null) {
			result = str.matches(regex);
		}
		return result;
	}

	/**
	 * Check if input str in the length range wihtout any unsafe chars
	 * 
	 * @param str input string need check
	 * @param minLength	minimum length
	 * @param maxLength maximum length
	 * @param unSafeChars unsafe cahrs
	 * @return true if valid, otherwise false
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
	 * Check if input str is integer
	 *
	 * @param str string need check
	 * @return true if can convert to integer, otherwise false
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
	 * Check input str is a valid date string.
	 *
	 * @param str string need check
	 * @param model refer SimpleDateFormat
	 * @return true if its a date string, otherwise false
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
	 * Check if input star is a valid IPv4 address.
	 *
	 * @param ip string need check
	 * @return true if its a ipv4 address, otherwise false
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
	 * Escape html marks
	 *
	 * @param input string need filter
	 * @return string with filter &lt; and &gt;
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