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
package org.realtimeetl.sdk.enums;

import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilder;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderDelete;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderInsert;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderQuery;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderQueryCount;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderQueryCursor;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilderUpdate;
import org.realtimeetl.sdk.constant.ConnectorConstant;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/28 22:13
 */
public enum SqlBuilderEnum {

    /**
     * InsertSQLGenerateDevice
     */
    INSERT(ConnectorConstant.OPERTION_INSERT, new SqlBuilderInsert()),
    /**
     * UpdateSQLGenerateDevice
     */
    UPDATE(ConnectorConstant.OPERTION_UPDATE, new SqlBuilderUpdate()),
    /**
     * DeleteSQLGenerateDevice
     */
    DELETE(ConnectorConstant.OPERTION_DELETE, new SqlBuilderDelete()),
    /**
     * QuestionSQLGenerateDevice
     */
    QUERY(ConnectorConstant.OPERTION_QUERY, new SqlBuilderQuery()),

    /**
     * QuestionObjective Combined SQLGenerateDevice
     */
    QUERY_TARGET(ConnectorConstant.OPERTION_QUERY_TARGET, new SqlBuilderQuery()),

    /**
     * QuestionSource tableCursorSQLGenerateDevice
     */
    QUERY_CURSOR(ConnectorConstant.OPERTION_QUERY_CURSOR, new SqlBuilderQueryCursor()),

    /**
     * QuestionTarget tableCursorSQLGenerateDevice
     */
    QUERY_TARGET_CURSOR(ConnectorConstant.OPERTION_QUERY_TARGET_CURSOR, new SqlBuilderQueryCursor()),

    /**
     * QuestionTotal
     */
    QUERY_COUNT(ConnectorConstant.OPERTION_QUERY_COUNT, new SqlBuilderQueryCount()),
    /**
     * QuestionObjective CombinedTotal
     */
    TARGET_QUERY_COUNT(ConnectorConstant.TARGET_QUERY_COUNT, new SqlBuilderQueryCount());

    /**
     * SQLConstructorName
     */
    private final String name;

    /**
     * SQLConstructor
     */
    private final SqlBuilder sqlBuilder;

    SqlBuilderEnum(String name, SqlBuilder sqlBuilder) {
        this.name = name;
        this.sqlBuilder = sqlBuilder;
    }

    public String getName() {
        return name;
    }

    public SqlBuilder getSqlBuilder() {
        return sqlBuilder;
    }
}
