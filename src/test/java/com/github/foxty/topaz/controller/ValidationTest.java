package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.tool.Mocks;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by foxty on 17/6/19.
 */
public class ValidationTest {

    public void setup() throws IOException {
        HttpServletRequest req = Mocks.httpRequest(HttpMethod.GET, "/", "test", new HashMap<String, String>() {
            {
                put("prop", "test");
                put("int16", "16");
                put("float", "5.123");
                put("string", "this is a string value");
                put("blank", " ");
                put("date1", "2017-6-20");
                put("date2", "2017-6-20 12:12:12");
                put("email", "foxty@wwa.com");
                put("cellphone", "18189898899");
            }
        });
        HttpServletResponse resp = Mocks.httpResponse();
        WebContext.create(req, resp, "/");
    }


    @Test
    public void testCreateValidation() throws Exception {
        setup();

        Validation v = Validation.create("prop");
        assertEquals(true, Mocks.getPrivate(v, "valid"));
        assertEquals(true, Mocks.getPrivate(v, "notBlank"));
        assertEquals("prop", Mocks.getPrivate(v, "key"));
        assertEquals("test", Mocks.getPrivate(v, "value"));
    }

    @Test
    public void testRequired() throws Exception {
        setup();
        WebContext wc = WebContext.get();
        Validation v = Validation.create("blank");
        String value = v.required().get();
        assertNull(value);
        assertFalse(Mocks.getPrivate(v, "valid"));
        assertFalse(wc.getErrors().isEmpty());
        assertTrue(wc.getErrors().containsKey("blank"));

        setup();
        wc = WebContext.get();
        v = Validation.create("prop");
        value = v.required().get();
        assertEquals("test", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertEquals(0, wc.getErrors().size());

        setup();
        wc = WebContext.get();
        v = Validation.create("noexsitpropety");
        value = v.get();
        assertNull(value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("noexsitpropety");
        value = v.required().get();
        assertNull(value);
        assertFalse(Mocks.getPrivate(v, "valid"));
        assertFalse(wc.getErrors().isEmpty());
    }

    @Test
    public void testRegex() throws Exception {
        setup();
        WebContext wc = WebContext.get();
        Validation v = Validation.create("string");
        String value = v.regex("^this is .*$").get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertEquals(0, wc.getErrors().size());

        v = Validation.create("int16");
        value = v.regex("^\\d+$").get();
        assertEquals("16", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());
    }

    @Test
    public void testRangeLen() throws Exception {
        setup();
        WebContext wc = WebContext.get();
        Validation v = Validation.create("string");
        String value = v.rangeLen(0, 100).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.rangeLen(22, 22).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.rangeLen(23, 26).get();
        assertNull(value);
        assertFalse(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().containsKey("string"));
    }

    @Test
    public void testMinLen() throws Exception {
        setup();
        WebContext wc = WebContext.get();
        Validation v = Validation.create("string");
        String value = v.minLen(10).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.minLen(22).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.minLen(23).get();
        assertNull(value);
        assertFalse(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().containsKey("string"));
    }

    @Test
    public void testMaxLen() throws Exception {
        setup();
        WebContext wc = WebContext.get();
        Validation v = Validation.create("string");
        String value = v.maxLen(30).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.maxLen(22).get();
        assertEquals("this is a string value", value);
        assertTrue(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().isEmpty());

        v = Validation.create("string");
        value = v.maxLen(2).get();
        assertNull(value);
        assertFalse(Mocks.getPrivate(v, "valid"));
        assertTrue(wc.getErrors().containsKey("string"));
    }

    @Test
    public void testvInt() throws Exception {
        setup();

        Validation v = Validation.create("int16");
        int result = v.vInt().get();
        assertEquals(16, result);
        assertTrue(WebContext.get().isInputValid());

        v = Validation.create("float");
        Integer result1 = v.vInt().get();
        assertNull(result1);
        assertFalse(WebContext.get().isInputValid());
    }

    @Test
    public void testvFloat() throws Exception {
        setup();

        Validation v = Validation.create("float");
        Float result = v.vFloat().get();
        assertEquals(5.123f, result.floatValue(), 0.0001f);
        assertTrue(WebContext.get().isInputValid());

        v = Validation.create("string");
        result = v.vFloat().get();
        assertNull(result);
        assertFalse(WebContext.get().isInputValid());
    }

    @Test
    public void testIn() throws Exception {
        setup();

        String value = Validation.create("prop").in(new String[]{"test", "abc", "def"}).get();
        assertEquals("test", value);
        assertTrue(WebContext.get().isInputValid());

        value = Validation.create("prop").in(new String[]{"test1", "abc", "def"}).get();
        assertNull(value);
        assertFalse(WebContext.get().isInputValid());

        setup();
        Integer intv = Validation.create("int16").in(new int[]{18, 16, 17}).get();
        assertEquals(16, intv.intValue());
        assertTrue(WebContext.get().isInputValid());

        intv = Validation.create("int16").in(new int[]{12, 13, 14}).get();
        assertNull(intv);
        assertFalse(WebContext.get().isInputValid());
    }

    @Test
    public void testDate() throws Exception {
        setup();

        LocalDate ld = Validation.create("date1").date("yyyy-M-dd").get();
        assertEquals(2017, ld.getYear());
        assertEquals(6, ld.getMonthValue());
        assertEquals(20, ld.getDayOfMonth());

        LocalDateTime ldt = Validation.create("date2").dateTime("yyyy-M-dd HH:mm:ss").get();
        assertEquals(2017, ldt.getYear());
        assertEquals(6, ldt.getMonthValue());
        assertEquals(20, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHour());
        assertEquals(12, ldt.getMinute());
        assertEquals(12, ldt.getSecond());
    }
}
