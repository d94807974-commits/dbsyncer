/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.elasticsearch.schema;

import org.realtimeetl.connector.elasticsearch.ElasticsearchException;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchBooleanType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchByteType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchBytesType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchDecimalType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchDoubleType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchFloatType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchIntType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchLongType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchStringType;
import org.realtimeetl.connector.elasticsearch.schema.support.ElasticsearchTimestampType;
import org.realtimeetl.sdk.schema.AbstractSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Elasticsearch Isomer DataTypeParser
 * SupportES All standard dataTypeMap andConvert
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-01-13 00:16
 */
public final class ElasticsearchSchemaResolver extends AbstractSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new ElasticsearchStringType(), new ElasticsearchIntType(), new ElasticsearchLongType(), new ElasticsearchDecimalType(), new ElasticsearchFloatType(), new ElasticsearchDoubleType(), new ElasticsearchTimestampType(), new ElasticsearchBooleanType(), new ElasticsearchBytesType(), new ElasticsearchByteType())
                .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
                    if (mapping.containsKey(typeName)) {
                        throw new ElasticsearchException("Duplicate type name: " + typeName);
                    }
                    mapping.put(typeName, t);
                }));
    }
}