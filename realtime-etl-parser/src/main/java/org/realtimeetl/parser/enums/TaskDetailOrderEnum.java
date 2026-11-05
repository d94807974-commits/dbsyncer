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
package org.realtimeetl.parser.enums;

/**
 * TasksDetailsListVisible Sorting (andIndicatorsNoneuse at close;Shh! when indicators are available{@link TaskDetailMetricEnum#getOrderSql()}）。
 *
 * @author Realtime ETL
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
