/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.TaskDetailQuery;

import java.util.Map;

/**
 * TasksImplementation Details（realtime_etl_task_detail）Query:and meta / table_group Associated assembly display.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-20 15:00
 */
public interface TaskDetailProfile {

    /**
     * QuestionValidation/removalResultList（Storage side filters, sorting,minPage）。
     *
     * @param query QuestionParameter（HamtaskId、minPage、Type/StatusScreening, indicatorsandSort)
     * @return minPageResult
     */
    Paging queryResults(TaskDetailQuery query);

    /**
     * Query a line detail.
     *
     * @param query QuestionParameter（OrganisationtaskId、detailId）
     * @return Show lines when they do not existnull
     */
    Map<String, Object> getDetail(TaskDetailQuery query);

    /**
     * DeleteTasksDetailed data
     */
    void delete(String taskId, String id);

    /**
     * AccessTasksDetailed data
     */
    Map getData(String taskId, String messageId);

}