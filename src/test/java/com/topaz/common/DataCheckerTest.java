package com.topaz.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataCheckerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegexTest() {
		assertTrue(DataChecker.regexTest("\\d{2}", "11"));
		assertTrue(DataChecker.regexTest("^\\d+", "11"));
	}

	@Test
	public void testIsSafeString() {
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
		String re = DataChecker.filteHTML("<abc>");
		assertEquals("&lt;abc&gt;", re);
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
