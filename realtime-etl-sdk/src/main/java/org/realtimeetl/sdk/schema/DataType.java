/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema;

import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;

import java.util.Set;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-23 23:00
 */
public interface DataType {

    /**
     * SupportedConvertType
     */
    Set<String> getSupportedTypeName();

    /**
     * ConvertAs Standard DataType
     *
     * @param val   ConvertValue
     * @param field Data FactType
     * @return Object
     */
    Object mergeValue(Object val, Field field);

    /**
     * ConvertAssign DataType
     *
     * @param val   ConvertValue
     * @param field Data FactType
     * @return Object
     */
    Object convertValue(Object val, Field field);

    /**
     * Getting dataType
     */
    DataTypeEnum getType();
}
