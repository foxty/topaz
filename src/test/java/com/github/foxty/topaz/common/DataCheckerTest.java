package com.github.foxty.topaz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataCheckerTest {

	@Test
	public void testRegexTest() {
		assertTrue(DataChecker.regexTest("\\d{2}", "11"));
		assertTrue(DataChecker.regexTest("^\\d+", "11"));
	}

	@Test
	public void testIsSafeString() {
		assertTrue(DataChecker.isSafeString("abc", 3, 3, null));
		assertFalse(DataChecker.isSafeString("abcd", 4, 3, null));
		assertTrue(DataChecker.isSafeString("abc", 1, 3, null));
		assertTrue(DataChecker.isSafeString("abc", 1, 3, new char[]{'A'}));
		assertFalse(DataChecker.isSafeString("abc", 1, 3, new char[]{'a'}));
	}

	@Test
	public void testIsInt() {
		assertTrue(DataChecker.isInt("1111"));
		assertTrue(DataChecker.isInt("-1111"));
		assertTrue(DataChecker.isInt("+1111"));
		assertTrue(DataChecker.isInt("-111123123123123211"));
		assertFalse(DataChecker.isInt("-11.11"));
		assertFalse(DataChecker.isInt("-1aaa1"));
	}

	@Test
	public void testIsFloat() {
		assertTrue(DataChecker.isFloat("1.1"));
		assertTrue(DataChecker.isFloat("0.111"));
		assertTrue(DataChecker.isFloat(".123"));
		assertFalse(DataChecker.isFloat("a"));
		assertFalse(DataChecker.isFloat("0.0."));
	}

	@Test
	public void testIsDate() {
	}

	@Test
	public void testIsIp() {
		assertTrue(DataChecker.isIpv4("1.1.1.1"));
		assertTrue(DataChecker.isIpv4("10.11.11.11"));
		assertFalse(DataChecker.isIpv4("256.1.1.1"));
	}

	@Test
	public void testFilteHTML() {
		String re = DataChecker.filterHTML("<abc>");
		assertEquals("&lt;abc&gt;", re);
		assertEquals("&lt;", DataChecker.filterHTML("\u003c"));
		assertEquals("&gt;", DataChecker.filterHTML("\u003E"));
	}

	@Test
	public void testIsEmail() {
		assertTrue(DataChecker.isEmail("1@a.com"));
		assertTrue(DataChecker.isEmail("lucky.foxty@gmail.com"));
		assertTrue(DataChecker.isEmail("lu-y@gm-ail.com"));
		assertFalse(DataChecker.isEmail("lu-ym-ail.com"));
		assertFalse(DataChecker.isEmail("lu-ym-@ail.com."));
	}

	@Test
	public void testIsCellphone() {
		assertTrue(DataChecker.isCellphone("18688998145"));
		assertTrue(DataChecker.isCellphone("13688998145"));
		assertTrue(DataChecker.isCellphone("15088998145"));
		assertFalse(DataChecker.isCellphone("1508899815"));
		assertFalse(DataChecker.isCellphone("1108899815"));
		assertFalse(DataChecker.isCellphone("0108899815"));
	}

}
