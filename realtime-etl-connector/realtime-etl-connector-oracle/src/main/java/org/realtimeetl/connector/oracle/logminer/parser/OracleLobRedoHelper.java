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
package org.realtimeetl.connector.oracle.logminer.parser;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LogMiner redo MediumLOB Place recognition (large)FieldAlways.EMPTY_BLOB/EMPTY_CLOB PresentSQL_REDO）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-07 17:10
 */
public final class OracleLobRedoHelper {

    private OracleLobRedoHelper() {
    }

    /**
     * Press in linePrimary keyCheck back.OverwriteSubscript:LOB CRC Committee on the Rights of the ChildValueYesEMPTY_BLOB()/EMPTY_CLOB() PositionINSERT/UPDATE LargeFieldCommon).
     */
    public static List<Integer> findEmptyLobPlaceholderIndexes(List<Field> fields, List<Object> data) {
        List<Integer> indexes = new ArrayList<>();
        if (CollectionUtils.isEmpty(fields) || CollectionUtils.isEmpty(data) || fields.size() != data.size()) {
            return indexes;
        }
        for (int i = 0; i < fields.size(); i++) {
            if (!isLobField(fields.get(i))) {
                continue;
            }
            if (isEmptyLobPlaceholder(data.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static boolean isEmptyLobPlaceholder(Object value) {
        if (value == null || OracleUnchangedValue.isUnchanged(value)) {
            return false;
        }
        String text = String.valueOf(value).trim();
        if (StringUtil.isBlank(text)) {
            return false;
        }
        String upper = text.toUpperCase(Locale.ROOT);
        return "EMPTY_BLOB()".equals(upper) || "EMPTY_CLOB()".equals(upper);
    }

    public static boolean isLobField(Field field) {
        if (field == null) {
            return false;
        }
        int type = field.getType();
        if (type == Types.BLOB || type == Types.CLOB || type == Types.NCLOB
                || type == Types.LONGVARBINARY || type == Types.LONGVARCHAR) {
            return true;
        }
        String typeName = field.getTypeName();
        if (StringUtil.isBlank(typeName)) {
            return false;
        }
        String upper = typeName.trim().toUpperCase(Locale.ROOT);
        return upper.contains("BLOB")
                || upper.contains("CLOB")
                || "LONG".equals(upper)
                || "LONG RAW".equals(upper);
    }
}
