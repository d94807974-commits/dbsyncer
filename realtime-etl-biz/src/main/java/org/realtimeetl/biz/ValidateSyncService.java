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
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.ValidateSyncListVO;
import org.realtimeetl.biz.vo.ValidateSyncTaskVO;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.model.Table;

import java.util.Map;

public interface ValidateSyncService {

    /**
     * AccessTasks
     */
    ValidateSyncTaskVO get(String id);

    /**
     * AddTasks
     */
    String add(Map<String, String> params);

    /**
     * UpdateTasks
     */
    String edit(Map<String, String> params);

    /**
     * CopyTasks
     *
     * @param id       SourceTasks ID
     * @param username ActionsWhoUsername（For awarding newResource ACL）
     */
    String copy(String id, String username);

    /**
     * DeleteTasks
     */
    String delete(String id);

    /**
     * StartTasks
     */
    String start(String id);

    /**
     * StopTasks
     */
    String stop(String id);

    /**
     * 搜索任务（列表轻量字段，不含完整配置/连接器敏感信息）
     */
    Paging<ValidateSyncListVO> search(Map<String, String> params);

    /**
     * SearchTable relation
     */
    Paging<TableGroup> searchTableGroup(Map<String, String> params);

    /**
     * minPageSearchVerify INTRODUCTIONTasksTable (for bottom frame remote)Search）
     */
    Paging<Table> searchTables(Map<String, String> params);

    /**
     * AccessResult
     */
    Object result(String id);

    /**
     * Refresh tablesList
     */
    String refreshTables(String id);

    /**
     * Refresh tablesField
     */
    String refreshFields(String id);

    /**
     * minPageQuery VerificationResultDetails
     *
     * @param params OrganisationtaskId、pageNum、pageSize；Optionallite=true ListNo, no, no.CONTENT
     * @return minPageResult
     */
    Paging searchResult(Map<String, String> params);

    /**
     * By DetailsPrimary keyQuery lines (including complete)CONTENT）。
     *
     * @param taskId Verify INTRODUCTIONTask ID(DetailsminTable location)
     * @param id     DetailsID
     * @return SingleMap，does not existBack null
     */
    Object getValidateResultDetail(String taskId, String id);

    /**
     * Not (1985) yetRevisedYes.VarianceManualRevised。
     *
     * @param taskId   Verify INTRODUCTIONTask ID(DetailsminTable location)
     * @param detailId DetailsPrimary key
     * @return Updated Details
     */
    Object manualReviseDetail(String taskId, String detailId);

    String addTableGroup(Map<String, String> params);

    /**
     * BatchInsertMap;jumping in case of repetition; closing only once in detail (Match similar tablesWait for the scene.
     */
    void addTableGroupBatch(Map<String, String> params);

    String editTableGroup(Map<String, String> params);

    String removeTableGroup(String id, String ids);
}
