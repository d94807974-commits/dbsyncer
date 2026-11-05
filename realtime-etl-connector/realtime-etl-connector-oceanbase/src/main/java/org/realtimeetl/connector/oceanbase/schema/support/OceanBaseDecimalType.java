/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbase.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.DecimalType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseDecimalType extends DecimalType {

    private enum TypeEnum {

        DECIMAL("DECIMAL"), DECIMAL_UNSIGNED("DECIMAL UNSIGNED"), BIGINT_UNSIGNED("BIGINT UNSIGNED");

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
    protected BigDecimal merge(Object val, Field field) {
        if (val instanceof Number) {
            return new BigDecimal(val.toString());
        }
        return throwUnsupportedException(val, field);
    }

}