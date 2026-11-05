/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Realtime ETL
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
