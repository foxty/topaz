package com.topaz.common;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigTest {
	private static File CFG_FILE = new File("target/test-classes/config-test.properties");
	
	@BeforeClass
	public static void setUpClass() {
		
	}

	@Test
	public void testDataSource() {
		System.out.println(CFG_FILE.getAbsolutePath());
		Config.init(CFG_FILE);
		Config c = Config.getInstance();
		assertEquals("com.mysql.jdbc.Driver", c.getDbDriver());
		assertEquals("jdbc:mysql://localhost/mysql?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true",
				c.getDbUrl());
		assertEquals("root", c.getDbUsername());
		assertEquals("123456", c.getDbPassword());
		assertEquals(5, c.getDbPoolMaxIdle());
		assertEquals(2, c.getDbPoolMinIdle());
		assertEquals(20, c.getDbPoolMaxActive());
		assertEquals(10000, c.getDbPoolMaxWait());

	}


	@Test
	public void testHotConf() {
		Config.init(CFG_FILE);
		Config.REFRESH_TIME = 5*1000;
		Config c = Config.getInstance();
		assertEquals("root", c.getDbUsername());

		List<String> configItems = new ArrayList<String>();
		configItems.add("");
		configItems.add("hotconfig=true");
		try {
			FileUtils.writeLines(CFG_FILE, configItems,
					true);
			Thread.sleep(6 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("root", c.getDbUsername());
		assertEquals("true", c.getConfig("hotconfig"));
	}
}
