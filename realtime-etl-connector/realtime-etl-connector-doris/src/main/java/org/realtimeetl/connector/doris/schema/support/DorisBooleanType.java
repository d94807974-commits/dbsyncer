/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.doris.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BooleanType;

import java.util.Collections;
import java.util.Set;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 23:50
 */
public final class DorisBooleanType extends BooleanType {

    @Override
    public Set<String> getSupportedTypeName() {
        return Collections.singleton("BOOLEAN");
    }

    @Override
    protected Boolean merge(Object val, Field field) {
        return (Boolean) convert(val, field);
    }
}
