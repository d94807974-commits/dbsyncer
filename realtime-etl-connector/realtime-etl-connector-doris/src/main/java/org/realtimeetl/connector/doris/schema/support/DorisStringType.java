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
package org.realtimeetl.connector.doris.schema.support;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 23:50
 */
public final class DorisStringType extends StringType {

    private enum TypeEnum {
        STRING, VARCHAR, JSON, HLL, BITMAP, ARRAY, MAP, STRUCT, IPV4, IPV6, VARIANT, AGG_STATE, QUANTILE_STATE;

        public String getValue() {
            return name();
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
        if (val instanceof String) {
            return (String) val;
        }
        if (val instanceof byte[]) {
            // MySQL BLOB Map toDoris STRING/VARCHAR PressLatin1 It's real easy.andSourcebyte[] Semantic Comparison
            return new String((byte[]) val, StandardCharsets.ISO_8859_1);
        }
        return JsonUtil.objToJsonSafe(val);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof String) {
            return val;
        }
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.ISO_8859_1);
        }
        return JsonUtil.objToJsonSafe(val);
    }
}
