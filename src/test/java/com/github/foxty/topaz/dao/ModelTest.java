package com.github.foxty.topaz.dao;

import com.github.foxty.topaz.common.Config;
import com.sun.scenario.effect.Offset;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.cglib.core.Local;

import static com.github.foxty.topaz.dao.sql.SQLDelete.fn.deleteById;
import static com.github.foxty.topaz.dao.sql.SQLSelect.fn.findById;
import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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
    public static void before() throws Exception {

    }


    @Test
    public void testFindByIdAndDeleteById() throws Exception {
        ModelA a1 = new ModelA();
        a1.setName("foxty");
        a1.setBornDate(LocalDateTime.now());
        a1.setScore(100);
        a1.save();
        assertNotNull(a1.getId());
        System.out.println("Saved ModelA with id " + a1.getId());

        ModelA a2 = findById(ModelA.class, -1);
        assertNull(a2);
        a2 = findById(ModelA.class, a1.getId());
        assertNotNull(a2);
        assertEquals(a1.getId(), a2.getId());
        assertEquals(a1.getName(), a2.getName());
        assertEquals(a1.getBornDate(), a2.getBornDate());
        assertEquals(a1.getScore(), a2.getScore());

        boolean deleted = deleteById(ModelA.class, a2.getId());
        assertTrue(deleted);
        a2 = findById(ModelA.class, a2.getId());
        assertNull(a2);
    }
}
