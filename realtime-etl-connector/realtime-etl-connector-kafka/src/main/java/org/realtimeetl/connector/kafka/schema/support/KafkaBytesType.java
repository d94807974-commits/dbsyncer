/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.kafka.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BytesType;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2026-01-11 22:21
 */
public final class KafkaBytesType extends BytesType {

    @Override
    protected byte[] merge(Object val, Field field) {
        return throwUnsupportedException(val, field);
    }

    @Override
    public Set<String> getSupportedTypeName() {
        Set<String> types = new HashSet<>();
        types.add(getType().name());
        return types;
    }
}
