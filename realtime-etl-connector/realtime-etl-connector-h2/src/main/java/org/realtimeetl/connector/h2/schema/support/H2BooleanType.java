/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.h2.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BooleanType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H2 BooleanType
 */
public final class H2BooleanType extends BooleanType {

    private enum TypeEnum {

        BOOLEAN("BOOLEAN"),
        BOOL("BOOL");

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
    protected Boolean merge(Object val, Field field) {
        return throwUnsupportedException(val, field);
    }
}
