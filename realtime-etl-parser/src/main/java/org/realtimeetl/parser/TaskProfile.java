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

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.TaskImportResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

/**
 * TasksDomain Unity Entry
 * andTasksRunResult（Meta、TASK_DETAIL）CleaningandReset。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface TaskProfile {

    /**
     * Pressid Query one.TasksConfigure。
     */
    <T extends ConfigModel> T getTask(String id, Class<T> clazz);

    /**
     * Press ModelTypeminPageQuestionTasks，and pressID IN Filter.
     *
     * @param searchKey optional; non-empty time pair{@code name} Do it.LIKE
     * @param ids       Resource ID collect; empty ornull Nope.ID Condition
     */
    <T extends ConfigModel> Paging<T> queryTasks(Class<T> clazz, int pageNum, int pageSize, String searchKey, List<String> ids);

    /**
     * Press ModelTypeminPageAll over the clock.AllTasks。
     */
    <T extends ConfigModel> void pageScanTasks(Class<T> clazz, int pageSize, Consumer<List<T>> pageConsumer);

    /**
     * InsertTasksConfigure。
     */
    String addTask(ConfigModel task);

    /**
     * UpdateTasksConfigure。
     */
    String updateTask(ConfigModel task);

    /**
     * BatchInsertTasksConfigure。
     */
    void addTaskBatch(List<? extends ConfigModel> tasks);

    /**
     * DeleteTasksConfigure（Delete onlytask Table line).
     */
    void deleteTask(String id);

    /**
     * PressTYPE StatisticsTasksNumber.
     */
    int countTasks(String type);

    /**
     * AllTasksLine number.
     */
    int countAllTasks();

    /**
     * ExportWith:AllTasksConfigure JSON（Map Form).
     */
    List<Map<String, Object>> listAllTaskJsonMaps();

    /**
     * ConfigureRevert: WriteTasksLine (Reservation)id；NoneTaskService.add，To avoid duplicationMeta）。
     */
    String importTask(ConfigModel task);

    /**
     * Fromtask.json / mapping.json ArrayImportAllTypeTasks；Mapping BatchdownLibrary，EnterpriseShh! EffectivenessTasksArticle by articleimportTask。
     */
    TaskImportResult importTasksFromJson(String json);

    /**
     * Fromtask_detail.json Pre-emptedminTable (structure only,NoneRow data）。
     */
    void importTaskDetailSchemasFromJson(String json);

    /**
     * FromZIP Import task.json（Compatibilitymapping.json）。
     */
    TaskImportResult importTasksFromZip(ZipFile zip) throws IOException;

    /**
     * Buildtask_detail.json Content (only)taskIds）。
     */
    String exportTaskDetailSchemasJson(List<String> taskIds);

    /**
     * ClearTasksTransportRow data：ResetTableMeta（Keep rows,Clear TASK_DETAIL minTable dataEdit/ResetUse it.
     */
    void deleteRunData(String taskId);

    /**
     * OnlyResetDetailsMeta（Counting/quick returnREADY），Oh, no.TRUNCATE DetailsminTable.
     */
    void resetTableDetailMetas(String taskId);

    /**
     * DeleteTasksTransportRow data：Delete Table LevelMeta，And physics.DROP DetailsminTable Vocational training.
     */
    void dropTaskDetailTable(String taskId);
    
    /**
     * Batch pre-run detailsminTableConfigurePackageImport）。
     */
    void createRunDetailTables(List<String> taskIds);

}
