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
package org.realtimeetl.connector.kingbase.schema;

import com.google.protobuf.ByteString;
import com.kingbase8.geometric.KBpoint;
import com.kingbase8.util.KBobject;
import org.realtimeetl.connector.kingbase.KingbaseException;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLBooleanType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLBytesType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLDateType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLDecimalType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLDoubleType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLFloatType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLIntType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLLongType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLTimeType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseSQLTimestampType;
import org.realtimeetl.connector.kingbase.schema.support.KingbaseStringType;
import org.realtimeetl.connector.postgresql.schema.PostgreSQLSchemaResolver;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 18:30
 */
public final class KingbaseSchemaResolver extends PostgreSQLSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new KingbaseStringType(), new KingbaseSQLTimeType(), new KingbaseSQLTimestampType(), new KingbaseSQLLongType(),
                        new KingbaseSQLIntType(), new KingbaseSQLFloatType(), new KingbaseSQLDoubleType(),
                        new KingbaseSQLDecimalType(), new KingbaseSQLDateType(), new KingbaseSQLBytesType(), new KingbaseSQLBooleanType())
                .forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
                    if (mapping.containsKey(typeName)) {
                        throw new KingbaseException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }

    @Override
    public ByteString serialize(Object value, Field field) {
        String type = value.getClass().getName();
        switch (type) {
            case "com.kingbase8.util.KBobject":
                KBobject kbObject = (KBobject) value;
                return ByteString.copyFromUtf8(Objects.requireNonNull(kbObject.getValue()));
            case "com.kingbase8.geometric.KBpoint":
                KBpoint kbPoint = (KBpoint) value;
                return ByteString.copyFromUtf8(Objects.requireNonNull(kbPoint.getValue()));
            default:
                break;
        }
        return super.serialize(value, field);
    }
}
