package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.common.Config;
import com.github.foxty.topaz.dao.sql.Operators;
import com.github.foxty.topaz.dao.sql.SQLSelect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.foxty.topaz.dao.sql.SQLDelete.fn.deleteById;
import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.find;
import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.findById;
import static org.junit.Assert.*;

/**
 * Created by itian on 6/14/2017.
 */
public class ModelTest {

    static Config config;

    @BeforeClass
    public static void setUp() throws Exception {
        File cfgFile = new File(ClassLoader.class.getResource("/topaz.properties").getFile());
        Config.init(cfgFile);
        config = Config.getInstance();
    }

    @Before
    public void before() throws Exception {

    }

    @Test
    public void testFind() throws Exception {
        // find by id
        ModelA a = findById(ModelA.class, -1);
        assertNull(a);
        a = findById(ModelA.class, 1);
        assertNotNull(a);
        assertEquals(1, a.getId().intValue());
        assertEquals("test_data_1", a.getName());
        assertTrue(a.getBornDate().isBefore(LocalDateTime.now()));
        assertEquals(0, a.getScore().intValue());

        // find list
        List<ModelA> list = find(ModelA.class).where("score", Operators.GT, 0).fetch();
        assertEquals(3, list.size());
        for(ModelA ma: list) {
            assertTrue(ma.getName().startsWith("test_data_"));
            assertTrue(ma.getScore() > 0);
        }

        // find count
        long c = find(ModelA.class).count();
        assertEquals(4, c);
    }

    @Test
    public void testFindWith() throws Exception {
        SQLSelect ss = find(ModelA.class, "modelb")
                .where("id", 2);
        ModelA a = ss.first();
        assertEquals(2, a.getId().intValue());
        assertEquals("test_data_2", a.getName());
        assertEquals(3, a.getModelb().getId().intValue());
        System.out.println(ss.toString());

        ModelA a1 = find(ModelA.class, "modelcList").where("id", 2).first();
        assertEquals(2, a1.getId().intValue());
    }

    @Test
    public void testSave() throws Exception {
        ModelA a1 = new ModelA();
        a1.setName("foxty");
        a1.setBornDate(LocalDateTime.now());
        a1.setScore(100);
        a1.save();
        assertNotNull(a1.getId());
        System.out.println("Saved ModelA with id " + a1.getId());
    }

    @Test
    public void testUpdate() throws Exception {
        ModelA a = findById(ModelA.class, 1);
        assertEquals("test_data_1", a.getName());

        a.setName("model_a_1");
        boolean re = a.updated();
        assertTrue(re);
        ModelA a1 = findById(ModelA.class, 1);
        assertEquals("model_a_1", a.getName());
    }

    @Test
    public void testDelete() throws Exception {
        boolean deleted = deleteById(ModelA.class, 4);
        assertTrue(deleted);
        ModelA a = findById(ModelA.class, 4);
        assertNull(a);
    }
}
