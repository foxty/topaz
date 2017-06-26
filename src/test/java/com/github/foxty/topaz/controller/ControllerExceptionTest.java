package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.ControllerException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by itian on 6/26/2017.
 */
public class ControllerExceptionTest {

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
