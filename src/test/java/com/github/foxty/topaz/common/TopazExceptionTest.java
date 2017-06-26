package com.github.foxty.topaz.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by itian on 6/14/2017.
 */
public class TopazExceptionTest {

    @Test
    public void testCreateWithMessage() throws Exception {
        ControllerException e = new ControllerException("test message");
        assertEquals("test message", e.getMessage());
    }

    @Test
    public void testCreateWithCause() throws Exception {
        NullPointerException npe = new NullPointerException();
        ControllerException e = new ControllerException(npe);
        assertEquals(npe, e.getCause());
    }
}
