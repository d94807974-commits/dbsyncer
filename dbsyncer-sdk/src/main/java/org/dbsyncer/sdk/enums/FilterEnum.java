/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.SdkException;
import org.dbsyncer.sdk.filter.CompareFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Operator expressionType
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2019-09-26 23:21
 */
public enum FilterEnum {

    /**
     * equals
     */
    EQUAL("=", StringUtil::equals),
    /**
     * Not equal to
     */
    NOT_EQUAL("!=", (value, filterValue)->!StringUtil.equals(value, filterValue)),
    /**
     * Greater than
     */
    GT(">", (value, filterValue)->NumberUtil.toLong(value) > NumberUtil.toLong(filterValue)),
    /**
     * less than
     */
    LT("<", (value, filterValue)->NumberUtil.toLong(value) < NumberUtil.toLong(filterValue)),
    /**
     * greater than or equal to
     */
    GT_AND_EQUAL(">=", (value, filterValue)->NumberUtil.toLong(value) >= NumberUtil.toLong(filterValue)),
    /**
     * less than or equal to
     */
    LT_AND_EQUAL("<=", (value, filterValue)->NumberUtil.toLong(value) <= NumberUtil.toLong(filterValue)),
    /**
     * Fuzzy Match
     */
    LIKE("like", (value, filterValue)-> {
        boolean startsWith = StringUtil.startsWith(filterValue, StringUtil.PERCENT) || StringUtil.startsWith(filterValue, StringUtil.STAR);
        boolean endsWith = StringUtil.endsWith(filterValue, StringUtil.PERCENT) || StringUtil.endsWith(filterValue, StringUtil.STAR);
        String compareValue = StringUtil.replace(filterValue, StringUtil.PERCENT, StringUtil.EMPTY);
        compareValue = StringUtil.replace(compareValue, StringUtil.STAR, StringUtil.EMPTY);
        // Fuzzy Match
        if (startsWith && endsWith) {
            return StringUtil.contains(value, compareValue);
        }
        // Prefix Match
        if (endsWith) {
            return StringUtil.startsWith(value, compareValue);
        }
        // Postfix Match
        if (startsWith) {
            return StringUtil.endsWith(value, compareValue);
        }
        return false;
    }),
    /**
     * Gather Match
     */
    IN("in", (value, filterValue)-> {
        if (StringUtil.isNotBlank(filterValue)) {
            return Arrays.stream(StringUtil.split(filterValue, StringUtil.COMMA)).anyMatch(v->StringUtil.equals(v, value));
        }
        return false;
    }),
    /**
     * NullValue
     */
    IS_NULL("is null", (value, filterValue)->value == null),
    /**
     * NotNullValue
     */
    IS_NOT_NULL("is not null", (value, filterValue)->value != null);

    /**
     * Createname and FilterEnum CreateMapping，Time-consuming quick search
     */
    private static final Map<String, FilterEnum> MAPPING = new HashMap<>();

    static {
        Arrays.stream(FilterEnum.values()).forEach((item)->MAPPING.put(item.getName().toUpperCase(), item));
    }

    // OperatorsName
    private final String name;
    // Comparer
    private final CompareFilter compareFilter;

    FilterEnum(String name, CompareFilter compareFilter) {
        this.name = name;
        this.compareFilter = compareFilter;
    }

    /**
     * Get Expression
     *
     * @param name
     * @return
     * @throws SdkException
     */
    public static FilterEnum getFilterEnum(String name) throws SdkException {
        Objects.requireNonNull(name, "name is null");
        FilterEnum e = MAPPING.get(name.toUpperCase());
        if (e != null) {
            return e;
        }
        throw new SdkException(String.format("FilterEnum name \"%s\" does not exist.", name));
    }

    public String getName() {
        return name;
    }

    public CompareFilter getCompareFilter() {
        return compareFilter;
    }

}