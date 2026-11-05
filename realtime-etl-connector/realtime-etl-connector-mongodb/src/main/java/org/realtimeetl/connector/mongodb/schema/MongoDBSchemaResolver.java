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
package org.realtimeetl.connector.mongodb.schema;

import org.realtimeetl.connector.mongodb.MongoDBException;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBBooleanType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBDecimalType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBDoubleType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBIntType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBLongType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBStringType;
import org.realtimeetl.connector.mongodb.schema.support.MongoDBTimestampType;
import org.realtimeetl.sdk.schema.AbstractSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public final class MongoDBSchemaResolver extends AbstractSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new MongoDBStringType(), new MongoDBIntType(), new MongoDBLongType(), new MongoDBDecimalType(), new MongoDBDoubleType(), new MongoDBTimestampType(), new MongoDBBooleanType())
                .forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
                    if (mapping.containsKey(typeName)) {
                        throw new MongoDBException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}
