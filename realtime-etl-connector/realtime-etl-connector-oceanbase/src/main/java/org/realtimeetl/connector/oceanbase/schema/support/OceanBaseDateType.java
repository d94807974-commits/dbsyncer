/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbase.schema.support;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.DateType;

import java.sql.Date;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseDateType extends DateType {

    private enum TypeEnum {
        DATE
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected Date merge(Object val, Field field) {
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        // CompatibilityOceanBaseYear/70
        if (val instanceof Integer) {
            return val;
        }

        if (val instanceof String) {
            if (StringUtil.equals((String) val, "0000-00-00")) {
                return val;
            }
        }
        return super.convert(val, field);
    }
}
