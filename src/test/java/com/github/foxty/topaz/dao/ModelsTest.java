package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.meta.RelationMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by itian on 6/22/2017.
 */
public class ModelsTest {

    static ModelMeta mma;
    static ModelMeta mmb;

    @BeforeClass
    public static void beforeClass() {
        Models.getInstance().register(ModelA.class, ModelB.class);
        mma = Models.getInstance().getModelMeta(ModelA.class);
        mmb = Models.getInstance().getModelMeta(ModelB.class);
    }

    @Test
    public void testModelAnnontations() {

        assertEquals(4, mma.getColumnMetaMap().size());
        assertEquals(1, mma.getRelationMetaMap().size());
        assertTrue(mma.getColumnMetaMap().containsKey("id"));
        assertTrue(mma.getColumnMetaMap().containsKey("name"));
        assertTrue(mma.getColumnMetaMap().containsKey("score"));
        assertTrue(mma.getColumnMetaMap().containsKey("bornDate"));
        assertEquals("table_name_a", mma.getTableName());

        assertEquals(4, mmb.getColumnMetaMap().size());
        assertTrue(mmb.getRelationMetaMap().isEmpty());
        assertEquals("model_b", mmb.getTableName());

        assertEquals("id", mma.findColumnMeta("id").getColumnName());
        assertEquals("id", mma.findColumnMeta("modelb.id").getColumnName());
        assertEquals("name", mma.findColumnMeta("modelb.name").getColumnName());
    }

    @Test
    public void testColumnAnno() {
        ColumnMeta cmName = mma.findColumnMeta("name");
        ColumnMeta cmScore = mma.findColumnMeta("score");

        assertEquals("table_name_a", cmName.getTableName());
        assertEquals("aname", cmName.getColumnName());
        assertEquals("name", cmName.getFieldName());
        assertEquals(String.class, cmName.getFieldClazz());

        ColumnMeta cmExpiredAt = mmb.findColumnMeta("expiredAt");
        assertEquals("model_b", cmExpiredAt.getTableName());
        assertEquals("expired_date_on", cmExpiredAt.getColumnName());
    }

    @Test
    public void testRelationAnno() throws Exception {
        RelationMeta relation = mma.getRelationMeta("modelb");
        assertEquals(ModelB.class, relation.getFieldClazz());
        assertEquals(Relation.HasOne, relation.getRelation());
        assertEquals("table_name_a_id", relation.byKey());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiedColumnMap() throws Exception {
        mma.getColumnMetaMap().put("a", null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiedRelationMap() throws Exception {
        mma.getRelationMetaMap().put("a", null);
    }
}
