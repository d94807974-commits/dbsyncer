/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.clickhouse.schema;

import org.realtimeetl.connector.clickhouse.ClickHouseException;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseBooleanType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseBytesType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseDateType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseDecimalType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseDoubleType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseFloatType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseIntType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseLongType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseStringType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseTimeType;
import org.realtimeetl.connector.clickhouse.schema.support.ClickHouseTimestampType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ClickHouse Standard ContextTypeParser
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-05-29 23:50
 */
public final class ClickHouseSchemaResolver extends AbstractDatabaseSchemaResolver {

    public static String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        String normalized = typeName.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("nullable(") && normalized.endsWith(")")) {
            normalized = normalized.substring(9, normalized.length() - 1).trim();
        }
        if (normalized.startsWith("lowcardinality(") && normalized.endsWith(")")) {
            normalized = normalized.substring(15, normalized.length() - 1).trim();
        }
        int parenIndex = normalized.indexOf('(');
        if (parenIndex > 0) {
            normalized = normalized.substring(0, parenIndex).trim();
        }
        return normalized;
    }

    @Override
    protected DataType getDataType(Map<String, DataType> mapping, Field field) {
        DataType dataType = mapping.get(field.getTypeName());
        if (dataType == null) {
            dataType = mapping.get(normalizeTypeName(field.getTypeName()));
        }
        return dataType;
    }

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(
                new ClickHouseStringType(),
                new ClickHouseIntType(),
                new ClickHouseLongType(),
                new ClickHouseDecimalType(),
                new ClickHouseFloatType(),
                new ClickHouseDoubleType(),
                new ClickHouseDateType(),
                new ClickHouseTimestampType(),
                new ClickHouseTimeType(),
                new ClickHouseBooleanType(),
                new ClickHouseBytesType())
                .forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
                    if (mapping.containsKey(typeName)) {
                        throw new ClickHouseException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}
