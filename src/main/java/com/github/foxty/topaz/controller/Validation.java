package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.DataChecker;
import com.github.foxty.topaz.common.TopazUtil;
import org.apache.commons.lang.StringUtils;

import javax.imageio.metadata.IIOInvalidTreeException;
import java.util.Arrays;

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

    public Validation regex(String regex) {
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
    public Validation rengeLen(int minLength,
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
    public Validation minLen(int minlength) {
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
    public Validation maxLen(int maxlength) {
        if (valid && notBlank) {
            valid &= (StringUtils.trimToEmpty(value).length() <= maxlength);
        }
        return this;
    }

    public Validation vInt() {
        if (valid && notBlank) {
            valid &= DataChecker.isInt(value);
            if (valid) target = Integer.valueOf(value);
        }
        return this;
    }

    public Validation vFloat() {
        if (valid && notBlank) {
            valid &= DataChecker.isFloat(value);
            if (valid) target = Float.valueOf(value);
        }
        return this;
    }

    public Validation in(int[] values) {
        if (valid && notBlank) {
            int re = Integer.parseInt(value);
            Arrays.sort(values);
            valid &= (Arrays.binarySearch(values, re) >= 0);
            if (valid) target = new Integer(value);
        }
        return this;
    }

    public Validation in(String[] values) {
        if (valid && notBlank) {
            Arrays.sort(values);
            valid &= Arrays.binarySearch(values, value) >= 0;
            if (valid) target = value;
        }
        return this;
    }

    public Validation date(String format) {
        if (valid && notBlank) {
            target = TopazUtil.parseDate(value, format);
            valid &= (target != null);
        }
        return this;
    }

    public Validation dateTime(String format) {
        if (valid && notBlank) {
            target = TopazUtil.parseDateTime(value, format);
            valid &= (target != null);
        }
        return this;
    }

    public Validation email() {
        if (valid && notBlank) {
            valid &= DataChecker.isEmail(value);
            if (valid) target = value;
        }
        return this;
    }

    public Validation cellphone() {
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
