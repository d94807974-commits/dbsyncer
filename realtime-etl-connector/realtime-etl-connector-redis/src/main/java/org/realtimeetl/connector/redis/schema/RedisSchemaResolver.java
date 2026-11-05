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
package org.realtimeetl.connector.redis.schema;

import org.realtimeetl.connector.redis.RedisException;
import org.realtimeetl.connector.redis.schema.support.RedisBooleanType;
import org.realtimeetl.connector.redis.schema.support.RedisByteType;
import org.realtimeetl.connector.redis.schema.support.RedisBytesType;
import org.realtimeetl.connector.redis.schema.support.RedisDateType;
import org.realtimeetl.connector.redis.schema.support.RedisDecimalType;
import org.realtimeetl.connector.redis.schema.support.RedisDoubleType;
import org.realtimeetl.connector.redis.schema.support.RedisFloatType;
import org.realtimeetl.connector.redis.schema.support.RedisIntType;
import org.realtimeetl.connector.redis.schema.support.RedisLongType;
import org.realtimeetl.connector.redis.schema.support.RedisShortType;
import org.realtimeetl.connector.redis.schema.support.RedisStringType;
import org.realtimeetl.connector.redis.schema.support.RedisTimeType;
import org.realtimeetl.connector.redis.schema.support.RedisTimestampType;
import org.realtimeetl.sdk.schema.AbstractSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-01-11 22:18
 */
public final class RedisSchemaResolver extends AbstractSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new RedisStringType(), new RedisIntType(), new RedisShortType(), new RedisLongType(), new RedisDecimalType(), new RedisFloatType(), new RedisDoubleType(), new RedisDateType(), new RedisTimestampType(), new RedisTimeType(), new RedisBooleanType(), new RedisBytesType(), new RedisByteType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new RedisException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}