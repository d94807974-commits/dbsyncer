/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.util;

import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.SdkException;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.model.Table;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PrimaryKeyUtil {

    /**
     * BackPrimary keyName
     *
     * @param table
     * @return
     */
    public static List<String> findTablePrimaryKeys(Table table) {
        if (null == table) {
            throw new SdkException("The table is null.");
        }

        // Get treasury syncsPrimary keyField
        List<String> primaryKeys = findPrimaryKeyFields(table.getColumn()).stream().map(Field::getName).collect(Collectors.toList());

        // IfExisting TableFieldMapping，Auditorless.ConfigurePrimary keyThrowErrorNotice
        if (!CollectionUtils.isEmpty(table.getColumn()) && CollectionUtils.isEmpty(primaryKeys)) {
            throw new SdkException(String.format("Table %s missing primary key.", table.getName()));
        }
        return primaryKeys;
    }

    /**
     * BackPrimary keyPropertiesFieldGather!
     *
     * @param fields
     * @return
     */
    public static List<Field> findExistPrimaryKeyFields(List<Field> fields) {
        List<Field> list = findPrimaryKeyFields(fields);
        if (CollectionUtils.isEmpty(list)) {
            throw new SdkException("Main key is empty");
        }
        return list;
    }

    /**
     * BackPrimary keyPropertiesFieldGather!
     *
     * @param fields
     * @return
     */
    public static List<Field> findPrimaryKeyFields(List<Field> fields) {
        List<Field> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fields)) {
            Set<String> mark = new HashSet<>();
            fields.forEach(f-> {
                if (f.isPk() && !mark.contains(f.getName())) {
                    list.add(f);
                    mark.add(f.getName());
                }
            });
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * CursorPrimary keyMust be comparable.Type（Numbers, characters, datesTimeThis post is part of our special coverage Syria Protests 2011.NoAnd it'll lead to...minPageInvalid
     * 
     * <p>SupportedType：
     * <ul>
     *   <li>NumbersType：NUMERIC, BIGINT, INTEGER, TINYINT, SMALLINT</li>
     *   <li>CharacterType：VARCHAR, CHAR, NCHAR, NVARCHAR, LONGVARCHAR（Dictionary Comparison)</li>
     *   <li>DateTimeType：DATE, TIME, TIMESTAMP（PressTimeOrder comparison)</li>
     * </ul>
     * </p>
     */
    public static boolean isSupportedCursor(List<Field> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return false;
        }

        Map<String, Integer> typeAliases = new HashMap<>();
        fields.forEach(field-> {
            if (field.isPk()) {
                typeAliases.put(field.getName(), field.getType());
            }
        });

        for (Integer pkType : typeAliases.values()) {
            if (!isSupportedCursorType(pkType)) {
                return false;
            }
        }
        return !CollectionUtils.isEmpty(typeAliases);
    }

    /**
     * YesNoSupport cursorType
     * 
     * <p>Support comparableType，Including:
     * <ul>
     *   <li>NumbersType：Directly Available> Comparison</li>
     *   <li>CharacterType：Use dictionary comparison</li>
     *   <li>DateTimeType：PressTimeOrder comparison</li>
     * </ul>
     * </p>
     * <p>Note: Not supportedCLOB/NCLOB/BLOB Big objectTypeAs a cursor.</p>
     *
     * @param type JDBCType
     * @return YesNoSupport
     */
    private static boolean isSupportedCursorType(Integer type) {
        // NumbersType（Support direct comparison)
        if (type == Types.NUMERIC || type == Types.BIGINT || type == Types.INTEGER || type == Types.TINYINT || type == Types.SMALLINT || type == Types.DECIMAL || type == Types.FLOAT
                || type == Types.DOUBLE || type == Types.REAL) {
            return true;
        }
        // CharacterType（Supports dictionary comparison)
        if (type == Types.VARCHAR || type == Types.CHAR || type == Types.NCHAR || type == Types.NVARCHAR || type == Types.LONGVARCHAR) {
            return true;
        }
        // DateTimeType（SupportTimeOrder comparison)
        if (type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP) {
            return true;
        }
        return false;
    }

    /**
     * Get the latest cursorValue
     *
     * @param data Data FactList
     * @param primaryKeys Primary keyField nameList
     * @return CursorValuearray, if data are empty orPrimary keyListemptyBacknull
     */
    public static Object[] getLastCursors(List<Map> data, List<String> primaryKeys) {
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(primaryKeys)) {
            return null;
        }
        Map last = data.get(data.size() - 1);
        if (last == null || last.isEmpty()) {
            return null;
        }

        Object[] cursors = new Object[primaryKeys.size()];
        int i = 0;
        for (String pk : primaryKeys) {
            cursors[i++] = rowValue(last, pk);
        }
        return cursors;
    }

    public static Object[] getLastCursors(String str) {
        Object[] cursors = null;
        if (StringUtil.isNotBlank(str)) {
            String[] split = StringUtil.split(str, StringUtil.COMMA);
            int length = split.length;
            cursors = new Object[length];
            for (int i = 0; i < length; i++) {
                String val = split[i];
                cursors[i] = NumberUtil.isCreatable(val) ? NumberUtil.toLong(val) : val;
            }
        }
        return cursors;
    }

    /**
     * Compare the two sets, pressPrimary keyLeft to right.
     *
     * @param left  LeftValue
     * @param right ApplauseValue
     * @return Negative/zero/positive
     */
    public static int compareCursors(Object[] left, Object[] right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        int size = Math.min(left.length, right.length);
        for (int i = 0; i < size; i++) {
            int cmp = compareCursorValue(left[i], right[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(left.length, right.length);
    }

    /**
     * Current cursorYesNoIssuance of end cursor (included). Issuance will never be considered as an arrival.
     *
     * @param current Current cursor
     * @param end     End Curve
     * @return true Reached or crossed the end
     */
    public static boolean reachedEnd(Object[] current, Object[] end) {
        if (end == null || end.length == 0) {
            return false;
        }
        return compareCursors(current, end) >= 0;
    }

    /**
     * Keep cursor lines smaller than or equal to end cursor lines (end cursors as empty)Back）。
     * <p>CursorminPageResultPressPrimary keyascending;first beat last lineO(1)，Only when the last line passes.stream This post is part of our special coverage Syria Protests 2011.
     * Avoid Ben.PageNoneThe high reading has led to counting more than a piece of the budget.
     *
     * @param data        Data Row
     * @param primaryKeys Primary keyFirst Name
     * @param end         End Curve
     * @return After breakList
     */
    public static List<Map> trimToEndInclusive(List<Map> data, List<String> primaryKeys, Object[] end) {
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(primaryKeys) || end == null || end.length == 0) {
            return data;
        }
        // Up: end line does not cross end cursor→ FullPageIt's all in the Budget Parties.
        if (compareCursors(cursorOf(data.get(data.size() - 1), primaryKeys), end) <= 0) {
            return data;
        }
        return data.stream()
                .filter(row -> compareCursors(cursorOf(row, primaryKeys), end) <= 0)
                .collect(Collectors.toList());
    }

    private static Object[] cursorOf(Map row, List<String> primaryKeys) {
        if (row == null) {
            return null;
        }
        Object[] cursor = new Object[primaryKeys.size()];
        for (int i = 0; i < primaryKeys.size(); i++) {
            cursor[i] = rowValue(row, primaryKeys.get(i));
        }
        return cursor;
    }

    @SuppressWarnings("rawtypes")
    private static Object rowValue(Map row, String primaryKey) {
        if (StringUtil.isBlank(primaryKey)) {
            return null;
        }
        Object value = row.get(primaryKey);
        if (value != null || row.containsKey(primaryKey)) {
            return value;
        }
        for (Object key : row.keySet()) {
            if (key != null && primaryKey.equalsIgnoreCase(String.valueOf(key))) {
                return row.get(key);
            }
        }
        return null;
    }

    private static int compareCursorValue(Object left, Object right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        if (left instanceof Number && right instanceof Number) {
            return new java.math.BigDecimal(left.toString())
                    .compareTo(new java.math.BigDecimal(right.toString()));
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

}