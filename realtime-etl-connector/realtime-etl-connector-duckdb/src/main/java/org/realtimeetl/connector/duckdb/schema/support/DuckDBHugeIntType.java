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
package org.realtimeetl.connector.duckdb.schema.support;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DuckDB HUGEINT / UHUGEINT，Transfer avoided by stringOutlook saluteLost
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-16 10:00
 */
public final class DuckDBHugeIntType extends StringType {

    private enum TypeEnum {
        HUGEINT("HUGEINT"),
        UHUGEINT("UHUGEINT");

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
    protected String merge(Object val, Field field) {
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger) {
            return val.toString();
        }
        if (val instanceof Number) {
            return String.valueOf(val);
        }
        return String.valueOf(val);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof String) {
            return val;
        }
        if (val instanceof BigInteger) {
            return val.toString();
        }
        if (val instanceof Number) {
            return String.valueOf(val);
        }
        return JsonUtil.objToJsonSafe(val);
    }
}
