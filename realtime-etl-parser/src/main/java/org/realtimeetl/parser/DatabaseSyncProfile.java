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
import org.realtimeetl.sdk.model.DatabaseSyncTask;

import java.util.List;
import java.util.function.Consumer;

/**
 * Library Migration Task
 *
 * @author Realtime ETL
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
