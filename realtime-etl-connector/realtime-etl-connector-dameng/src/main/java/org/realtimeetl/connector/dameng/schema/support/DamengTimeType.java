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
package org.realtimeetl.connector.dameng.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.TimeType;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tamang!TimeType
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-07-18
 */
public final class DamengTimeType extends TimeType {

    private enum TypeEnum {
        TIME("TIME"),
        TIME_WITH_TIME_ZONE("TIME WITH TIME ZONE");

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
    protected Time merge(Object val, Field field) {
        if (val instanceof Time) {
            return (Time) val;
        }
        if (val instanceof Timestamp) {
            return new Time(((Timestamp) val).getTime());
        }
        if (val instanceof LocalTime) {
            return Time.valueOf((LocalTime) val);
        }
        if (val instanceof String) {
            return parseTimeString((String) val);
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Time) {
            return val;
        }
        if (val instanceof String) {
            return parseTimeString((String) val);
        }
        if (val instanceof LocalTime) {
            return Time.valueOf((LocalTime) val);
        }
        return throwUnsupportedException(val, field);
    }

    private Time parseTimeString(String value) {
        String text = value.trim();
        int space = text.indexOf(' ');
        if (space > 0) {
            // Compatible Time Zone Corrections12:00:00 +08:00
            text = text.substring(0, space);
        }
        int dot = text.indexOf('.');
        if (dot > 0) {
            text = text.substring(0, dot);
        }
        return Time.valueOf(text);
    }
}
