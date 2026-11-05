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
package org.realtimeetl.connector.oceanbase.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.TimestampType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseTimestampType extends TimestampType {

    private enum TypeEnum {
        DATETIME, TIMESTAMP;
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected Timestamp merge(Object val, Field field) {
        if (val instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) val;
            return new Timestamp(date.getTime());
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (isDatetime(field)) {
            if (val instanceof Timestamp) {
                return DateFormatUtil.timestampToString((Timestamp) val);
            }
            if (val instanceof LocalDateTime) {
                return ((LocalDateTime) val).format(DateFormatUtil.YYYY_MM_DD_HH_MM_SS);
            }
        }
        if (val instanceof String) {
            if (StringUtil.equals((String) val, "0000-00-00 00:00:00")) {
                return val;
            }
        }
        return super.convert(val, field);
    }

    private boolean isDatetime(Field field) {
        return StringUtil.equalsIgnoreCase(TypeEnum.DATETIME.name(), field.getTypeName());
    }
}
