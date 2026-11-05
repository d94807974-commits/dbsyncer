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
import org.realtimeetl.sdk.schema.support.LongType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * D'ArtagnanMySQL INT UNSIGNED Alias)
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-07-18
 */
public final class DamengLongType extends LongType {

    private enum TypeEnum {
        BIGINT("BIGINT"),
        INT_UNSIGNED("INT UNSIGNED");

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
    protected Long merge(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            return Long.parseLong(((String) val).trim());
        }
        if (val instanceof Boolean) {
            return ((Boolean) val) ? 1L : 0L;
        }
        return throwUnsupportedException(val, field);
    }
}
