/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbaseoracle.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.FloatType;

import java.util.Collections;
import java.util.Set;

/**
 * OceanBase Oracle ModelistOutlook saluteFloating Point
 *
 * @author wuji
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
