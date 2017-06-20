package com.github.foxty.topaz.controller;

import java.util.Arrays;

import com.github.foxty.topaz.common.DataChecker;
import com.github.foxty.topaz.common.TopazUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Validation to valid input parameter.
 */
public class Validation {
	private boolean valid;
	private boolean notBlank;
	private String key;
	private String value;
	private Object target;
	private String errMsg;

	public static Validation create(String propKey, String msg) {
	    return new Validation(propKey, msg);
    }

    public static Validation create(String propKey) {
	    return new Validation(propKey, propKey + " is not valid.");
    }

	private Validation(String propKey, String errMsg) {
		this.valid = true;
		this.key = propKey;
		this.value = StringUtils.trim(WebContext.get().param(propKey));
		this.notBlank = StringUtils.isNotBlank(value);
		this.target = value;
		this.errMsg = errMsg;
	}

	public Validation trim() {
		value = StringUtils.trim(value);
		return this;
	}

	public Validation required() {
		if (valid) {
			valid &= StringUtils.isNotBlank(value);
		}
		return this;
	}

	public Validation vRegex(String regex) {
		if (valid && notBlank) {
			valid &= DataChecker.regexTest(regex, value);
		}
		return this;
	}

	/**
	 * Check is current properties is in the length range(inclusive).
	 * 
	 * @param minLength minimum length
	 * @param maxLength maximum length
	 * @return current validation object
	 */
	public Validation vRangeLength(int minLength,
                                   int maxLength) {
		if (valid && notBlank) {
			valid &= DataChecker.isSafeString(value, minLength, maxLength, null);
		}
		return this;
	}

	/**
	 * Check is current properties is longer than the input length (inclusive).
	 * 
	 * @param minlength minimum length
	 * @return current validation object
	 */
	public Validation vMinLength(int minlength) {
		if (valid && notBlank) {
			valid &= (StringUtils.trimToEmpty(value).length() >= minlength);
		}
		return this;
	}

	/**
	 * Check is current properties is shorter than the input length (inclusive).
	 * 
	 * @param maxlength maximum length
	 * @return current validation object
	 */
	public Validation vMaxLength(int maxlength) {
		if (valid && notBlank) {
			valid &= (StringUtils.trimToEmpty(value).length() <= maxlength);
		}
		return this;
	}

	public Validation vInt() {
		if (valid && notBlank) {
			valid &= DataChecker.isInt(value);
			if (valid) target = new Integer(value);
		}
		return this;
	}

	public Validation vFloat() {
		if (valid && notBlank) {
			valid &= DataChecker.isFloat(value);
			if (valid) target = new Float(value);
		}
		return this;
	}

	public Validation vIntInclude(int[] values) {
		if (valid && notBlank) {
			int re = Integer.parseInt(value);
			Arrays.sort(values);
			valid &= (Arrays.binarySearch(values, re) >= 0);
			if (valid) target = new Integer(value);
		}
		return this;
	}

	public Validation vStringInclude(String[] values) {
		if (valid && notBlank) {
			Arrays.sort(values);
			valid &= Arrays.binarySearch(values, value) >= 0;
			if (valid) target = value;
		}
		return this;
	}

	public Validation vDate(String format) {
		if (valid && notBlank) {
			valid &= DataChecker.isDate(value, format);
			if (valid) target = TopazUtil.parseDate(value, format);
		}
		return this;
	}

	public Validation vEmail() {
		if (valid && notBlank) {
			valid &= DataChecker.isEmail(value);
			if (valid) target = value;
		}
		return this;
	}

	public Validation vCellphone() {
		if (valid && notBlank) {
			valid &= DataChecker.isCellphone(value);
			if (valid) target = value;
		}
		return this;
	}

	private void addError() {
		WebContext.get().getErrors().put(key, errMsg);
	}

	public <T> T get() {
		T re = null;
		if (valid) {
			re = (T) target;
		} else {
			addError();
		}
		return re;
	}
}
