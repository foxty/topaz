package com.github.foxty.topaz.controller;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.reflections.ReflectionUtils;

import javax.servlet.FilterConfig;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by foxty on 17/6/14.
 */
public class CoreFilterTest {

    String cfgFile = ClassLoader.class.getResource("/topaz.properties").getFile();

    @BeforeClass
    public static void setup() {
    }

    @Test
    public void testInit() throws Exception {
        CoreFilter filter = new CoreFilter();
        FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.when(config.getInitParameter("controllerPackage")).thenReturn("");
        Mockito.when(config.getInitParameter("viewBase")).thenReturn("/config/");
        Mockito.when(config.getInitParameter("configFile")).thenReturn(cfgFile);
        Mockito.when(config.getInitParameter("xssFilterOn")).thenReturn("false");

        filter.init(config);
    }
}
