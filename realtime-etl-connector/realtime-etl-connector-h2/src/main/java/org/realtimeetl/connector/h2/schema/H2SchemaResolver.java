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
package org.realtimeetl.connector.h2.schema;

import org.realtimeetl.connector.h2.H2Exception;
import org.realtimeetl.connector.h2.schema.support.H2BooleanType;
import org.realtimeetl.connector.h2.schema.support.H2BytesType;
import org.realtimeetl.connector.h2.schema.support.H2DoubleType;
import org.realtimeetl.connector.h2.schema.support.H2IntType;
import org.realtimeetl.connector.h2.schema.support.H2LongType;
import org.realtimeetl.connector.h2.schema.support.H2StringType;
import org.realtimeetl.connector.h2.schema.support.H2TimestampType;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Map;
import java.util.stream.Stream;

/**
 * H2 TypeTable Group
 */
public final class H2SchemaResolver extends AbstractDatabaseSchemaResolver {

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(new H2StringType(), new H2IntType(), new H2LongType(), new H2DoubleType(), new H2BytesType(), new H2TimestampType(), new H2BooleanType()).forEach(t->t.getSupportedTypeName().forEach(typeName-> {
            if (mapping.containsKey(typeName)) {
                throw new H2Exception("Duplicate type name: " + typeName);
            }
            mapping.put(typeName, t);
        }));
    }
}
