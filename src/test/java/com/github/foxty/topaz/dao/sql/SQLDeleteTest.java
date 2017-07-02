package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.ModelA;
import org.junit.Test;

import static com.github.foxty.topaz.dao.sql.SQLDelete.fn.delete;
import static com.github.foxty.topaz.dao.sql.SQLUpdate.fn.update;
import static org.junit.Assert.assertEquals;

/**
 * Created by foxty on 17/6/30.
 */
public class SQLDeleteTest {

    @Test
    public void testCreation() throws Exception {
        SQLDelete sd = delete(ModelA.class);
        assertEquals("DELETE FROM model_a", sd.sql.toString());
    }

    @Test
    public void testWhere() throws Exception {
        SQLDelete sd = delete(ModelA.class).where("id", 100);
        String expSql = "DELETE FROM model_a WHERE  model_a.id = ? ";
        assertEquals(expSql, sd.sql.toString());
        assertEquals(1, sd.sqlParams.size());
        assertEquals(100, sd.sqlParams.get(0));
    }
}
