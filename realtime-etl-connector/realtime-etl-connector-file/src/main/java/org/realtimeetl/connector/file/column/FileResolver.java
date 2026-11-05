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
package org.realtimeetl.connector.file.column;

import org.realtimeetl.common.column.Lexer;
import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-05-06 00:04
 */
public class FileResolver {

    private final FileColumnValue value = new FileColumnValue();

    public Map<String, Object> parseMap(List<Field> fields, char separator, String line) {
        Map<String, Object> row = new ConcurrentHashMap<>();
        parse(fields, separator, line, (key, value)-> {
            if (value != null) {
                row.put(key, value);
            }
        });
        return row;
    }

    public List<Object> parseList(List<Field> fields, char separator, String line) {
        List<Object> data = new ArrayList<>();
        parse(fields, separator, line, (key, value)->data.add(value));
        return data;
    }

    /**
     * Resolve the value
     *
     * @param typeName
     * @param columnValue
     * @return
     */
    private Object resolveValue(String typeName, String columnValue) {
        value.setValue(columnValue);

        if (value.isNull()) {
            return null;
        }
        try {
            DataTypeEnum type = DataTypeEnum.getType(typeName);
            switch (type) {
                case STRING:
                    return value.asString();
                case INT:
                    return value.asInteger();
                case DATE:
                    return value.asDate();
                case TIMESTAMP:
                    return value.asTimestamp();
                case BOOLEAN:
                    return value.asBoolean();
                case LONG:
                    return value.asLong();
                case FLOAT:
                    return value.asFloat();
                case DOUBLE:
                    return value.asDouble();
                case TIME:
                    return value.asTime();
                case BYTE:
                    return value.asByte();
                case BYTES:
                    return value.asByteArray();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    private void parse(List<Field> fields, char separator, String line, ResultSetMapper mapper) {
        int fieldSize = fields.size();
        int i = 0;
        Lexer lexer = new Lexer(line);
        while (i < fieldSize) {
            if (lexer.hasNext()) {
                mapper.apply(fields.get(i).getName(), resolveValue(fields.get(i).getTypeName(), lexer.nextToken(separator)));
            } else {
                mapper.apply(fields.get(i).getName(), null);
            }
            i++;
        }
    }

    private interface ResultSetMapper {

        void apply(String key, Object value);
    }

}
