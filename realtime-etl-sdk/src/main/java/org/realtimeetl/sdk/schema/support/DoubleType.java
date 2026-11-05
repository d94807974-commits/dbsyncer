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
public abstract class DoubleType extends AbstractDataType<Double> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.DOUBLE;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return throwUnsupportedException(val, field);
    }
}
