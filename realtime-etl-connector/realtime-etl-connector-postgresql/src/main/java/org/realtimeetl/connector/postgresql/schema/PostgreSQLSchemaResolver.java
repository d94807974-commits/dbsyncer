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
package org.realtimeetl.connector.postgresql.schema;

import com.google.protobuf.ByteString;
import org.realtimeetl.connector.postgresql.PostgreSQLException;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLBooleanType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLBytesType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLDateType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLDecimalType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLDoubleType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLFloatType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLIntType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLLongType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLStringType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLTimeType;
import org.realtimeetl.connector.postgresql.schema.support.PostgreSQLTimestampType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PostgreSQLStandard ContextTypeParser
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-06-25 23:01
 */
public class PostgreSQLSchemaResolver extends AbstractDatabaseSchemaResolver {

    /**
     * NormativePostgreSQL TypeName: Removeschema Prefix(s)"public"."geometry" -> geometry）And quotes and lowercases.
     *
     * @param typeName OriginalTypeFirst Name
     * @return NormalizedTypeFirst Name
     */
    public static String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        // Processing tapeschema Prefix.Type，Like"public"."geometry" orpublic.geometry
        int dotIndex = typeName.lastIndexOf('.');
        if (dotIndex >= 0) {
            typeName = typeName.substring(dotIndex + 1);
        }
        // Remove quote
        typeName = typeName.replace("\"", "").replace("'", "").trim();
        return typeName.toLowerCase();
    }

    @Override
    protected DataType getDataType(Map<String, DataType> mapping, Field field) {
        // Use original first.TypeName search, if not standardized
        DataType dataType = mapping.get(field.getTypeName());
        if (dataType == null) {
            dataType = mapping.get(normalizeTypeName(field.getTypeName()));
        }
        // ArrayType（_int8 I'll be right back.PostgreSQLStringType Processing
        if (dataType == null && isArrayType(field.getTypeName())) {
            dataType = mapping.get("text");
        }
        return dataType;
    }

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new PostgreSQLStringType(), new PostgreSQLIntType(), new PostgreSQLLongType(), new PostgreSQLDecimalType(), new PostgreSQLFloatType(), new PostgreSQLDoubleType(), new PostgreSQLDateType(), new PostgreSQLTimestampType(), new PostgreSQLBooleanType(), new PostgreSQLBytesType(),new PostgreSQLTimeType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new PostgreSQLException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }

    @Override
    public ByteString serialize(Object value, Field field) {
        String type = value.getClass().getName();
        switch (type) {
            case "org.postgresql.util.PGobject":
                PGobject pgObject = (PGobject) value;
                return ByteString.copyFromUtf8(Objects.requireNonNull(pgObject.getValue()));
            case "org.postgresql.geometric.PGpoint":
                PGpoint pgpoint = (PGpoint) value;
                return ByteString.copyFromUtf8(Objects.requireNonNull(pgpoint.getValue()));
            default:
                break;
        }
        return super.serialize(value, field);
    }

    /**
     * PostgreSQL ArrayType：JDBC TYPE_NAME As   prefix (if)int8[] => _int8），DepartmentminThe scene isxxx[]。
     */
    public static boolean isArrayType(String typeName) {
        if (typeName == null) {
            return false;
        }
        String normalized = normalizeTypeName(typeName);
        return normalized.startsWith("_") || normalized.endsWith("[]");
    }
}