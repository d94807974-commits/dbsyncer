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
package org.realtimeetl.connector.doris.schema;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.doris.DorisException;
import org.realtimeetl.connector.doris.schema.support.DorisBooleanType;
import org.realtimeetl.connector.doris.schema.support.DorisBytesType;
import org.realtimeetl.connector.doris.schema.support.DorisLargeIntType;
import org.realtimeetl.connector.doris.schema.support.DorisStringType;
import org.realtimeetl.connector.doris.schema.support.DorisTimestampType;
import org.realtimeetl.connector.mysql.schema.MySQLSchemaResolver;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.DataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Doris Standard ContextTypeParser
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 23:50
 */
public final class DorisSchemaResolver extends MySQLSchemaResolver {

    private static final Map<String, DataType> DORIS_TYPES = new HashMap<>();

    static {
        register(new DorisBytesType());
        register(new DorisTimestampType());
        register(new DorisStringType());
        register(new DorisLargeIntType());
        register(new DorisBooleanType());
    }

    private static void register(DataType dataType) {
        dataType.getSupportedTypeName().forEach(typeName -> {
            if (DORIS_TYPES.containsKey(typeName)) {
                throw new DorisException("Duplicate type name: " + typeName);
            }
            DORIS_TYPES.put(typeName, dataType);
        });
    }

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
        String normalized = normalizeTypeName(field.getTypeName());
        DataType dorisType = DORIS_TYPES.get(normalized);
        if (dorisType != null) {
            return dorisType;
        }
        DataType mysqlType = mapping.get(normalized);
        if (mysqlType != null) {
            return mysqlType;
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
