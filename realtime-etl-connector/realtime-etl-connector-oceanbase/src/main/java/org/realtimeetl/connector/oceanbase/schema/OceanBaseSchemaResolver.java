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
package org.realtimeetl.connector.oceanbase.schema;

import org.realtimeetl.connector.oceanbase.OceanBaseException;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseByteType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseBytesType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseDateType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseDecimalType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseDoubleType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseFloatType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseIntType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseLongType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseShortType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseStringType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseTimeType;
import org.realtimeetl.connector.oceanbase.schema.support.OceanBaseTimestampType;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDatabaseSchemaResolver;
import org.realtimeetl.sdk.schema.DataType;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * OceanBaseStandard ContextTypeParser
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-04 00:20
 */
public final class OceanBaseSchemaResolver extends AbstractDatabaseSchemaResolver {

    /**
     * NormativeTypeName: Capitalise
     */
    public static String normalizeTypeName(String typeName) {
        return typeName.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    protected DataType getDataType(Map<String, DataType> mapping, Field field) {
        DataType dataType = mapping.get(field.getTypeName());
        if (dataType == null) {
            dataType = mapping.get(normalizeTypeName(field.getTypeName()));
        }
        return dataType;
    }

    @Override
    protected void initDataTypeMapping(Map<String, DataType> mapping) {
        Stream.of(
                        new OceanBaseBytesType(),
                        new OceanBaseByteType(),
                        new OceanBaseDateType(),
                        new OceanBaseDecimalType(),
                        new OceanBaseDoubleType(),
                        new OceanBaseFloatType(),
                        new OceanBaseIntType(),
                        new OceanBaseLongType(),
                        new OceanBaseShortType(),
                        new OceanBaseStringType(),
                        new OceanBaseTimestampType(),
                        new OceanBaseTimeType())
                .forEach(t -> t.getSupportedTypeName().forEach(typeName -> {
                    String key = normalizeTypeName(typeName);
                    if (mapping.containsKey(key)) {
                        throw new OceanBaseException("Duplicate type name: " + key);
                    }
                    mapping.put(key, t);
                }));
    }

}