/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.connector.doris.schema.support;

import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BytesType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Doris BinaryType（BINARY / VARBINARY）。
 * <p>
 * Doris MySQL AgreementsJDBC Printable content is often used when reading binary columnsMappingHere.{@link String} Back；
 * Prior to verification/comparison, one must be assigned{@code byte[]}，and MySQL BLOB/BINARY Standard domain alignment.
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-08-06
 */
public final class DorisBytesType extends BytesType {

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
            // JDBC When you can print a binary into a string, pressLatin1 1:1 Resume bytes, avoidUTF-8 Multibytes destroy binary SPT.
            return ((String) val).getBytes(StandardCharsets.ISO_8859_1);
        }
        return throwUnsupportedException(val, field);
    }
}
