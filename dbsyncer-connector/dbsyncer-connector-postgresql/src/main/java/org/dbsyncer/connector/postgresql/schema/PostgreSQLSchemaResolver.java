/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.connector.postgresql.schema;

import com.google.protobuf.ByteString;
import org.dbsyncer.connector.postgresql.PostgreSQLException;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLBooleanType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLBytesType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLDateType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLDecimalType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLDoubleType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLFloatType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLIntType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLLongType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLStringType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLTimeType;
import org.dbsyncer.connector.postgresql.schema.support.PostgreSQLTimestampType;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.AbstractDatabaseSchemaResolver;
import org.dbsyncer.sdk.schema.DataType;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * PostgreSQLStandard ContextTypeParser
 *
 * @author The clouds.
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