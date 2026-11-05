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
package org.realtimeetl.sdk.storage;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Storage services (support records)Configure/Log/Sync data）
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019-11-16 23:22
 */
public interface StorageService {

    /**
     * Initialize
     */
    void init(Properties properties);

    /**
     * Query All Data
     */
    Paging query(Query query);

    /**
     * PressPrimary keyQuery a single record (not to go)minPage、Do Not CountTotal）。
     */
    Map queryObject(Query query);

    /**
     * Based onConditionDelete
     */
    void delete(Query query);

    /**
     * ClearData/Log（ReservationsTable schema）。
     */
    void clear(StorageEnum type, String taskId);

    /**
     * DeleteStorageminFilm (Video)minTable physicsDROP）。
     * <p>DeleteTasksis used at times;and {@link #clear} It's different. It'll remove the physical table.
     */
    void drop(StorageEnum type, String taskId);

    /**
     * Add
     */
    void add(StorageEnum type, Map params);

    /**
     * Add
     */
    void add(StorageEnum type, String taskId, Map params);

    /**
     * BatchAdd
     */
    void addBatch(StorageEnum type, String taskId, List<Map> list);

    /**
     * Update
     */
    void edit(StorageEnum type, Map params);

    /**
     * Update
     */
    void edit(StorageEnum type, String taskId, Map params);

    /**
     * BatchUpdate
     */
    void editBatch(StorageEnum type, String taskId, List<Map> list);

    /**
     * Delete
     */
    void remove(StorageEnum type, String id);

    /**
     * Delete
     */
    void remove(StorageEnum type, String taskId, String id);

    /**
     * BatchDelete
     */
    void removeBatch(StorageEnum type, String taskId, List<String> ids);

    /**
     * AtomIncrementalUpdate (additional by column, ifCOL = GREATEST(COL + ?, 0)），For strict walking.LibraryDownMeta Counting;Resultless than0 Timepliers0
     *
     * @param type   StorageType
     * @param id     RecordsPrimary key
     * @param deltas ColumnsIncremental（key ColumnslabelName，value YesIncrementalValue）
     */
    void increment(StorageEnum type, String id, Map<String, Long> deltas);

    /**
     * Execute original queries SQL（System settingsLibrary）；{@link ExecuteRequest#isPaged()} Yestrue AIJminPage。
     *
     * @param query SQL andOptionalminPageParameter
     * @return Okay.List，NoneResultTimeList
     */
    List<Map<String, Object>> queryList(ExecuteRequest query);

    /**
     * Execute Native Updates
     */
    int update(String sql, Object[] args);

    /**
     * Execute original queries
     */
    <T> List<T> query(String sql, Object[] args, Class<T> beanClass);
}
