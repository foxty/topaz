package com.github.foxty.topaz.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnVisitor {
	Object visit(Connection conn) throws SQLException;
}
