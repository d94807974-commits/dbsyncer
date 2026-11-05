/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbaseoracle.schema.support;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.DoubleType;

import java.util.Collections;
import java.util.Set;

/**
 * OceanBase Oracle Double ModeOutlook saluteFloating Point
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleDoubleType extends DoubleType {

    @Override
    public Set<String> getSupportedTypeName() {
        return Collections.singleton("BINARY_DOUBLE");
    }

    @Override
    protected Double merge(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            return Double.valueOf(StringUtil.trimToEmpty(val.toString()));
        }
        return throwUnsupportedException(val, field);
    }
}
