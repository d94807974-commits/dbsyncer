/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.dameng.schema.support;

import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.support.ByteType;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dream byte/bitType
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-07-18
 */
public final class DamengByteType extends ByteType {

    private enum TypeEnum {
        BIT, TINYINT, BYTE
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected Byte merge(Object val, Field field) {
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        if (val instanceof Boolean) {
            return (byte) (((Boolean) val) ? 1 : 0);
        }
        if (val instanceof BitSet) {
            byte[] bytes = ((BitSet) val).toByteArray();
            return bytes.length > 0 ? bytes[0] : (byte) 0;
        }
        if (val instanceof byte[]) {
            byte[] bytes = (byte[]) val;
            return bytes.length > 0 ? bytes[0] : (byte) 0;
        }
        if (val instanceof String) {
            return Byte.parseByte(((String) val).trim());
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        // Base CategoryBoolean→byte[] It'll Initiate Dreams.TINYINT/BIT Report「NoneFrenchConvertDataType」
        if (val instanceof Byte) {
            return val;
        }
        if (val instanceof Number) {
            return ((Number) val).byteValue();
        }
        if (val instanceof Boolean) {
            return (byte) (((Boolean) val) ? 1 : 0);
        }
        if (val instanceof byte[]) {
            byte[] bytes = (byte[]) val;
            return bytes.length > 0 ? bytes[0] : (byte) 0;
        }
        if (val instanceof String) {
            return Byte.parseByte(((String) val).trim());
        }
        return throwUnsupportedException(val, field);
    }
}
