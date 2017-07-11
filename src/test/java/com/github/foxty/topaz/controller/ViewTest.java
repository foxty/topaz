package com.github.foxty.topaz.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Created by itian on 6/26/2017.
 */
public class ViewTest {

    @Test
    public void testViewCreation() throws Exception {
        View v = View.create("view1");
        assertNull(v.getLayout());
        assertNull(v.getResponseData());
        assertEquals("view1", v.getName());
        assertFalse(v.isNoLayout());

        v = View.create("layout1", "view2");
        assertEquals("layout1", v.getLayout());
        assertNull(v.getResponseData());
        assertEquals("view2", v.getName());
        assertFalse(v.isNoLayout());

        v.data("data1", "string1");
        v.data("data2", new Integer(1));
        assertEquals("string1", v.getResponseData().get("data1"));
        assertEquals(1, v.getResponseData().get("data2"));
        
        v = View.createWithoutLayout("test");
        assertTrue(v.isNoLayout());
    }
}
