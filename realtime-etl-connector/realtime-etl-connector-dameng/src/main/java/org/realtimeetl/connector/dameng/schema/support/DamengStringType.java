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
package org.realtimeetl.connector.dameng.schema.support;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oracle.schema.OracleLobParameter;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dream characterType（HamMySQL It's from the same displacement.TEXT/JSON Alias)
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 02:00
 */
public final class DamengStringType extends StringType {

    private enum TypeEnum {
        VARCHAR("VARCHAR"),
        CHARACTER("CHARACTER"),
        NVARCHAR("NVARCHAR"),
        TEXT("TEXT"),
        TINYTEXT("TINYTEXT"),
        MEDIUMTEXT("MEDIUMTEXT"),
        LONGTEXT("LONGTEXT"),
        LONGVARCHAR("LONGVARCHAR"),
        JSON("JSON");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        static boolean isLob(String typeName) {
            if (StringUtil.isBlank(typeName)) {
                return false;
            }
            String t = typeName.trim().toUpperCase(Locale.ROOT);
            return t.contains("TEXT") || "LONGVARCHAR".equals(t) || "JSON".equals(t) || t.contains("CLOB");
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected String merge(Object val, Field field) {
        if (val instanceof String) {
            return (String) val;
        }
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.UTF_8);
        }
        if (val instanceof Number || val instanceof Boolean || val instanceof Character) {
            return String.valueOf(val);
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        // AlreadyYesSubject to binding: WriteLibraryDirectly available; before sequencing unbundled strings
        if (val instanceof OracleLobParameter) {
            return val;
        }
        Object converted = super.convert(val, field);
        // MERGE LargeFieldRequiredCLOB Tie it up.CAST(? AS CLOB)
        if (converted instanceof String && TypeEnum.isLob(field.getTypeName())) {
            return new OracleLobParameter((String) converted, field);
        }
        return converted;
    }
}
