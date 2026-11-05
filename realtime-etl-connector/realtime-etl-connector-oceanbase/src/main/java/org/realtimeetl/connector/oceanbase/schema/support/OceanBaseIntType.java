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

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.IntType;

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseIntType extends IntType {

    private enum TypeEnum {

        SMALLINT_UNSIGNED("SMALLINT UNSIGNED"), MEDIUMINT("MEDIUMINT"), MEDIUMINT_UNSIGNED("MEDIUMINT UNSIGNED"), INT("INT"), INTEGER("INTEGER"), YEAR("YEAR");

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
    protected Integer merge(Object val, Field field) {
        if (val instanceof Date) {
            Date d = (Date) val;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            return calendar.get(Calendar.YEAR);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return throwUnsupportedException(val, field);
    }

}