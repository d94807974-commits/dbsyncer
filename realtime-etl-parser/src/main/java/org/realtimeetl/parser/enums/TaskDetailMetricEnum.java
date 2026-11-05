/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.enums;

/**
 * TasksDetailsListStatusFilter the indicator and carry the default sorting of the indicator.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-31 16:00
 */
public enum TaskDetailMetricEnum {

    /**
     * Data validation：PressDIFF（Diffs）FilterandSort
     */
    DIFF("dm.DIFF", "ORDER BY dm.DIFF DESC, d.UPDATE_TIME DESC "),

    /**
     * Database migration：PressFAIL（Failures）FilterandSort
     */
    FAIL("dm.FAIL", "ORDER BY dm.FAIL DESC, d.UPDATE_TIME DESC ");

    private final String column;
    private final String orderSql;

    TaskDetailMetricEnum(String column, String orderSql) {
        this.column = column;
        this.orderSql = orderSql;
    }

    /**
     * SQL ListAchievable ( all table aliases).
     */
    public String getColumn() {
        return column;
    }

    /**
     * Default for this indicatorORDER BY Substatement (with end spaces).
     */
    public String getOrderSql() {
        return orderSql;
    }
}
