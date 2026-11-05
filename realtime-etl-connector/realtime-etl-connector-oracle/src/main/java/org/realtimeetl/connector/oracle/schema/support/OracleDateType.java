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
package org.realtimeetl.connector.oracle.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.DateType;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-12-25 00:03
 */
public final class OracleDateType extends DateType {

    private enum TypeEnum {

        DATE("DATE");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected Date merge(Object val, Field field) {
        if (val instanceof Date) {
            return (Date) val;
        }
        if (val instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) val;
            return new Date(timestamp.getTime());
        }
        if (val instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) val;
            return new Date(date.getTime());
        }
        if (val instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) val;
            Timestamp timestamp = Timestamp.valueOf(dateTime);
            return new Date(timestamp.getTime());
        }
        if (val instanceof String) {
            String s = (String) val;
            // Try Approximate AsTimestamp（Support tapeTimestring)
            Timestamp timestamp = DateFormatUtil.stringToTimestamp(s);
            if (timestamp != null) {
                return new Date(timestamp.getTime());
            }
            // Try to resolve directly toDate（AI only)
            return DateFormatUtil.stringToDate(s);
        }
        return throwUnsupportedException(val, field);
    }
}
