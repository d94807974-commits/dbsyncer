/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.common.model.Paging;
import org.dbsyncer.sdk.model.DatabaseSyncTask;

import java.util.List;
import java.util.function.Consumer;

/**
 * Library Migration Task
 *
 * @author wuji
 * @version 1.0.0
 */
public interface DatabaseSyncProfile {

    /**
     * Pressid QuestionMapping TasksConfigure。
     */
    DatabaseSyncTask get(String id);

    /**
     * InsertTasksConfigure。
     */
    String add(DatabaseSyncTask task);

    /**
     * UpdateTasksConfigure。
     */
    String update(DatabaseSyncTask tasks);

    /**
     * DeleteTasksConfigure
     */
    void delete(String id);

    void clearRunData(String id);

    /**
     * Press ModelTypeminPageQuestionTasks，Optional PressNameBlurSearch。
     *
     * @param searchKey optional; non-empty time pair{@code name} Do it.LIKE
     * @param ids optional 38.ID IN Filter.
     */
    Paging<DatabaseSyncTask> query(int pageNum, int pageSize, String searchKey, List<String> ids);

    /**
     * Press ModelTypeminPageAll over the clock.AllTasks。
     */
    void pageScanTasks(int pageSize, Consumer<List<DatabaseSyncTask>> pageConsumer);

    /**
     * Pre-run DetailsminTable
     */
    void syncTaskTableMetaDetails(String taskId);

}
