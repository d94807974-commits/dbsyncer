/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.elasticsearch.schema.support;

import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.support.IntType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES IntegerType
 * Support: integer
 *
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2026-01-11 22:21
 */
public final class ElasticsearchIntType extends IntType {

    private enum TypeEnum {

        SHORT("short"),
        INTEGER("integer"),
        INT("INT"),
        LONG("LONG"),
        VERSION("version"); /* For storage of documentsVersionNo. No.,The optimism lock.,Prevent data conflicts when updating*/

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
    protected Integer merge(Object val, Field field) {
        if (val instanceof String) {
            return Integer.parseInt((String) val);
        }

        if (val instanceof Number) {
            return ((Number) val).intValue();
        }

        if (val instanceof Boolean) {
            return ((Boolean) val) ? 1 : 0;
        }

        return throwUnsupportedException(val, field);
    }
}
