package com.github.foxty.topaz.dao.sql;

import static org.junit.Assert.*;

import com.github.foxty.topaz.dao.ModelA;
import com.github.foxty.topaz.dao.Models;
import com.github.foxty.topaz.tool.Mocks;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by itian on 6/26/2017.
 */
public class SQLSelectTest {

    @Test
    public void testCreation() throws Exception {
        SQLSelect ss = new SQLSelect(ModelA.class);
        assertEquals("table_name_a", ss.tableName);
        assertEquals(ModelA.class, ss.modelClazz);
        Assert.assertEquals(Models.getInstance().getModelMeta(ModelA.class), ss.modelMeta);
        assertEquals(0, ((String[])Mocks.getPrivate(ss, "with")).length);
        assertEquals("SELECT *  FROM table_name_a", ss.sql.toString());

        ss = new SQLSelect(ModelA.class, "modelb");
        assertEquals(1, ((String[])Mocks.getPrivate(ss, "with")).length);
        assertEquals("SELECT * ,modelb.expired_date_on AS 'modelb.expired_date_on'," +
                        "modelb.name AS 'modelb.name',modelb.modela_id AS 'modelb.modela_id'," +
                        "modelb.id AS 'modelb.id' FROM table_name_a JOIN model_b modelb ON table_name_a.id=modelb.table_name_a_id",
                ss.sql.toString());

        String sql = "Select * from model_a where a=? and b=?";
        ss = new SQLSelect(sql, new ArrayList());
        assertEquals(sql, ss.sql.toString());
    }

    @Test
    public void testOrderby() throws Exception {
        SQLSelect ss = new SQLSelect(ModelA.class);
        ss.orderBy("name", true);
        assertEquals("SELECT *  FROM table_name_a ORDER BY table_name_a.aname ASC ", ss.sql.toString());

        ss = new SQLSelect(ModelA.class);
        ss.orderBy("name", false);
        assertEquals("SELECT *  FROM table_name_a ORDER BY table_name_a.aname DESC ", ss.sql.toString());
    }

    @Test
    public void testLimit() throws Exception {
        SQLSelect ss = new SQLSelect(ModelA.class);
        ss.limit(10, 5);
        assertEquals("SELECT *  FROM table_name_a LIMIT 10,5", ss.sql.toString());
    }
}
