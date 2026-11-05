/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.sdk.model.ValidateSyncTask;

import java.util.List;
import java.util.function.Consumer;

/**
 * Revised verification tasks
 *
 * @author wuji
 * @version 1.0.0
 */
public interface ValidateSyncProfile {

    /**
     * Pressid QuestionMapping TasksConfigure。
     */
    ValidateSyncTask get(String id);

    /**
     * InsertTasksConfigure。
     */
    String add(ValidateSyncTask task);

    /**
     * UpdateTasksConfigure。
     */
    String update(ValidateSyncTask tasks);

    /**
     * DeleteTasksConfigure
     */
    void delete(String id);

    /**
     * Press ModelTypeminPageQuestionTasks，Optional PressNameBlurSearch。
     *
     * @param searchKey optional; non-empty time pair{@code name} Do it.LIKE
     * @param ids optional 38.ID IN Filter.
     */
    Paging<ValidateSyncTask> query(int pageNum, int pageSize, String searchKey, List<String> ids);

    /**
     * Press ModelTypeminPageAll over the clock.AllTasks。
     */
    void pageScanTasks(int pageSize, Consumer<List<ValidateSyncTask>> pageConsumer);

    /**
     * Save/EditTime to recreate detailminCobra line (first)ClearAnd write on the current table.
     */
    void syncTaskTableMetaDetails(String taskId);

}
