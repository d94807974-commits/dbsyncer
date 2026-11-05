/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.sqlserver.schema;

import org.realtimeetl.connector.sqlserver.SqlServerException;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerBooleanType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerByteType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerBytesType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerDateType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerDecimalType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerDoubleType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerFloatType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerIntType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerLongType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerShortType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerStringType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerTimeType;
import org.realtimeetl.connector.sqlserver.schema.support.SqlServerTimestampType;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2026-01-13 00:08
 */
public final class SqlServerSchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new SqlServerStringType(), new SqlServerIntType(), new SqlServerShortType(), new SqlServerLongType(), new SqlServerDecimalType(), new SqlServerFloatType(), new SqlServerDoubleType(), new SqlServerDateType(), new SqlServerTimestampType(), new SqlServerTimeType(), new SqlServerBooleanType(), new SqlServerBytesType(), new SqlServerByteType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new SqlServerException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}