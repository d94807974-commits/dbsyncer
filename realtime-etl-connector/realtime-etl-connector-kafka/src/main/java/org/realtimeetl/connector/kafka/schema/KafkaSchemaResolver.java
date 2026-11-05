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
package org.realtimeetl.connector.kafka.schema;

import org.realtimeetl.connector.kafka.KafkaException;
import org.realtimeetl.connector.kafka.schema.support.KafkaBooleanType;
import org.realtimeetl.connector.kafka.schema.support.KafkaByteType;
import org.realtimeetl.connector.kafka.schema.support.KafkaBytesType;
import org.realtimeetl.connector.kafka.schema.support.KafkaDateType;
import org.realtimeetl.connector.kafka.schema.support.KafkaDecimalType;
import org.realtimeetl.connector.kafka.schema.support.KafkaDoubleType;
import org.realtimeetl.connector.kafka.schema.support.KafkaFloatType;
import org.realtimeetl.connector.kafka.schema.support.KafkaIntType;
import org.realtimeetl.connector.kafka.schema.support.KafkaLongType;
import org.realtimeetl.connector.kafka.schema.support.KafkaShortType;
import org.realtimeetl.connector.kafka.schema.support.KafkaStringType;
import org.realtimeetl.connector.kafka.schema.support.KafkaTimeType;
import org.realtimeetl.connector.kafka.schema.support.KafkaTimestampType;
import org.realtimeetl.sdk.schema.AbstractSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-01-11 22:18
 */
public class KafkaSchemaResolver extends AbstractSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new KafkaStringType(), new KafkaIntType(), new KafkaShortType(), new KafkaLongType(), new KafkaDecimalType(), new KafkaFloatType(), new KafkaDoubleType(), new KafkaDateType(), new KafkaTimestampType(), new KafkaTimeType(), new KafkaBooleanType(), new KafkaBytesType(), new KafkaByteType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new KafkaException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}