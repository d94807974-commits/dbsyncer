/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.sdk.schema;

import com.google.protobuf.ByteString;
import org.dbsyncer.sdk.enums.DataTypeEnum;
import org.dbsyncer.sdk.model.Field;

/**
 * Data FactTypeParser
 *
 * @author The clouds.
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
