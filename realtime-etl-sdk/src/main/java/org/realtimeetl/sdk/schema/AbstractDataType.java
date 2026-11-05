/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema;

import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.model.Field;

import java.lang.reflect.ParameterizedType;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2024-11-24 20:58
 */
public abstract class AbstractDataType<T> implements DataType {

    private final Class<T> parameterClazz;

    public AbstractDataType() {
        parameterClazz = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * ConvertAs Standard DataType
     */
    protected abstract T merge(Object val, Field field);

    /**
     * Get Default MergeValue
     */
    protected T getDefaultMergedVal(Field field) {
        return null;
    }

    /**
     * ConvertAssign DataType
     */
    protected abstract Object convert(Object val, Field field);

    /**
     * 71. Get DefaultConvertValue
     */
    protected Object getDefaultConvertedVal(Field field) {
        return null;
    }

    @Override
    public Object mergeValue(Object val, Field field) {
        if (val == null) {
            return getDefaultMergedVal(field);
        }
        // Data FactTypeMatch
        if (val.getClass().equals(parameterClazz)) {
            return val;
        }
        // Isomer DataTypeConvert
        return merge(val, field);
    }

    @Override
    public Object convertValue(Object val, Field field) {
        if (val == null) {
            return getDefaultConvertedVal(field);
        }
        // Isomer DataTypeConvert
        return convert(val, field);
    }

    protected T throwUnsupportedException(Object val, Field field) {
        throw new SdkException(String.format("%s does not support type [%s] convert to %s(%s), val [%s]", getClass().getSimpleName(), val.getClass(), field.getName(), field.getTypeName(), val));
    }
}
