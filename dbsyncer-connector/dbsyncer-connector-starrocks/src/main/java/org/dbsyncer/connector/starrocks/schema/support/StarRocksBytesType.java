/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.starrocks.schema.support;

import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.support.BytesType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * StarRocks BinaryType（VARBINARY）。
 * <p>
 * StarRocks MySQL AgreementsJDBC MaltaVARBINARY , printable content is often usedMappingHere.{@link String} Back；
 * Prior to verification/comparison, one must be assigned{@code byte[]}，and MySQL BLOB/BINARY Standard domain alignment.
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-08-06
 */
public final class StarRocksBytesType extends BytesType {

    private enum TypeEnum {
        BINARY, VARBINARY;

        public String getValue() {
            return name();
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected byte[] merge(Object val, Field field) {
        if (val instanceof String) {
            // JDBC PrintableVARBINARY When reading string, pressLatin1 1:1 Resume bytes, avoidUTF-8 Multibytes destroy binary SPT.
            return ((String) val).getBytes(StandardCharsets.ISO_8859_1);
        }
        return throwUnsupportedException(val, field);
    }
}
