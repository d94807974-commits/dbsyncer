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
package org.realtimeetl.connector.duckdb.schema;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.duckdb.DuckDBException;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBBooleanType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBBytesType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBDateType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBDecimalType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBDoubleType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBHugeIntType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBIntType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBLongType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBStringType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBTimeType;
import org.realtimeetl.connector.duckdb.schema.support.DuckDBTimestampType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * DuckDB Standard ContextTypeParser
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-16 10:00
 */
public final class DuckDBSchemaResolver extends AbstractDatabaseSchemaResolver {

    /**
     * StripOutlook salute/Punctuation suffix, ifDECIMAL(10,2) → DECIMAL。
     */
    public static String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        String normalized = typeName.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ARRAY")) {
            return "ARRAY";
        }
        if (normalized.startsWith("MAP")) {
            return "MAP";
        }
        if (normalized.startsWith("STRUCT")) {
            return "STRUCT";
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
        if (dataType != null) {
            return dataType;
        }
        return mapping.get(normalizeTypeName(field.getTypeName()));
    }

    @Override
    public Object merge(Object val, Field field) {
        return super.merge(val, normalizeField(field));
    }

    @Override
    public Object convert(Object val, Field field) {
        return super.convert(val, normalizeField(field));
    }

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(
                new DuckDBStringType(),
                new DuckDBIntType(),
                new DuckDBLongType(),
                new DuckDBDecimalType(),
                new DuckDBDoubleType(),
                new DuckDBBooleanType(),
                new DuckDBBytesType(),
                new DuckDBDateType(),
                new DuckDBTimeType(),
                new DuckDBTimestampType(),
                new DuckDBHugeIntType()
        ).forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
            if (mapping.containsKey(typeName)) {
                throw new DuckDBException("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }

    private Field normalizeField(Field field) {
        String normalized = normalizeTypeName(field.getTypeName());
        if (StringUtil.equals(normalized, field.getTypeName())) {
            return field;
        }
        Field copy = new Field(field.getName(), normalized, field.getType(), field.isPk(),
                field.getColumnSize(), field.getRatio());
        copy.setLabelName(field.getLabelName());
        return copy;
    }
}
