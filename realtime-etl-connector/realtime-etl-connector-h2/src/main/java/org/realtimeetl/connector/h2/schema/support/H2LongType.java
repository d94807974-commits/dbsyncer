/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.h2.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.LongType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H2 Long
 */
public final class H2LongType extends LongType {

    private enum TypeEnum {

        BIGINT("BIGINT");

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
    protected Long merge(Object val, Field field) {
        return throwUnsupportedException(val, field);
    }
}
