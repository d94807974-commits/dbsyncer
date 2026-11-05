/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbase.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BytesType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-26 22:59
 */
public final class OceanBaseBytesType extends BytesType {

    private enum TypeEnum {
        TINYBLOB, BLOB, MEDIUMBLOB, LONGBLOB, BINARY, VARBINARY;
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected byte[] merge(Object val, Field field) {
        if (val instanceof String) {
            return ((String) val).getBytes(StandardCharsets.UTF_8);
        }
        if (val instanceof BitSet) {
            BitSet bitSet = (BitSet) val;
            return bitSet.toByteArray();
        }
        return throwUnsupportedException(val, field);
    }
}
