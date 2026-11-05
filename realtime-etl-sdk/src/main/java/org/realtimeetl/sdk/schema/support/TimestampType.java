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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-11-21 23:56
 */
public abstract class TimestampType extends AbstractDataType<Timestamp> {

    @Override
    public DataTypeEnum getType() {
        return DataTypeEnum.TIMESTAMP;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Timestamp) {
            return val;
        }
        if (val instanceof String) {
            Timestamp timestamp = DateFormatUtil.stringToTimestamp((String) val);
            if (null != timestamp) {
                return timestamp;
            }
        }
        if (val instanceof Date) {
            Date date = (Date) val;
            return new Timestamp(date.getTime());
        }

        if (val instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) val;
            return Timestamp.valueOf(dateTime);
        }

        if (val instanceof LocalDate) {
            return Timestamp.valueOf(((LocalDate) val).atStartOfDay());
        }

        if (val instanceof byte[]) {
            byte[] bytes = (byte[]) val;
            String s = new String(bytes);
            Timestamp timestamp = DateFormatUtil.stringToTimestamp(s);
            if (null != timestamp) {
                return timestamp;
            }
        }

        if (val instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) val;
            return new Timestamp(date.getTime());
        }

        if (val instanceof OffsetDateTime) {
            OffsetDateTime date = (OffsetDateTime) val;
            return Timestamp.from(date.toInstant());
        }
        if (val instanceof Number) {
            return new Timestamp(((Number) val).longValue());
        }
        return throwUnsupportedException(val, field);
    }
}
