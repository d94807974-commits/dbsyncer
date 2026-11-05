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

import java.util.List;

public final class CDCEvent {

    private String tableName;
    private int code;
    private List<Object> row;

    public CDCEvent(String tableName, int code, List<Object> row) {
        this.tableName = tableName;
        this.code = code;
        this.row = row;
    }

    public String getTableName() {
        return tableName;
    }

    public int getCode() {
        return code;
    }

    public List<Object> getRow() {
        return row;
    }
}
