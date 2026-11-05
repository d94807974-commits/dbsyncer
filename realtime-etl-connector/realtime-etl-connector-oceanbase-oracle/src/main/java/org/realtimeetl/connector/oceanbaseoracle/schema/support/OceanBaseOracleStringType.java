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
package org.realtimeetl.connector.oceanbaseoracle.schema.support;

import org.realtimeetl.connector.oceanbaseoracle.OceanBaseOracleException;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OceanBase Oracle Mode CharacterType（StandardJDBC Clob）
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleStringType extends StringType {

    private enum TypeEnum {
        CHAR("CHAR"),
        NCHAR("NCHAR"),
        VARCHAR2("VARCHAR2"),
        NVARCHAR2("NVARCHAR2"),
        CLOB("CLOB"),
        NCLOB("NCLOB"),
        LONG("LONG"),
        ROWID("ROWID"),
        UROWID("UROWID");

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
    protected String merge(Object val, Field field) {
        if (val instanceof String) {
            return (String) val;
        }
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.UTF_8);
        }
        if (val instanceof Clob) {
            return clobToString((Clob) val);
        }
        if (val instanceof Number || val instanceof Boolean || val instanceof Character) {
            return String.valueOf(val);
        }
        return throwUnsupportedException(val, field);
    }

    private String clobToString(Clob clob) {
        try (Reader is = clob.getCharacterStream(); BufferedReader br = new BufferedReader(is)) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } catch (SQLException | IOException e) {
            throw new OceanBaseOracleException(e);
        }
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Time) {
            return val.toString();
        }
        return super.convert(val, field);
    }
}
