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
		if (instance == null) {
			throw new TopazException("Configuratoin file haven't initialized!!");
		}
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
		if (cfgFile == null || !cfgFile.exists()) {
			log.warn("Cant'f get configuration file " + cfgFile
					+ ", using default configuration.");
			return;
		}
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

	public String getConfig(String key) {
		long millisDiff = System.currentTimeMillis() - lastCheckTime;
		if (lastCheckTime != 0 && millisDiff > REFRESH_TIME
				&& FileUtils.isFileNewer(cfgFile, lastModifiedTime)) {
			log.info("Config file changed, reloading... ");
			loadConfig();
		}
		String value = (String) props.get(key);
		return value;
	}

	public int getInt(String key) {
		int result = 0;
		String v = getConfig(key);
		try {
			result = Integer.parseInt(v);
		} catch (Exception e) {
			log.error("Config item [" + key + "] not found.");
		}
		return result;
	}

	public String getString(String key) {
		String v = getConfig(key);
		if (v == null || v.isEmpty()) {
			log.error("Config item [" + key + "] not found.");
		}
		return v;
	}

	public boolean getBoolean(String key) {
		boolean re = false;
		String v = getConfig(key);
		if (booleanValues.contains(v)) {
			re = Boolean.valueOf(v);
		} else {
			log.error("Config item [" + key + "] not found.");
		}
		return re;
	}

	/*
	 * Database Connection Configurations
	 */
	public String getDbDriver() {
		return getString("ds.Driver");
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
		return getInt("ds.MinIdle");
	}

	public int getDbPoolMaxIdle() {
		return getInt("ds.MaxIdle");
	}

	public int getDbPoolMaxActive() {
		return getInt("ds.MaxActive");
	}

	public int getDbPoolMaxWait() {
		return getInt("ds.MaxWait");
	}
}