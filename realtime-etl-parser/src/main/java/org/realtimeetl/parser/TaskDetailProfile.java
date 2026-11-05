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
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.TaskDetailQuery;

import java.util.Map;

/**
 * TasksImplementation Details（realtime_etl_task_detail）Query:and meta / table_group Associated assembly display.
 *
 * @author Realtime ETL
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