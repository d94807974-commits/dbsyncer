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
package org.realtimeetl.connector.http.schema;

import org.realtimeetl.connector.http.HttpException;
import org.realtimeetl.connector.http.schema.support.HttpBooleanType;
import org.realtimeetl.connector.http.schema.support.HttpByteType;
import org.realtimeetl.connector.http.schema.support.HttpBytesType;
import org.realtimeetl.connector.http.schema.support.HttpDateType;
import org.realtimeetl.connector.http.schema.support.HttpDecimalType;
import org.realtimeetl.connector.http.schema.support.HttpDoubleType;
import org.realtimeetl.connector.http.schema.support.HttpFloatType;
import org.realtimeetl.connector.http.schema.support.HttpIntType;
import org.realtimeetl.connector.http.schema.support.HttpLongType;
import org.realtimeetl.connector.http.schema.support.HttpShortType;
import org.realtimeetl.connector.http.schema.support.HttpStringType;
import org.realtimeetl.connector.http.schema.support.HttpTimestampType;
import org.realtimeetl.sdk.schema.AbstractSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-01-11 22:18
 */
public final class HttpSchemaResolver extends AbstractSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new HttpStringType(), new HttpIntType(), new HttpShortType(), new HttpLongType(), new HttpDecimalType(), new HttpFloatType(), new HttpDoubleType(), new HttpDateType(), new HttpTimestampType(), new HttpBooleanType(), new HttpBytesType(), new HttpByteType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new HttpException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}