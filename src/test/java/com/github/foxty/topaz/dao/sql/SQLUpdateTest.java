package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.ModelA;
import org.junit.Test;
import sun.reflect.annotation.EnumConstantNotPresentExceptionProxy;

import static com.github.foxty.topaz.dao.sql.SQLUpdate.fn.update;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by foxty on 17/6/30.
 */
public class SQLUpdateTest {

    @Test
    public void testCreation() throws Exception {
        SQLUpdate su = update(ModelA.class);
        assertEquals("UPDATE model_a SET ", su.sql.toString());
    }

    @Test
    public void testSet() throws Exception {
        SQLUpdate su = update(ModelA.class)
                .set("id", 100).set("name", "haha").set("score", 101);
        String expSql = "UPDATE model_a SET id=?,name=?,score=?";
        assertEquals(expSql, su.sql.toString());
        assertEquals(3, su.sqlParams.size());
        assertEquals(100, su.sqlParams.get(0));
        assertEquals("haha", su.sqlParams.get(1));
        assertEquals(101, su.sqlParams.get(2));

        su.where("score", 0);
        expSql += " WHERE  model_a.score = ? ";
        assertEquals(expSql, su.sql.toString());
    }

    @Test
    public void testIncDec() throws Exception {
        SQLUpdate su = update(ModelA.class).inc("score", 10)
                .dec("id", 1)
                .where("score", Operators.GT, 10);
        String expSql = "UPDATE model_a SET score=score+10,id=id-1 WHERE  model_a.score > ? ";
        assertEquals(expSql, su.sql.toString());
        assertEquals(1, su.sqlParams.size());
        assertEquals(10, su.sqlParams.get(0));
    }
}
