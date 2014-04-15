package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.topaz.common.Config;

/**
 * Manage all Daos initialization, provide JDBC collection access and
 * transaction service.
 * 
 * @author itian
 */
public class DaoManager {

	private static Log log = LogFactory.getLog(DaoManager.class);
	private final static DaoManager instance = new DaoManager();
	private final static ThreadLocal<Connection> LOCAL_CONN = new ThreadLocal<Connection>();
	private final GenericObjectPool<Connection> connectionPool;
	private final PoolingDataSource ds;

	private DaoManager() {
		Config c = Config.getInstance();
		try {
			Class.forName(c.getDbDriver());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		connectionPool = new GenericObjectPool<Connection>(null);
		connectionPool.setMaxIdle(c.getDbPoolMaxIdle());
		connectionPool.setMinIdle(c.getDbPoolMinIdle());
		connectionPool.setMaxActive(c.getDbPoolMaxActive());
		connectionPool.setMaxWait(c.getDbPoolMaxWait());

		Properties props = new Properties();
		props.setProperty("user", c.getDbUsername());
		props.setProperty("password", c.getDbPassword());
		props.setProperty("characterEncoding", "UTF-8");
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				c.getDbUrl(), props);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
				connectionFactory, connectionPool, null, "SELECT 1", false,
				true);
		ds = new PoolingDataSource(connectionPool);
	}

	public static DaoManager getInstance() {
		return instance;
	}

	public DataSource getDataSource() {
		return this.ds;
	}

	public int getNumActive() {
		return connectionPool.getNumActive();
	}

	private boolean isInTransaction() {
		return (LOCAL_CONN.get() != null);
	}

	@SuppressWarnings("unchecked")
	public <T> T accessDB(IConnVisitor inter) {
		Connection conn = prepareConnection();
		Object result = null;
		try {
			result = inter.visit(conn);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DaoException(e);
		} finally {
			closeConnection(conn);
		}
		return (T) result;
	}

	private Connection prepareConnection() {
		Connection curConn = LOCAL_CONN.get();
		if (curConn == null) {
			try {
				logPoolStatus();
				curConn = ds.getConnection();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
		}
		return curConn;
	}

	private void closeConnection(Connection conn) {
		if (!isInTransaction()) {
			try {
				DbUtils.close(conn);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
		} else {
			log.debug("In Transaction mode, conenction " + conn
					+ " will not close!");
		}
	}

	public void logPoolStatus() {
		if (log.isDebugEnabled()) {
			StringBuffer re = new StringBuffer(
					"[ConnectionPool status before get connection: NumActive/MaxActive=");
			re.append(connectionPool.getNumActive()).append("/")
					.append(connectionPool.getMaxActive());
			re.append(", MinIdle/NumIdle/MaxIdle=");
			re.append(connectionPool.getMinIdle()).append("/")
					.append(connectionPool.getNumIdle()).append("/")
					.append(connectionPool.getMaxIdle()).append("]");
			log.debug(re);
		}
	}

	public boolean useTransaction(ITransVisitor inter) {
		// Start transaction set transaction flag
		Connection conn = prepareConnection();
		LOCAL_CONN.set(conn);
		try {
			conn.setAutoCommit(false);
			boolean re = inter.visit();
			if (re)
				conn.commit();
			else
				conn.rollback();

			return re;
		} catch (Exception e) {
			// Roll back
			try {
				conn.rollback();
			} catch (SQLException e1) {
				log.error(e1.getMessage(), e1);
				throw new DaoException(e1);
			}

			// Re throw exception
			if (e instanceof DaoException) {
				throw (DaoException) e;
			} else {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
		} finally {
			// End transaction set flat to null
			LOCAL_CONN.set(null);
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				throw new DaoException(e);
			}
			closeConnection(conn);
		}
	}
}