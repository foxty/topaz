package com.github.foxty.topaz.common;

import com.github.foxty.topaz.tool.Mocks;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by itian on 6/14/2017.
 */
public class TopazExceptionTest {

    @Test
    public void testCreateWithMessage() throws Exception {
        TopazException e = new TopazException("test message");
        assertEquals("test message", e.getMessage());
    }

    @Test
    public void testCreateWithCause() throws Exception {
        NullPointerException npe = new NullPointerException();
        TopazException e = new TopazException(npe);
        assertEquals(npe, e.getCause());
    }
}
