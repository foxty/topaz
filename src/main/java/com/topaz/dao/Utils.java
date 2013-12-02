package com.topaz.dao;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.topaz.common.TopazException;

public class Utils {

	private static Log log = LogFactory.getLog(Utils.class);

	/**
	 * e.g. AbcDefDAo -> abc_def
	 * 
	 * @param className
	 * @return
	 */
	public static String camel2flat(String input) {

		StringBuffer result = new StringBuffer();
		result.append(Character.toLowerCase(input.charAt(0)));

		char[] chars = input.substring(1).toCharArray();
		for (char c : chars) {
			if (c >= 'A' && c <= 'Z') {
				result.append("_").append(Character.toLowerCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString().toLowerCase();
	}

	/**
	 * e.g. set_something -> setSomething
	 * 
	 * @param className
	 * @return
	 */
	public static String flat2camel(String input) {
		StringBuffer result = new StringBuffer();
		String[] arr = input.split("_");
		result.append(arr[0]);
		for (int i = 1; i < arr.length; i++) {
			String ele = arr[i];
			if (StringUtils.isNotBlank(ele)) {
				result.append(StringUtils.capitalize(ele));
			}
		}
		return result.toString();
	}

	/**
	 * Customized MD5 algorithm
	 * 
	 * @param str
	 * @return MD5 String
	 */
	public static String md5(String str) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new TopazException(e);
		}
		digest.update(str.getBytes());
		byte[] bytes = digest.digest();
		StringBuffer sb = new StringBuffer(32);
		for (byte b : bytes) {
			String s = Integer.toHexString(Math.abs(b));
			sb.append(s.length() == 1 ? "0" + s : s);
		}
		return sb.toString();
	}

	public static long checksumCRC32(InputStream ins) throws IOException {
		CRC32 crc = new CRC32();
		InputStream in = null;
		try {
			in = new CheckedInputStream(ins, crc);
			IOUtils.copy(in, new NullOutputStream());
		} finally {
			IOUtils.closeQuietly(in);
		}
		return crc.getValue();
	}

	public static float parseFloat(String v, float def) {
		float re = def;
		try {
			re = Float.parseFloat(v);
		} catch (NumberFormatException e) {
			log.error(e.getMessage(), e);
		}
		return re;

	}
	
	public static boolean isFloatEqual(float f1, float f2, float precision) {
		return Math.abs(f1 - f2) <= precision;
	}
	
	public static boolean isDoubleEqual(double f1, double f2, double precision) {
		return Math.abs(f1 - f2) <= precision;
	}
}
