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
        int code = HttpRequest.get("http://localhost:8080/").code();
        assertEquals(200, code);

        code = HttpRequest.get("http://localhost:8080/404").code();
        assertEquals(404, code);
    }
}
