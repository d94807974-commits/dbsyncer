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
 * <p></p>
 *
 * @Author Realtime ETL.
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