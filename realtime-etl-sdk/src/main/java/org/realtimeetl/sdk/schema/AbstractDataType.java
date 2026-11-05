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

import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.model.Field;

import java.lang.reflect.ParameterizedType;

/**
 * @author Realtime ETL.
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
