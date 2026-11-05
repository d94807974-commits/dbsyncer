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
package org.realtimeetl.connector.sqlite.schema;

import org.realtimeetl.connector.sqlite.SQLiteException;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteBytesType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteDoubleType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteIntType;
import org.realtimeetl.connector.sqlite.schema.support.SQLiteStringType;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-01-13 00:08
 */
public final class SQLiteSchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new SQLiteStringType(), new SQLiteIntType(), new SQLiteDoubleType(), new SQLiteBytesType()).forEach(t->t.getSupportedTypeName().forEach(typeName-> {
            if (mapping.containsKey(typeName)) {
                throw new SQLiteException("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }
}