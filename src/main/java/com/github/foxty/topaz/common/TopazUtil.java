package com.github.foxty.topaz.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class TopazUtil {

    private static Log log = LogFactory.getLog(TopazUtil.class);

    /**
     * Convert input str to underscore split
     * <p>
     * e.g. AbcDefDAo to abc_def
     *
     * @param input input string
     * @return converted string
     */
    public static String camel2flat(String input) {

        StringBuffer result = new StringBuffer();
        result.append(Character.toLowerCase(input.charAt(0)));

        char[] chars = input.substring(1).toCharArray();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z') {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString().toLowerCase();
    }

    /**
     * e.g. set_something to setSomething
     *
     * @param input input string
     * @return converted string
     */
    public static String flat2camel(String input) {
        StringBuffer result = new StringBuffer();
        String[] arr = input.split("_");
        result.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            String ele = arr[i];
            if (StringUtils.isNotBlank(ele)) {
                result.append(StringUtils.capitalize(ele));
            }
        }
        return result.toString();
    }

    @Deprecated
    public static String MD5(String str) {
        return digest("MD5", str);
    }

    public static String SHA1(String str) {
        return digest("SHA-1", str);
    }

    public static String SHA256(String str) {
        return digest("SHA-256", str);
    }

    public static String SHA512(String str) {
        return digest("SHA-512", str);
    }

    public static String digest(String algo, String origStr) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new ControllerException(e);
        }
        digest.update(origStr.getBytes(Charset.forName("ASCII")));
        byte[] bytes = digest.digest();
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String s = Integer.toHexString(Math.abs(b));
            sb.append(s.length() == 1 ? "0" + s : s);
        }
        return sb.toString();
    }

    public static long checksumCRC32(InputStream ins) throws IOException {
        CRC32 crc = new CRC32();
        InputStream in = null;
        try {
            in = new CheckedInputStream(ins, crc);
            IOUtils.copy(in, new NullOutputStream());
        } finally {
            IOUtils.closeQuietly(in);
        }
        return crc.getValue();
    }

    public static float parseFloat(String v, float def) {
        float re = def;
        try {
            re = Float.parseFloat(v);
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return re;

    }

    public static boolean isFloatEqual(float f1, float f2, float precision) {
        return Math.abs(f1 - f2) <= precision;
    }

    public static boolean isDoubleEqual(double f1, double f2, double precision) {
        return Math.abs(f1 - f2) <= precision;
    }

    public static String formatDate(Date d, String fmt) {
        String re = d.toString();
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        re = sdf.format(d);
        return re;
    }

    public static LocalDate parseDate(String dateString, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        LocalDate ld = null;
        try {
            ld = LocalDate.parse(dateString, dtf);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage(), e);
        }
        return ld;
    }


    public static LocalDateTime parseDateTime(String dateString, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        LocalDateTime ldt = null;
        try {
            ldt = LocalDateTime.parse(dateString, dtf);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage(), e);
        }
        return ldt;
    }

    /**
     * Truncate hh:mm:ss.si part with 0, leave year/month/day not touched.
     *
     * @param d input date
     * @return Date without hours, minutes and seconds
     */
    public static Date truncTime(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date endOfMonth(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        Date endOfMonth = cal.getTime();
        return endOfMonth;
    }

    /**
     * Get milliseconds diff between two times, use d1.getTime()- d2.getTime(), use 0
     * if date is null.
     *
     * @param d1 first input datetime
     * @param d2 second input datetime
     * @return long milliseconds
     */
    public static long timeDiffInMilli(Date d1, Date d2) {
        long t1 = d1 == null ? 0 : d1.getTime();
        long t2 = d2 == null ? 0 : d2.getTime();
        return t1 - t2;
    }

    public static long timeDiffInSec(Date d1, Date d2) {
        long milliDiff = timeDiffInMilli(d1, d2);
        return milliDiff / 1000;
    }

    public static long timeDiffInHour(Date d1, Date d2) {
        long milliDiff = timeDiffInMilli(d1, d2);
        return milliDiff / 1000 / 3600;
    }

    public static String genUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * Remove duplicate / in the uri, remove trailing /
     * e.g.
     * //a/b/predicate/ -> /a/b/predicate
     * a/b/predicate -> /a/b/predicate
     * a//b//predicate -> a/b/predicate
     *
     * @param uri
     */
    public static String cleanUri(String uri) {
        Objects.requireNonNull(uri);
        uri = uri.replaceAll("/{2,}", "/");

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        return uri;
    }

    public static void main(String[] args) {
        System.out.println(genUUID().length());
    }
}
