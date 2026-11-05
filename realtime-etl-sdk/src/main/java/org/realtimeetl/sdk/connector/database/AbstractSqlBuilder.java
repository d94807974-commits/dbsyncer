/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.connector.database;

import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.database.sqlbuilder.SqlBuilder;

public abstract class AbstractSqlBuilder implements SqlBuilder {

    @Override
    public String buildQuerySql(SqlBuilderConfig config) {
        throw new SdkException("Not implemented");
    }
}
