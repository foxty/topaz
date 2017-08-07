package com.github.foxty.topaz.common;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ConfigTest {
	private static File CFG_FILE = new File(ConfigTest.class.getResource("/test_topaz.properties").getFile());

	@Test
	public void testDefaultConfig() throws Exception {
		Config.init(null);
		Config c = Config.getInstance();
		assertEquals("jdbc:h2:mem:default_db;", c.getDbUrl());
	}

	@Test
	public void testSystemPropOverride() throws Exception {
		Config.init(null);
		Config c = Config.getInstance();
		assertEquals("org.h2.Driver", c.getDbDriver());
		assertEquals("jdbc:h2:mem:default_db;", c.getDbUrl());
		System.setProperty("ds.Driver", "override.driver.class");
		assertEquals("override.driver.class", c.getDbDriver());
		System.setProperty("ds.Driver", "org.h2.Driver");
	}

	@Test
	public void testDataSource() {
		Config.init(CFG_FILE);
		Config c = Config.getInstance();
		assertEquals("org.h2.Driver", c.getDbDriver());
		assertEquals("jdbc:h2:mem:testdb;INIT=RUNSCRIPT FROM 'src/test/resources/testdb.sql'", c.getDbUrl());
		assertEquals("sa", c.getDbUsername());
		assertEquals("", c.getDbPassword());
		assertEquals(5, c.getDbPoolMaxIdle());
		assertEquals(2, c.getDbPoolMinIdle());
		assertEquals(20, c.getDbPoolMaxActive());
		assertEquals(10000, c.getDbPoolMaxWait());

	}

	@Test
	public void testHotConf() {
		Config.init(CFG_FILE);
		Config.REFRESH_TIME = 1000;
		Config c = Config.getInstance();
		assertEquals("sa", c.getDbUsername());

		List<String> configItems = new ArrayList<>();
		configItems.add("");
		configItems.add("hotconfig=true");
		try {
			FileUtils.writeLines(CFG_FILE, configItems, true);
			Thread.sleep(2 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("sa", c.getDbUsername());
		assertEquals("true", c.getConfig("hotconfig"));
	}

	@Test
	public void testInterpolatin() throws Exception {
		Config.init(CFG_FILE);
		Config c = Config.getInstance();

		assertEquals(null, c.getString("test.env1"));
		assertEquals("env2", c.getString("test.env2"));
		assertEquals(System.getenv("JAVA_HOME"), c.getString("test.env3"));

		System.setProperty("PROP3_VALUE", "prop3");
		assertEquals(null, c.getString("test.prop1"));
		assertEquals("prop2", c.getString("test.prop2"));
		assertEquals("prop3", c.getString("test.prop3"));

		assertEquals("a", c.getString("test.a"));
		assertEquals("b", c.getString("test.b"));
		assertEquals("ab", c.getString("test.ab"));
		assertEquals("a1b2", c.getString("test.a1b2"));
		assertEquals("1a", c.getString("test.1a"));
		assertEquals("1a1", c.getString("test.1a1"));
	}

	public static void main(String args[]) {
		String s = "${test.a}${";
		int spos = s.indexOf("${");
		int epos = s.indexOf("}", spos);
		while(spos >= 0 && epos > 0) {
			String exp = s.substring(spos, epos + 1);
			System.out.println(exp);
			spos = s.indexOf("${", epos);
			epos = s.indexOf("}", spos);
		}
	}
}
