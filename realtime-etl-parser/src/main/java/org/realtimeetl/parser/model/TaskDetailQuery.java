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
package org.realtimeetl.parser.model;

import org.realtimeetl.parser.enums.TaskDetailMetricEnum;
import org.realtimeetl.parser.enums.TaskDetailOrderEnum;
import org.realtimeetl.parser.enums.TaskDetailStatusEnum;

/**
 * TasksDetailed queriesParameter。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 16:00
 */
public class TaskDetailQuery {

    private String taskId;
    private String detailId;
    private int pageNum = 1;
    private int pageSize = 10;
    private String detailType;
    private TaskDetailStatusEnum detailStatus;
    private TaskDetailMetricEnum statusMetric;
    private TaskDetailOrderEnum orderBy;

    private TaskDetailQuery() {
    }

    /**
     * PressTasks ID Builds a query.
     *
     * @param taskId Tasks ID
     * @return QuestionParameter
     */
    public static TaskDetailQuery of(String taskId) {
        TaskDetailQuery query = new TaskDetailQuery();
        query.taskId = taskId;
        return query;
    }

    /**
     * SettingsminPage。
     */
    public TaskDetailQuery setPage(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * By DetailsID Query a single bar.
     */
    public TaskDetailQuery setDetailId(String detailId) {
        this.detailId = detailId;
        return this;
    }

    /**
     * By DetailsTypeFilter.
     */
    public TaskDetailQuery setDetailType(String detailType) {
        this.detailType = detailType;
        return this;
    }

    /**
     * By DetailsStatusFilter.
     * <p>and {@link #setStatusMetric(TaskDetailMetricEnum)} Use in pairs onlyStatusNo Submittion will assert when searchingFailed。
     */
    public TaskDetailQuery setDetailStatus(TaskDetailStatusEnum detailStatus) {
        this.detailStatus = detailStatus;
        return this;
    }

    /**
     * StatusFilter basisMeta Organizational (indicators)DIFF / FAIL）。
     * <p>Settings {@link #setDetailStatus(TaskDetailStatusEnum)} It must be filled in, silent acquiescence is forbidden.
     */
    public TaskDetailQuery setStatusMetric(TaskDetailMetricEnum statusMetric) {
        this.statusMetric = statusMetric;
        return this;
    }

    /**
     * Visible Sorting (andIndicatorsNoneUsed at close.
     */
    public TaskDetailQuery setOrderBy(TaskDetailOrderEnum orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDetailId() {
        return detailId;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getDetailType() {
        return detailType;
    }

    public TaskDetailStatusEnum getDetailStatus() {
        return detailStatus;
    }

    public TaskDetailMetricEnum getStatusMetric() {
        return statusMetric;
    }

    public TaskDetailOrderEnum getOrderBy() {
        return orderBy;
    }
}
