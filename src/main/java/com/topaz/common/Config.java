package com.topaz.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration for the app. Will reload configuration file if the file
 * changed.
 * 
 * @author foxty
 */
public class Config {
	public static long REFRESH_TIME = 300 * 1000;
	private static Log log = LogFactory.getLog(Config.class);
	private static Config instance;

	private File cfgFile;
	private long lastModifiedTime = 0;
	private long lastCheckTime = 0;
	private Properties props;
	private List<String> booleanValues = new ArrayList<String>(2);

	public static void init(File cFile) {
		instance = new Config(cFile);
	}

	public static Config getInstance() {
		return instance;
	}

	private Config(File cFile) {
		cfgFile = cFile;
		props = new Properties();
		booleanValues.add("true");
		booleanValues.add("True");
		booleanValues.add("false");
		booleanValues.add("False");
		
		loadConfig();
	}

	private void loadConfig() {
		try {
			props.load(FileUtils.openInputStream(cfgFile));
			lastModifiedTime = cfgFile.lastModified();
			lastCheckTime = System.currentTimeMillis();
			log.info("Load config " + cfgFile);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new TopazException(e);
		}
	}

	protected String getConfig(String key) {
		long millisDiff = System.currentTimeMillis() - lastCheckTime;
		if (millisDiff > REFRESH_TIME
				&& FileUtils.isFileNewer(cfgFile, lastModifiedTime)) {
			log.info("Config file changed, reloading. ");
			loadConfig();
		}
		return (String) props.get(key);
	}

	private int getIntItem(String key, int defaultValue) {
		int result = defaultValue;
		String v = getConfig(key);
		try {
			result = Integer.parseInt(v);
		} catch (Exception e) {
			log.error("Error while get config item [" + key
					+ "], use default value " + defaultValue);
			result = defaultValue;
		}
		return result;
	}

	private long getLongItem(String key, long defValue) {
		long result = defValue;
		String v = getConfig(key);
		try {
			result = Integer.parseInt(v);
		} catch (Exception e) {
			log.error("Error while get config item [" + key
					+ "], use default value " + defValue);
			result = defValue;
		}
		return result;
	}

	private boolean getBooleanItem(String key, boolean defValue) {
		boolean re = defValue;
		String v = getConfig(key);
		if (booleanValues.contains(v)) {
			re = Boolean.valueOf(v);
		}
		return re;
	}

	/*
	 * Database Connection Configurations
	 */
	public String getDbDriver() {
		return getConfig("ds.Driver");
	}

	public String getDbUrl() {
		return getConfig("ds.Url");
	}

	public String getDbUsername() {
		return getConfig("ds.Username");
	}

	public String getDbPassword() {
		return getConfig("ds.Password");
	}

	public int getDbPoolMinIdle() {
		return getIntItem("ds.MinIdle", 2);
	}

	public int getDbPoolMaxIdle() {
		return getIntItem("ds.MaxIdle", 5);
	}

	public int getDbPoolMaxActive() {
		return getIntItem("ds.MaxActive", 20);
	}

	public int getDbPoolMaxWait() {
		return getIntItem("ds.MaxWait", 10000);
	}
}