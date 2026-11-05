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
package org.realtimeetl.sdk.schema.support;

import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDataType;

import java.sql.Time;

/**
 * @Author Realtime ETL.
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
