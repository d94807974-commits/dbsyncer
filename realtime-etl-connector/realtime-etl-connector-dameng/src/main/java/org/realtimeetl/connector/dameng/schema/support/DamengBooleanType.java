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
import org.realtimeetl.sdk.schema.support.BooleanType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Darmbull.Type
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-07-18
 */
public final class DamengBooleanType extends BooleanType {

    private enum TypeEnum {
        BOOLEAN
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected Boolean merge(Object val, Field field) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        if (val instanceof String) {
            String text = ((String) val).trim();
            if ("1".equals(text) || "true".equalsIgnoreCase(text) || "Y".equalsIgnoreCase(text)) {
                return Boolean.TRUE;
            }
            if ("0".equals(text) || "false".equalsIgnoreCase(text) || "N".equalsIgnoreCase(text)) {
                return Boolean.FALSE;
            }
        }
        if (val instanceof byte[]) {
            byte[] bytes = (byte[]) val;
            return bytes.length > 0 && bytes[0] != 0;
        }
        return throwUnsupportedException(val, field);
    }
}
