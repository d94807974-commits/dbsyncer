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
package org.realtimeetl.connector.oceanbaseoracle.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.TimestampType;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OceanBase Oracle ModeTimePokeType（TypeNameSchemaResolver Without formalizationOutlook saluteparenthesis)
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleTimestampType extends TimestampType {

    private enum TypeEnum {
        TIMESTAMP("TIMESTAMP"),
        TIMESTAMP_WITH_TIME_ZONE("TIMESTAMP WITH TIME ZONE"),
        TIMESTAMP_WITH_LOCAL_TIME_ZONE("TIMESTAMP WITH LOCAL TIME ZONE");

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
    protected Timestamp merge(Object val, Field field) {
        if (val instanceof Timestamp) {
            return (Timestamp) val;
        }
        if (val instanceof Date) {
            return new Timestamp(((Date) val).getTime());
        }
        if (val instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) val).getTime());
        }
        if (val instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) val);
        }
        if (val instanceof String) {
            Timestamp timestamp = DateFormatUtil.stringToTimestamp((String) val);
            if (timestamp != null) {
                return timestamp;
            }
        }
        if (val instanceof Number) {
            return new Timestamp(((Number) val).longValue());
        }
        return throwUnsupportedException(val, field);
    }
}
