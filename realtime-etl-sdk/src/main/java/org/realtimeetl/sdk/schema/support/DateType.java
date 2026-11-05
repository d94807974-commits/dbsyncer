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

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.sdk.enums.DataTypeEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.AbstractDataType;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-11-21 23:56
 */
public abstract class DateType extends AbstractDataType<Date> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.DATE;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Date) {
            return val;
        }

        if (val instanceof Timestamp) {
            return val;
        }

        if (val instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) val;
            return Timestamp.valueOf(dateTime);
        }

        if (val instanceof String) {
            String s = (String) val;
            Timestamp timestamp = DateFormatUtil.stringToTimestamp(s);
            if (null != timestamp) {
                return timestamp;
            }
        }
        return throwUnsupportedException(val, field);
    }
}
