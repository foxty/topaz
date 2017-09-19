package com.github.foxty.topaz.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.foxty.topaz.dao.meta.ColumnMeta;
import com.github.foxty.topaz.dao.meta.ModelMeta;
import com.github.foxty.topaz.dao.meta.RelationMeta;

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

        assertEquals(5, mma.getColumnMetaMap().size());
        assertEquals(2, mma.getRelationMetaMap().size());
        assertTrue(mma.getColumnMetaMap().containsKey("id"));
        assertTrue(mma.getColumnMetaMap().containsKey("name"));
        assertTrue(mma.getColumnMetaMap().containsKey("score"));
        assertTrue(mma.getColumnMetaMap().containsKey("bornDate"));
        assertEquals("model_a", mma.getTableName());

        assertEquals(6, mmb.getColumnMetaMap().size());
        assertTrue(mmb.getRelationMetaMap().isEmpty());
        assertEquals("model_b", mmb.getTableName());

        assertEquals("id", mma.findColumnMeta("id").getColumnName());
        assertEquals("name", mma.findColumnMeta("name").getColumnName());
        assertEquals("id", mma.findColumnMeta("modelb.id").getColumnName());
        assertEquals("name", mma.findColumnMeta("modelb.name").getColumnName());
    }

    @Test
    public void testColumnAnno() {
        ColumnMeta cmName = mma.findColumnMeta("name");

        assertEquals("model_a", cmName.getTableName());
        assertEquals("name", cmName.getColumnName());
        assertEquals("name", cmName.getFieldName());
        assertEquals(String.class, cmName.getFieldClazz());
        
        ColumnMeta bornAt = mma.findColumnMeta("born_at");
        assertEquals("born_at", bornAt.getColumnName()); 
        ColumnMeta bornDate = mma.findColumnMeta("bornDate");
        assertEquals("born_at", bornDate.getColumnName()); 
        assertEquals(bornAt, bornDate);
        
        ColumnMeta cmExpiredAt = mmb.findColumnMeta("expiredDateOn");
        assertEquals("model_b", cmExpiredAt.getTableName());
        assertEquals("expired_date_on", cmExpiredAt.getColumnName());
    }

    @Test
    public void testRelationAnno() throws Exception {
        RelationMeta relation = mma.findRealtionMega("modelb");
        assertEquals(ModelB.class, relation.getFieldClazz());
        assertEquals(ModelB.class, relation.getModelClazz());
        assertEquals(Relation.HasOne, relation.getRelation());
        assertEquals("model_a_id", relation.byKey());

        RelationMeta relationC = mma.findRealtionMega("modelcList");
        assertEquals(Relation.HasMany, relationC.getRelation());
        assertEquals(ModelC.class, relationC.getModelClazz());
        assertEquals(ArrayList.class, relationC.getFieldClazz());
        assertEquals("model_a_id", relationC.byKey());
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
