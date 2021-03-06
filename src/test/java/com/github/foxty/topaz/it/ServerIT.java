package com.github.foxty.topaz.it;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by itian on 7/5/2017.
 */
public class ServerIT {

    @Test
    public void testServerReady() throws Exception {
        int code = HttpRequest.get("http://localhost:12345/").code();
        assertEquals(404, code);

        code = HttpRequest.get("http://localhost:12345/404").code();
        assertEquals(404, code);
    }
}
