package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.tool.Mocks;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Created by foxty on 17/6/19.
 */
public class ValidationTest {

    @Test
    public void testCreateValidation() throws Exception {

        HttpServletRequest req = Mocks.mockHttpServletRequest(HttpMethod.GET, "/", "test", new HashMap<String, String>() {
            {
                put("prop", "test");
            }
        });
        HttpServletResponse resp = Mocks.mockHttpServletResponse();
        WebContext.create(req, resp, "/", null);

        Validation v = Validation.create("prop");
        assertEquals(true, Mocks.getPrivate(v, "valid"));
        assertEquals(true, Mocks.getPrivate(v, "notBlank"));
        assertEquals("prop", Mocks.getPrivate(v, "key"));
        assertEquals("test", Mocks.getPrivate(v, "value"));

    }
}
