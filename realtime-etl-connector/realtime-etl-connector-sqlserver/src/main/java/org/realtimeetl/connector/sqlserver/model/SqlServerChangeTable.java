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
package org.realtimeetl.connector.sqlserver.model;

import org.realtimeetl.common.util.JsonUtil;

public class SqlServerChangeTable {

    private String schemaName;
    private String tableName;
    private String captureInstance;
    private int changeTableObjectId;
    private byte[] startLsn;
    private byte[] stopLsn;
    private String capturedColumns;

    public SqlServerChangeTable(String schemaName, String tableName, String captureInstance, int changeTableObjectId, byte[] startLsn, byte[] stopLsn, String capturedColumns) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.captureInstance = captureInstance;
        this.capturedColumns = capturedColumns;
        this.changeTableObjectId = changeTableObjectId;
        this.startLsn = startLsn;
        this.stopLsn = stopLsn;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCaptureInstance() {
        return captureInstance;
    }

    public int getChangeTableObjectId() {
        return changeTableObjectId;
    }

    public byte[] getStartLsn() {
        return startLsn;
    }

    public byte[] getStopLsn() {
        return stopLsn;
    }

    public String getCapturedColumns() {
        return capturedColumns;
    }

    @Override
    public String toString() {
        return JsonUtil.objToJson(this);
    }
}
