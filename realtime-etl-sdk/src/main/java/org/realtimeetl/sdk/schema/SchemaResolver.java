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
package org.realtimeetl.sdk.schema;

import com.google.protobuf.ByteString;
import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;

/**
 * Data FactTypeParser
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2024-11-25 22:48
 */
public interface SchemaResolver {

    /**
     * ConvertAs Standard DataType
     *
     * @param val   ConvertValue
     * @param field Data FactType
     * @return Object
     */
    Object merge(Object val, Field field);

    /**
     * ConvertAssign DataType
     *
     * @param val   ConvertValue
     * @param field Data FactType
     * @return Object
     */
    Object convert(Object val, Field field);

    /**
     * Access criteriaFieldType
     */
    DataTypeEnum getFieldType(Field field);

    /**
     * SyncFinishdata to be sequenced for writingDisk，Use standard dataTypeParserConvert
     *
     * @param value SequencedValue
     */
    ByteString serialize(Object value, Field field);

    /**
     * Inverse sequence fromDiskRead data, use standard dataTypeParserConvert，BackOriginalValue
     * @param value SequencedValue
     * @param field Data FactType
     */
    Object deserialize(ByteString value, Field field);

}
