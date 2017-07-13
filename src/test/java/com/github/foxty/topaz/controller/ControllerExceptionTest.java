package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.TopazException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by itian on 6/26/2017.
 */
public class ControllerExceptionTest {

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
