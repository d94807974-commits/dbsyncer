/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema.support;

import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDataType;

import java.sql.Time;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-21 23:56
 */
public abstract class TimeType extends AbstractDataType<Time> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.TIME;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Time) {
            return val;
        }
        if (val instanceof String) {
            return Time.valueOf(((String) val).trim());
        }
        return throwUnsupportedException(val, field);
    }
}
