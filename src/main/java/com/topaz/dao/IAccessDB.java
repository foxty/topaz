package com.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface IAccessDB {
	Object useDB(Connection conn) throws SQLException;
}
