/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle.schema;

import org.realtimeetl.sdk.schema.BindParameter;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Oracle BLOB Write binding (must use)ConnectTermsLOB，MERGE scenesetObject(byte[]) Report.17004 NoneActive ColumnsType）。
 *
 * @author wuji
 * @version 1.0.0
 */
public final class OracleBlobParameter implements BindParameter {

    private final byte[] value;

    public OracleBlobParameter(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public void setValue(PreparedStatement ps, int paramIndex, Connection connection) throws SQLException {
        if (value == null) {
            ps.setNull(paramIndex, Types.BLOB);
            return;
        }
        Blob blob = connection.createBlob();
        if (value.length > 0) {
            blob.setBytes(1, value);
        }
        ps.setBlob(paramIndex, blob);
    }
}
