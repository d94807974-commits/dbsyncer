/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.enums;

/**
 * TasksDetailsListVisible Sorting (andIndicatorsNoneuse at close;Shh! when indicators are available{@link TaskDetailMetricEnum#getOrderSql()}）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-31 16:00
 */
public enum TaskDetailOrderEnum {

    /**
     * By DetailsUpdated at
     */
    UPDATE_TIME("ORDER BY d.UPDATE_TIME DESC ");

    private final String sql;

    TaskDetailOrderEnum(String sql) {
        this.sql = sql;
    }

    /**
     * ORDER BY Substatement (with end spaces).
     */
    public String getSql() {
        return sql;
    }
}
