/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.DatabaseSyncTaskVO;
import org.realtimeetl.biz.vo.TablePreviewVO;
import org.realtimeetl.common.model.Paging;

import java.util.List;
import java.util.Map;

/**
 * Database migrationOperational services
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-22 00:00
 */
public interface DatabaseSyncService {

    /**
     * AccessTasksDetails
     *
     * @param id Tasks ID
     * @return Tasks VO
     */
    DatabaseSyncTaskVO get(String id);

    /**
     * InsertTasks
     *
     * @param params TasksParameter
     * @return Tasks ID
     */
    String add(Map<String, String> params);

    /**
     * UpdateTasks
     *
     * @param params TasksParameter（Hamid）
     * @return Tasks ID
     */
    String edit(Map<String, String> params);

    /**
     * DeleteTasks
     *
     * @param id Tasks ID
     * @return ActionsResultNotice
     */
    String delete(String id);

    /**
     * StartTasks
     *
     * @param id Tasks ID
     * @return ActionsResultNotice
     */
    String start(String id);

    /**
     * StopTasks
     *
     * @param id Tasks ID
     * @return ActionsResultNotice
     */
    String stop(String id);

    /**
     * minPageSearchTasksList
     *
     * @param params QuestionParameter（pageNum、pageSize、searchKey）
     * @return minPageResult
     */
    Paging<DatabaseSyncTaskVO> search(Map<String, String> params);

    /**
     * PreviewConnectorTableList（RollminPage）
     *
     * @param params connectorId、database、schema、searchKey、offset、limit
     * @return minPageData andTypeStatistics
     */
    TablePreviewVO previewTables(Map<String, String> params);

    /**
     * minPageQuery MigrationResultDetails
     */
    Paging searchResult(Map<String, String> params);
}
