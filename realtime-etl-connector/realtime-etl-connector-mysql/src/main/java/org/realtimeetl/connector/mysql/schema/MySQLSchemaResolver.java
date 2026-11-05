/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.mysql.schema;

import org.realtimeetl.connector.mysql.MySQLException;
import org.realtimeetl.connector.mysql.schema.support.MySQLByteType;
import org.realtimeetl.connector.mysql.schema.support.MySQLBytesType;
import org.realtimeetl.connector.mysql.schema.support.MySQLDateType;
import org.realtimeetl.connector.mysql.schema.support.MySQLDecimalType;
import org.realtimeetl.connector.mysql.schema.support.MySQLDoubleType;
import org.realtimeetl.connector.mysql.schema.support.MySQLFloatType;
import org.realtimeetl.connector.mysql.schema.support.MySQLIntType;
import org.realtimeetl.connector.mysql.schema.support.MySQLLongType;
import org.realtimeetl.connector.mysql.schema.support.MySQLShortType;
import org.realtimeetl.connector.mysql.schema.support.MySQLStringType;
import org.realtimeetl.connector.mysql.schema.support.MySQLTimeType;
import org.realtimeetl.connector.mysql.schema.support.MySQLTimestampType;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * MySQLStandard ContextTypeParser
 * <p>https://gitee.com/ghi/realtime-etl/wikis/%E9%A1%B9%E7%9B%AE%E8%AE%BE%E8%AE%A1/%E6%A0%87%E5%87%86%E6%95%B0%E6%8D%AE%E7%B1%BB%E5%9E%8B/MySQL</p>
 *
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-25 22:08
 */
public class MySQLSchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(
            new MySQLBytesType(),
            new MySQLByteType(),
            new MySQLDateType(),
            new MySQLDecimalType(),
            new MySQLDoubleType(),
            new MySQLFloatType(),
            new MySQLIntType(),
            new MySQLLongType(),
            new MySQLShortType(),
            new MySQLStringType(),
            new MySQLTimestampType(),
            new MySQLTimeType())
        .forEach(t->t.getSupportedTypeName().forEach(typeName-> {
            if (mapping.containsKey(typeName)) {
                throw new MySQLException("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }

}