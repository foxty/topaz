package com.topaz.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.topaz.common.Config;

public class DaoManagerTransactionTest {

	@Before
	public void setUp() throws Exception {
		File cfgFile = new File("src/test/resources/config-test.properties");
		Config.init(cfgFile);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnectionConsistency() {
		DaoManager mgr = DaoManager.getInstance();
		mgr.transaction(new ITransVisitor() {
			public boolean visit() {
				Connection conn1 = (Connection) DaoManager.getInstance()
						.accessDB(new IConnVisitor() {
							public Object visit(Connection conn)
									throws SQLException {
								return conn;
							}

						});

				Connection conn2 = (Connection) DaoManager.getInstance()
						.accessDB(new IConnVisitor() {
							public Object visit(Connection conn)
									throws SQLException {
								return conn;
							}

						});
				boolean autoCommit = true;
				try {
					autoCommit = conn1.getAutoCommit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Assert.assertEquals(conn1, conn2);
				Assert.assertEquals(false, autoCommit);
				return true;
			}
		});
	}

	@Test
	public void testAutoCommitRecovery() {
		final DaoManager mgr = DaoManager.getInstance();
		final int numActive1 = mgr.getNumActive();
		mgr.transaction(new ITransVisitor() {

			public boolean visit() {

				Connection conn1 = (Connection) DaoManager.getInstance()
						.accessDB(new IConnVisitor() {
							public Object visit(Connection conn)
									throws SQLException {
								return conn;
							}
						});

				boolean autoCommit = true;
				try {
					autoCommit = conn1.getAutoCommit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Assert.assertEquals(false, autoCommit);
				int numActive2 = mgr.getNumActive();
				Assert.assertEquals(numActive1 + 1, numActive2);
				return true;
			}
		});
		int numActive3 = mgr.getNumActive();
		Assert.assertEquals(numActive1, numActive3);
	}
}
