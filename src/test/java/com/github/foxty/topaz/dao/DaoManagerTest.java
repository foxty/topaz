package com.github.foxty.topaz.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import com.github.foxty.topaz.tool.Mocks;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.*;
import static org.junit.Assert.*;

import com.github.foxty.topaz.common.Config;

public class DaoManagerTest {

	static Config config;

	@BeforeClass
	public static void setUp() throws Exception {
		File cfgFile = new File(ClassLoader.class.getResource("/topaz.properties").getFile());
		Config.init(cfgFile);
		config = Config.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnectionPool() throws Exception {
		final DaoManager mgr = DaoManager.getInstance();
		GenericObjectPool pool = Mocks.getPrivateFieldValue(mgr, "connectionPool");
		assertEquals(config.getDbPoolMinIdle(), pool.getMinIdle());
		assertEquals(config.getDbPoolMaxIdle(), pool.getMaxIdle());
		assertEquals(config.getDbPoolMaxActive(), pool.getMaxActive());
		assertEquals(config.getDbPoolMaxWait(), pool.getMaxWait());
	}

	@Test
	public void testConnectionConsistency() {
		final DaoManager mgr = DaoManager.getInstance();
		mgr.useTransaction(() -> {
				Connection conn1 = mgr.useConnection(conn -> conn);

				Connection conn2 = mgr.useConnection(conn -> conn);

				boolean autoCommit = true;
				try {
					autoCommit = conn1.getAutoCommit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Assert.assertEquals(conn1, conn2);
				Assert.assertEquals(false, autoCommit);
		});
	}

	@Test
	public void testAutoCommitRecovery() {
		final DaoManager mgr = DaoManager.getInstance();
		final int numActive1 = mgr.getNumActive();
		mgr.useTransaction(() -> {
				Connection conn1 = mgr.useConnection(conn -> conn);

				boolean autoCommit = true;
				try {
					autoCommit = conn1.getAutoCommit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Assert.assertEquals(false, autoCommit);
				int numActive2 = mgr.getNumActive();
				Assert.assertEquals(numActive1 + 1, numActive2);
		});
		int numActive3 = mgr.getNumActive();
		Assert.assertEquals(numActive1, numActive3);
	}

	@Test
	public void testTransactionInTransaction() {
		final DaoManager mgr = DaoManager.getInstance();
		mgr.useTransaction( () -> {
				final Connection conn1 = mgr.useConnection(conn -> conn);

				mgr.useTransaction(() -> {
						Connection conn2 = mgr.useConnection(conn -> conn);
						Assert.assertEquals(conn1, conn2);
				});

				try {
					Assert.assertFalse(conn1.getAutoCommit());
				} catch (SQLException e) {
					e.printStackTrace();
				}
		});
	}
}
