/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.storage;

import org.dbsyncer.common.model.Paging;
import org.dbsyncer.sdk.enums.StorageEnum;
import org.dbsyncer.sdk.filter.Query;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Storage services (support records)Configure/Log/Sync data）
 *
 * @author AE86
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
