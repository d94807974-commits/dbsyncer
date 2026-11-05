/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.dameng.schema.support;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.DoubleType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * D'Artagnan.Outlook saluteInteriorFLOAT / DOUBLE PRECISION）
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-07-18
 */
public final class DamengDoubleType extends DoubleType {

    private enum TypeEnum {
        DOUBLE("DOUBLE"),
        DOUBLE_PRECISION("DOUBLE PRECISION");

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
