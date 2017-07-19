package com.github.foxty.topaz.common;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
}
