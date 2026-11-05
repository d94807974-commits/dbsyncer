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
package org.realtimeetl.connector.starrocks.schema.support;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 03:00
 */
public final class StarRocksStringType extends StringType {

    private enum TypeEnum {
        STRING, JSON, HLL, BITMAP, ARRAY, MAP, STRUCT, PERCENTILE, VARCHAR;

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
            // Objective CombinedWhen it is a string column,byte[] TurnBase64，Avoidnew String(bytes) Sp 1(b) or"[B@..."
            return Base64.getEncoder().encodeToString((byte[]) val);
        }
        return JsonUtil.objToJsonSafe(val);
    }

    /**
     * Before writingConvert：STRING/VARCHAR Wait.Objective CombinedLiner, copy.byte[] ClockBase64，Ban SustainObject.toString ♪ Turn into ♪"[B@xxxx]"。
     */
    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) val);
        }
        if (val instanceof String) {
            return val;
        }
        return super.convert(val, field);
    }
}
