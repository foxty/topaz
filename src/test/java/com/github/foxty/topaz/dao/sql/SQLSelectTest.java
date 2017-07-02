package com.github.foxty.topaz.dao.sql;

import static org.junit.Assert.*;

import com.github.foxty.topaz.dao.ModelA;
import com.github.foxty.topaz.dao.Models;
import com.github.foxty.topaz.tool.Mocks;
import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by itian on 6/26/2017.
 */
public class SQLSelectTest {

    static String SELECT_A = "SELECT model_a.*";

    @Test
    public void testCreation() throws Exception {
        SQLSelect ss = find(ModelA.class);
        assertEquals("model_a", ss.tableName);
        assertEquals(ModelA.class, ss.modelClazz);
        Assert.assertEquals(Models.getInstance().getModelMeta(ModelA.class), ss.modelMeta);
        assertEquals(0, ((String[])Mocks.getPrivate(ss, "with")).length);
        assertEquals(SELECT_A + " FROM model_a",
                ss.sql.toString());

        ss = find(ModelA.class, "modelb");
        assertEquals(1, ((String[])Mocks.getPrivate(ss, "with")).length);
        assertEquals(SELECT_A +
                        ",model_b.name AS modelb__name,model_b.expired_date_on AS modelb__expired_date_on," +
                        "model_b.model_a_id AS modelb__model_a_id,model_b.id AS modelb__id " +
                        "FROM model_a JOIN model_b ON model_a.id=model_b.model_a_id",
                ss.sql.toString());
        System.out.println(ss.sql);

        String sql = "Select * from model_a where a=? and b=?";
        ss = findBySql(ModelA.class, sql);
        assertEquals(sql, ss.sql.toString());
    }

    @Test
    public void testOrderby() throws Exception {
        SQLSelect ss = find(ModelA.class);
        ss.orderBy("name", true);
        assertEquals(SELECT_A + " FROM model_a ORDER BY model_a.name ASC ",
                ss.sql.toString());

        ss = find(ModelA.class);
        ss.orderBy("name", false);
        assertEquals(SELECT_A + " FROM model_a ORDER BY model_a.name DESC ",
                ss.sql.toString());
    }

    @Test
    public void testLimit() throws Exception {
        SQLSelect ss = find(ModelA.class);
        ss.limit(10, 5);
        assertEquals(SELECT_A + " FROM model_a LIMIT 10,5", ss.sql.toString());
    }
}
