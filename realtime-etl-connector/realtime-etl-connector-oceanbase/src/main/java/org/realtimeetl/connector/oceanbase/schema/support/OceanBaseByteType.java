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
import org.realtimeetl.sdk.schema.support.ByteType;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseByteType extends ByteType {

    private enum TypeEnum {
        BIT, TINYINT
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected Byte merge(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        if (val instanceof BitSet) {
            BitSet bitSet = (BitSet) val;
            byte[] bytes = bitSet.toByteArray();
            if (bytes.length > 0) {
                return bytes[0];
            }
            return 0;
        }
        if (val instanceof Boolean) {
            Boolean b = (Boolean) val;
            return (byte) (b ? 1 : 0);
        }
        if (val instanceof byte[]) {
            byte[] bytes = (byte[]) val;
            if (bytes.length > 0) {
                return bytes[0];
            }
            return 0;
        }
        if (val instanceof String) {
            return Byte.parseByte((String) val);
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Byte) {
            return val;
        }
        return super.convert(val, field);
    }
}
