package com.topaz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

public class TopazUtilTest {

	@Test
	public void testCamel2flat() {
		assertEquals("a_b_c", TopazUtil.camel2flat("aBC"));
		assertEquals("a_b_c", TopazUtil.camel2flat("ABC"));
		assertEquals("aa_bb_cc", TopazUtil.camel2flat("aaBbCc"));
	}

	@Test
	public void testFlat2camel() {
		assertEquals("aBC", TopazUtil.flat2camel("a_b_c"));
		assertEquals("aaBbCc", TopazUtil.flat2camel("aa_bb_cc"));
	}

	@Test
	public void testMd5() {
		assertEquals("700150683c2e4f502a6a3f7d281f7f72", TopazUtil.md5("abc"));
	}

	@Test
	public void testParseFloat() {
		String fstr1 = "1.54";
		assertEquals(1.54f, TopazUtil.parseFloat(fstr1, 0), 0.01);
		String fstr2 = "1.ab";
		assertEquals(1.1, TopazUtil.parseFloat(fstr2, 1.1f), 0.01);
	}

	@Test
	public void testIsFloatEqual() {
		assertTrue(TopazUtil.isFloatEqual(1.122f, 1.121f, 0.01f));
		assertFalse(TopazUtil.isFloatEqual(1.122f, 1.121f, 0.0001f));
	}

	@Test
	public void testIsDoubleEqual() {
		assertTrue(TopazUtil.isDoubleEqual(1.122, 1.121, 0.01));
		assertFalse(TopazUtil.isDoubleEqual(1.122, 1.121, 0.001));
	}

	@Test
	public void testFormatDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2001);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DATE, 1);
		assertEquals("2001-01-01", TopazUtil.formatDate(c.getTime(), "yyyy-MM-dd"));
	}

	@Test
	public void testGenUUID() {
		assertEquals(36, TopazUtil.genUUID().length());
	}

}
