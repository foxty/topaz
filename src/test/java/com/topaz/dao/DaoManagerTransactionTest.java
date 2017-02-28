package com.topaz.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.*;

import com.topaz.common.Config;

public class DaoManagerTransactionTest {

	@Before
	public void setUp() throws Exception {
		File cfgFile = new File(ClassLoader.class.getResource("/topaz.properties").getFile());
		Config.init(cfgFile);
	}

	@After
	public void tearDown() throws Exception {
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
