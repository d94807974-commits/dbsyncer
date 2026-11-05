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

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.FloatType;

import java.util.Collections;
import java.util.Set;

/**
 * OceanBase Oracle ModelistOutlook saluteFloating Point
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleFloatType extends FloatType {

    @Override
    public Set<String> getSupportedTypeName() {
        return Collections.singleton("BINARY_FLOAT");
    }

    @Override
    protected Float merge(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            return Float.parseFloat((String) val);
        }
        return throwUnsupportedException(val, field);
    }
}
