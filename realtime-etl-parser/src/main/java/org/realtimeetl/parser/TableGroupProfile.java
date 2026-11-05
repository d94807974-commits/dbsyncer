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
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.storage.ExecuteRequest;

import java.util.List;
import java.util.function.Consumer;

/**
 * Map relationConfigure（realtime_etl_table_group）Actions。
 * <p>ConfigurePackageZIP/NDJSON Yes.ImportExportOrganized inbiz Yes.{@code ConfigImportService}/{@code ConfigExportService}。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface TableGroupProfile {

    /**
     * Add TableGroup，And build a scale with the path.Meta（id=Snowflake,taskId=tableGroupId，isTaskDetail=1）。
     */
    String addTableGroup(TableGroup model);

    /**
     * BatchAdd TableGroup，and pre-set scale with the path batchMeta（id=Snowflake,taskId=tableGroupId）。
     *
     * @param models TableGroup List
     */
    void addTableGroupBatch(List<TableGroup> models);

    /**
     * Batch writingtable_group Initiate a new level.Meta；ConfigureWhen the package is returnedMeta AlternativeImport）。
     */
    void addTableGroupBatchWithoutMeta(List<TableGroup> models);

    String editTableGroup(TableGroup model);

    /**
     * BatchUpdate TableGroup（PressPageOpenminPost-s Single Storage Bat CantonUPDATE）。
     *
     * @param models To be updatedTableGroup List
     */
    void editTableGroupBatch(List<TableGroup> models);

    /**
     * DeleteSingleTableGroup，And with the pathDeleteDetailsMeta。
     */
    void removeTableGroup(String id);

    /**
     * PressTasks ID ConditionDeleteAll table_group And the details.Meta（First.id DeleteMeta，Again.delete WHERE TASK_ID）。
     */
    void removeTableGroupsByTaskId(String taskId);

    TableGroup getTableGroup(String tableGroupId);

    /**
     * PresstaskId minPageMunicipalitiessortIndex Decline.
     *
     * @param mappingId Tasks/Mapping ID
     * @param searchKey optional; non-empty time pairsourceTable/targetTable Do it.LIKE（OR）
     * @param pageNum   PageCode (from1 Start
     * @param pageSize  EveryPageNumber of bars;fallbacks when illegal{@link org.realtimeetl.sdk.constant.ConfigConstant#PAGE_SIZE}
     * @return minPageResult
     */
    Paging<TableGroup> queryTableGroup(String mappingId, String searchKey, int pageNum, int pageSize);

    /**
     * Sync Results Details：table_group LEFT JOIN meta，LibrarySide pressUpdated at/Failures/SuccessesDec TerminatingminPage。
     *
     * @param mappingId    Mapping ID
     * @param detailStatus Optional;{@code fail} OnlyFailed、{@code success} OnlySuccess（fail=0），EmptyAll
     * @param pageNum      PageCode (from1 Start
     * @param pageSize     EveryPageNumber of bars
     * @return minPageSoldierField：tableGroupId / sourceTable / targetTable / successTotal / failTotal / updateTime）
     */
    Paging queryTableGroupResults(String mappingId, String detailStatus, int pageNum, int pageSize);

    /**
     * 按页回调遍历任务下全部表映射（页内顺序为 sortIndex 降序）。
     *
     * @param mappingId    Tasks/Mapping ID
     * @param pageSize     EveryPageNumber of bars;fallbacks when illegal{@link org.realtimeetl.sdk.constant.ConfigConstant#PAGE_SIZE}
     * @param pageConsumer PageRewind
     */
    void pageScanTableGroups(String mappingId, int pageSize, Consumer<List<TableGroup>> pageConsumer);

    /**
     * PressSQL Query table map (transmitted execution,ResultPressTableGroup Inverse sequence.
     * <p>Filter ConditionsBy CallerSQL Decisions (e.g. no)FinishDetailsMeta This method does not use business semantic filters.
     *
     * @param query SQL andOptionalminPageParameter
     * @return TableGroup List（NoneEmptyList）
     */
    List<TableGroup> listTableGroupsBySql(ExecuteRequest query);

    int getTableGroupCount(String mappingId);

    /**
     * YesNoSame existsSource table+Objective CombinedMapLibrarySideValueQuery, do not sweep the entire table).
     */
    boolean existTableGroup(String taskId, String sourceTable, String targetTable);

    /**
     * TableTable GroupTotal。
     */
    int countTableGroups();

    /**
     * PresstaskId Scroll ThroughAllMap.
     */
    void pageScanTableGroupsByTaskId(Consumer<TableGroup> consumer);

}
