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
package org.realtimeetl.connector.elasticsearch.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BytesType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES BinaryType
 * Support: binary
 * ES Mediumbinary TypeHere.Base64 Encoding Storage
 *
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2026-01-11 22:21
 */
public final class ElasticsearchBytesType extends BytesType {

    private enum TypeEnum {

        BINARY("binary");

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
    protected byte[] merge(Object val, Field field) {
        if (val instanceof String) {
            // The binary type accepts a binary value as a Base64 encoded string. The field is not
            // stored by default and is not searchable.
            // ES binary TypeHere.Base64 Encoding Storage
            try {
                return Base64.getDecoder().decode((String) val);
            } catch (IllegalArgumentException e) {
                // If not,Yes Base64 Encoding, directly to bytes Group
                return ((String) val).getBytes(StandardCharsets.UTF_8);
            }
        }

        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof byte[]) {
            // ConvertYesBase64 Encoded Strings forES Storage
            return Base64.getEncoder().encodeToString((byte[]) val);
        }
        return super.convert(val, field);
    }
}
