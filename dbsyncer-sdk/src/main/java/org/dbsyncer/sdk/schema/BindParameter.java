/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Custom JDBC ParameterTie (if)Oracle CLOB To passConnectAfter CreatesetClob）。
 */
public interface BindParameter {

    void setValue(PreparedStatement ps, int paramIndex, Connection connection) throws SQLException;
}
