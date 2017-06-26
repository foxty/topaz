package com.github.foxty.topaz.dao.sql;

import static org.junit.Assert.*;

import com.github.foxty.topaz.dao.ModelA;
import com.github.foxty.topaz.dao.Models;
import com.github.foxty.topaz.dao.sql.SelectBuilder;
import com.github.foxty.topaz.tool.Mocks;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by itian on 6/26/2017.
 */
public class SelectBuilderTest {

    @Test
    public void testCreation() throws Exception {
        SelectBuilder sb = new SelectBuilder(ModelA.class);
        assertEquals("table_name_a", sb.tableName);
        assertEquals(ModelA.class, sb.modelClazz);
        Assert.assertEquals(Models.getInstance().getModelMeta(ModelA.class), sb.modelMeta);
        assertEquals(0, ((String[])Mocks.getPrivate(sb, "with")).length);
        assertEquals("SELECT table_name_a.*  FROM table_name_a", sb.sql.toString());

        sb = new SelectBuilder(ModelA.class, "modelb");
        assertEquals(1, ((String[])Mocks.getPrivate(sb, "with")).length);
        assertEquals("SELECT table_name_a.* ,modelb.name AS 'modelb.name'," +
                "modelb.modela_id AS 'modelb.modela_id',modelb.id AS 'modelb.id' " +
                "FROM table_name_a JOIN model_b modelb ON table_name_a.id=modelb.table_name_a_id",
                sb.sql.toString());

        String sql = "Select * from model_a where a=? and b=?";
        sb = new SelectBuilder(sql, new ArrayList());
        assertEquals(sql, sb.sql.toString());
    }

    @Test
    public void testCondition() throws Exception {
        SelectBuilder sb = new SelectBuilder(ModelA.class);
    }
}
