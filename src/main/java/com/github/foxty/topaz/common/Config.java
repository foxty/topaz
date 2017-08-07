package com.github.foxty.topaz.common;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.util.IOUtils;

/**
 * Configuration for the application. Will reload configuration file if the file
 * changed.
 * 
 * @author foxty
 */
public class Config {
	public static long REFRESH_TIME = 300 * 1000;
	private static Log log = LogFactory.getLog(Config.class);
	private static String[] propertyFiles = new String[] { "/topaz.properties", "/topaz_default.properties" };
	private static Config instance;

	private static class DefInstanceHolder {
		static Config instance = new Config(null);
	}

	private File cfgFile;
	private long lastModifiedTime = 0;
	private long lastCheckTime = 0;
	private Properties props = new Properties();
	private List<String> booleanValues = new ArrayList<String>(2);

	public static void init(File cFile) {
		instance = new Config(cFile);
	}

	public static Config getInstance() {
		if (instance == null) {
			log.warn("Configuration haven't initialized, now create it.");
			return DefInstanceHolder.instance;
		} else {
			return instance;
		}
	}

	private Config(File cFile) {
		booleanValues.add("true");
		booleanValues.add("True");
		booleanValues.add("false");
		booleanValues.add("False");
		cfgFile = cFile;
		loadConfig();

	}

	private void loadConfig() {
		InputStream ins = null;
		try {
			if (cfgFile == null || !cfgFile.exists()) {
				for (String pFile : propertyFiles) {
					log.info("Try to locate config file " + pFile);
					ins = Config.class.getResourceAsStream(pFile);
					if (ins != null) {
						log.info("Will load config from " + pFile);
						break;
					}
				}
			} else {
				lastModifiedTime = cfgFile.lastModified();
				lastCheckTime = System.currentTimeMillis();
				ins = FileUtils.openInputStream(cfgFile);
				log.info("Will load config from " + cfgFile
						+ ", hot swap enabled since its an outside configuration file.");
			}
			// configurations.pro
			props.load(ins);
		} catch (Exception e) {
			log.error("Fail to load config", e);
			throw new TopazException(e);
		} finally {
			IOUtils.close(ins);
		}
	}

	/**
	 * interpolation the value from system env or system property. 
	 * <pre>
	 * - p1=${env:xxx:default}		Sytem.env("xxx") or default 
	 * - p2=${prop:yyy:default}		System.properties("yyy") or default 
	 * - p3=${p1}					this.getConfig("p1") 
	 * - p4=http://${p1}/			"http://" + this.getConfig("p1")
	 * </pre>
	 * @param val expression to interpolate
	 * @return interpolated value
	 */

	public String interpolate(String val) {
		String re = val;
		if (StringUtils.isNotBlank(val)) {
			StringBuffer sb = new StringBuffer(val);
			int spos = sb.indexOf("${");
			int epos = sb.indexOf("}", spos);
			while (spos >= 0 && epos > 0) {
				String exp = sb.substring(spos, epos + 1);
				String finalValue = interpolateExpression(exp);
				sb.replace(spos, epos + 1, finalValue == null ? "" : finalValue);
				spos = sb.indexOf("${");
				epos = sb.indexOf("}");
			}

			re = sb.length() == 0 ? null : sb.toString();
		}
		return re;
	}

	public String interpolateExpression(String expression) {
		String re = null;
		int expLen = expression.length();
		int firstPos = expression.indexOf(':');
		int lastPos = expression.lastIndexOf(':');
		if (firstPos != -1) {
			String prefix = expression.substring(2, firstPos);
			String key = expression.substring(firstPos + 1, firstPos == lastPos ? expLen - 1 : lastPos);
			String defVal = null;
			if (firstPos != lastPos) {
				defVal = expression.substring(lastPos + 1, expLen - 1);
			}
			if ("env".equals(prefix)) {
				String envVal = System.getenv(key);
				if (null != envVal) {
					re = envVal;
				} else {
					re = defVal;
				}
			}
			if ("prop".equals(prefix)) {
				re = System.getProperty(key, defVal);
			}
		} else {
			// Get rid of the starter "${" and trailing "}"
			String key = expression.substring(2, expLen - 1);
			re = getConfig(key);
		}
		return re;
	}

	public String getConfig(String key) {
		long millisDiff = System.currentTimeMillis() - lastCheckTime;
		if (lastCheckTime != 0 && millisDiff > REFRESH_TIME && FileUtils.isFileNewer(cfgFile, lastModifiedTime)) {
			log.info("Config file changed, reloading...");
			loadConfig();
		}
		// System property will override the config property
		String value = System.getProperty(key, props.getProperty(key));
		return interpolate(value);
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