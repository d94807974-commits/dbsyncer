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
package org.realtimeetl.connector.tidb.schema;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.mysql.schema.MySQLSchemaResolver;
import org.realtimeetl.connector.tidb.TiDBException;
import org.realtimeetl.connector.tidb.schema.support.TiDBStringType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.DataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * TiDB Standard ContextTypeParser
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 00:10
 */
public final class TiDBSchemaResolver extends MySQLSchemaResolver {

    private static final Map<String, DataType> TIDB_TYPES = new HashMap<>();

    static {
        register(new TiDBStringType());
    }

    private static void register(DataType dataType) {
        dataType.getSupportedTypeName().forEach(typeName -> {
            if (TIDB_TYPES.containsKey(typeName)) {
                throw new TiDBException("Duplicate type name: " + typeName);
            }
            TIDB_TYPES.put(typeName, dataType);
        });
    }

    public static String normalizeTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        String normalized = typeName.trim().toUpperCase(Locale.ROOT);
        int parenIndex = normalized.indexOf('(');
        if (parenIndex > 0) {
            normalized = normalized.substring(0, parenIndex).trim();
        }
        return normalized;
    }

    @Override
    protected DataType getDataType(Map<String, DataType> mapping, Field field) {
        String normalized = normalizeTypeName(field.getTypeName());
        DataType tidbType = TIDB_TYPES.get(normalized);
        if (tidbType != null) {
            return tidbType;
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
