/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.LongType;

import java.util.Collections;
import java.util.Set;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-12-25 00:03
 */
public final class OracleLongType extends LongType {

    @Override
    public Set<String> getSupportedTypeName() {
        return Collections.emptySet();
    }

    @Override
    protected Long merge(Object val, Field field) {
        return throwUnsupportedException(val, field);
    }
}
