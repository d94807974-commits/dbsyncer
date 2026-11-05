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
package org.realtimeetl.connector.oceanbaseoracle.schema;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oceanbaseoracle.OceanBaseOracleException;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleBytesType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleDateType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleDecimalType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleDoubleType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleFloatType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleStringType;
import org.realtimeetl.connector.oceanbaseoracle.schema.support.OceanBaseOracleTimestampType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * OceanBase Oracle Model Standard DataTypeParser (standard)JDBC，Not dependentoracle.sql.*）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleSchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(
                new OceanBaseOracleBytesType(),
                new OceanBaseOracleDateType(),
                new OceanBaseOracleDecimalType(),
                new OceanBaseOracleDoubleType(),
                new OceanBaseOracleFloatType(),
                new OceanBaseOracleStringType(),
                new OceanBaseOracleTimestampType()
        ).forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
            if (mapping.containsKey(typeName)) {
                throw new OceanBaseOracleException("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }

    /**
     * NormativeTypeName: uppercase, removedOutlook saluteparenthesis.
     * <p>For example:{@code TIMESTAMP(6) WITH TIME ZONE} → {@code TIMESTAMP WITH TIME ZONE}</p>
     */
    public static String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        String normalized = typeName.trim().toUpperCase(Locale.ROOT);
        normalized = normalized.replaceAll("\\(\\s*\\d+\\s*(?:,\\s*\\d+\\s*)?\\)", "");
        return normalized.replaceAll("\\s+", " ").trim();
    }

    @Override
    protected DataType getDataType(Map<String, DataType> mapping, Field field) {
        String normalized = normalizeTypeName(field.getTypeName());
        DataType dataType = mapping.get(normalized);
        if (dataType != null) {
            return dataType;
        }
        if (normalized != null && normalized.startsWith("INTERVAL")) {
            return mapping.get("VARCHAR2");
        }
        return mapping.get(field.getTypeName());
    }

    @Override
    public Object merge(Object val, Field field) {
        return super.merge(val, normalizeField(field));
    }

    @Override
    public Object convert(Object val, Field field) {
        return super.convert(val, normalizeField(field));
    }

    private Field normalizeField(Field field) {
        String normalized = normalizeTypeName(field.getTypeName());
        if (StringUtil.equals(normalized, field.getTypeName())) {
            return field;
        }
        Field copy = new Field(field.getName(), normalized, field.getType(), field.isPk(), field.getColumnSize(), field.getRatio());
        copy.setLabelName(field.getLabelName());
        return copy;
    }
}
