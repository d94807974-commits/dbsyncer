/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.sqlite.schema;

import org.realtimeetl.connector.sqlite.SQLiteException;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteBytesType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteDoubleType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteIntType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteStringType;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-01-13 00:08
 */
public final class SQLiteSchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new SQLiteStringType(), new SQLiteIntType(), new SQLiteDoubleType(), new SQLiteBytesType()).forEach(t->t.getSupportedTypeName().forEach(typeName-> {
            if (mapping.containsKey(typeName)) {
                throw new SQLiteException("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }
}