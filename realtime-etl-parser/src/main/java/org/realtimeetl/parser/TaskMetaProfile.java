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
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.sdk.model.MetaIncrement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipOutputStream;

/**
 * Task execution results table
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface TaskMetaProfile {

    /**
     * AccessTasksYes.metaCache
     */
    Meta getMeta(String taskId);

    /**
     * AccessTasksYes.metaDetails
     */
    Meta getMetaDetail(String taskId);

    /**
     * minPageQuestionMeta。
     *
     * @param isTaskDetail Optional;null No filtering.0 TasksLevel,1 Details
     * @param pageNum      PageCode (from1 Start
     * @param pageSize     EveryPageNumber of bars;fallbacks when illegal
     */
    Paging<Meta> queryMeta(Integer isTaskDetail, int pageNum, int pageSize);

    /**
     * minPageAll over the clock.Meta（Avoid one-time loadingMemory）。
     *
     * @param isTaskDetail Optional;null No filtering.0 TasksLevel,1 Details
     * @param pageSize     EveryPageNumber of bars;fallbacks when illegal
     * @param pageConsumer PageRewind
     */
    void pageScanMetas(Integer isTaskDetail, int pageSize, Consumer<List<Meta>> pageConsumer);

    /**
     * Batch byTasks ID QuestionTasksAdministrationMeta（IS_TASK_DETAIL=0），key=taskId。
     */
    Map<String, Meta> getTaskMetaMap(List<String> taskIds);

    /**
     * Batch by AssociationID Query DetailsMeta（IS_TASK_DETAIL=1）。
     */
    Map<String, Meta> getDetailMetaMap(List<String> refIds);

    /**
     * Meta Count Atoms/29Incremental(Strictly.Library)：Press{@link MetaIncrement} DropLibraryAutomatic
     */
    void incrementMeta(MetaIncrement increment);

    /**
     * UpdateMeta Progress
     */
    void updateMetaProgress(String taskId, int state, Map<String, String> snapshot);

    /**
     * Add Meta。
     */
    String addMeta(Meta meta);

    /**
     * BatchAdd Meta（ConfigurePackageImport）。
     */
    void addMetaBatch(List<Meta> metas);

    /**
     * BatchUpdate Meta
     */
    void updateMetaBatch(List<Meta> metas);

    /**
     * UpdateMeta。
     */
    String updateMeta(Meta meta);

    /**
     * UpdateMeta(Unnotified)
     */
    void updateMetaWithoutNotice(Meta meta);

    /**
     * Delete Meta。
     */
    void removeMeta(String taskId);

    /**
     * Meta Total。
     */
    int countMeta();

    /**
     * ExportAll Meta Present.ZIP（meta.json array,minPageIt's written in style.
     *
     * @return Write a number
     */
    int writeMetasToZip(ZipOutputStream zos) throws IOException;

    /**
     * Frommeta.json SeriesImport。
     */
    void importMetaFromJson(String json);

    /**
     * DeleteTasksmetaCache
     */
    void removeMetaCache(String taskId);

    void resetMeta(String taskId);
}
