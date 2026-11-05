/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
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
 * @author AE86
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
