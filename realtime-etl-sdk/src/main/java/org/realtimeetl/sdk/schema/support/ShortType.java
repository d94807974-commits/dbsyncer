/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema.support;

import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDataType;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-21 23:56
 */
public abstract class ShortType extends AbstractDataType<Short> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.SHORT;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).shortValue();
        }
        return throwUnsupportedException(val, field);
    }
}
