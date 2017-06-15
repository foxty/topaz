package com.github.foxty.topaz.controller;

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by itian on 6/15/2017.
 */
public class PaginationTest {

    @Test
    public void testPaginationInit() {
        Pagination p = new Pagination(10, 1);
        assertEquals(10, p.getPageSize());
        assertEquals(1, p.getPage());
        assertEquals(0, p.getOffset());
        assertFalse(p.isReady());


        p = new Pagination(0, 0);
        assertEquals(1, p.getPageSize());
        assertEquals(1, p.getPage());
        assertEquals(0, p.getOffset());
        assertFalse(p.isReady());
    }

    @Test
    public void testCalcPage() {
        Pagination p = new Pagination(7, 1);
        p.calcPagination(0);
        assertEquals(1, p.getPage());
        assertEquals(0, p.getMaxPage());
        assertEquals(0, p.getOffset());
        assertEquals(0, p.getRecordSize());

        p.calcPagination(6);
        assertEquals(1, p.getPage());
        assertEquals(1, p.getMaxPage());
        assertEquals(6, p.getRecordSize());

        p.calcPagination(7);
        assertEquals(1, p.getPage());
        assertEquals(1, p.getMaxPage());
        assertEquals(7, p.getRecordSize());

        p.calcPagination(8);
        assertEquals(1, p.getPage());
        assertEquals(2, p.getMaxPage());
        assertEquals(8, p.getRecordSize());

        p = new Pagination(7, 2);
        p.calcPagination(0);
        assertEquals(2, p.getPage());
        assertEquals(0, p.getMaxPage());
        assertEquals(7, p.getOffset());
        assertEquals(0, p.getRecordSize());

        p.calcPagination(10);
        assertEquals(2, p.getPage());
        assertEquals(2, p.getMaxPage());
        assertEquals(7, p.getOffset());
        assertEquals(10, p.getRecordSize());

        p.calcPagination(14);
        assertEquals(2, p.getPage());
        assertEquals(2, p.getMaxPage());
        assertEquals(7, p.getOffset());
        assertEquals(14, p.getRecordSize());

    }
}
