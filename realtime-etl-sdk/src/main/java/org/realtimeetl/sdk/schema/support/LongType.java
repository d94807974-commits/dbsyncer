/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema.support;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDataType;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-21 23:56
 */
public abstract class LongType extends AbstractDataType<Long> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.LONG;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Number) {
            Number num = (Number) val;
            return num.longValue();
        }
        if (val instanceof String) {
            return NumberUtil.toLong((String) val);
        }
        if (val instanceof Boolean) {
            Boolean b = (Boolean) val;
            return (long) (b ? 1 : 0);
        }
        return throwUnsupportedException(val, field);
    }
}
