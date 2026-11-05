/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
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
 * @author The clouds.
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
