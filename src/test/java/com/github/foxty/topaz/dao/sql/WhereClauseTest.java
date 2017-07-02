package com.github.foxty.topaz.dao.sql;

import com.github.foxty.topaz.dao.ModelA;
import com.github.foxty.topaz.dao.ModelB;
import com.github.foxty.topaz.dao.Models;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by itian on 6/27/2017.
 */
public class WhereClauseTest {

    static ModelMeta mma;
    static ModelMeta mmb;

    @BeforeClass
    public static void beforeAllTest() {
        Models.getInstance().register(ModelA.class, ModelB.class);
        mma = Models.getInstance().getModelMeta(ModelA.class);
        mmb = Models.getInstance().getModelMeta(ModelB.class);
    }

    @Test
    public void testCreation() throws Exception {
        WhereClause wc = new WhereClause(mma);
        assertEquals(" WHERE ", wc.getClause().toString());
        assertTrue(wc.getParams().isEmpty());

        wc = new WhereClause(mma, "id", Operators.EQ, 100);
        assertEquals(" WHERE  " + mma.getTableName()+".id = ? ", wc.getClause().toString());
        assertEquals(1, wc.getParams().size());
    }

    @Test
    public void testPredication() throws Exception {
        WhereClause wc = new WhereClause(mma, "id", Operators.EQ, 100);
        wc.and("name", Operators.NE, "Isaac").or("score", Operators.GE, 60);
        String tn = mma.getTableName();
        String expecSql = " WHERE  " + tn + ".id = ?  AND  " + tn + ".name <> ?  OR  " + tn + ".score >= ? ";
        assertEquals(expecSql, wc.getClause().toString());
        assertEquals(3, wc.getParams().size());
        assertEquals(100, wc.getParams().get(0));
        assertEquals("Isaac", wc.getParams().get(1));
        assertEquals(60, wc.getParams().get(2));
    }

    @Test
    public void testBracketPredication() throws Exception {
        WhereClause wc = new WhereClause(mma, "id", Operators.EQ, 100);
        wc.or(w -> {
            w.predicate("id", 200).and("name", Operators.EQ, "foxty");
        }).and(w -> {
            w.predicate("id", 300).or("score", Operators.LT, 80);
        });
        String tn = mma.getTableName();
        String expSql = " WHERE  %s.id = ?  OR (  %s.id = ?  AND  %s.name = ?  ) " +
                " AND (  %s.id = ?  OR  %s.score < ?  ) ";
        expSql = String.format(expSql, tn, tn ,tn, tn, tn);
        assertEquals(expSql, wc.getClause().toString());
        assertEquals(5, wc.getParams().size());
    }
}
