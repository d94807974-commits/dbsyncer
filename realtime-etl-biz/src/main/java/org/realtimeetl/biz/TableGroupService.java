/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.TableGroup;

import java.util.List;
import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/11/27 23:14
 */
public interface TableGroupService {

    /**
     * InsertTable relations (meetings)SubmitTable StatisticsTasks）。
     */
    String add(Map<String, String> params);

    /**
     * BatchInsertForm Timely, NoSubmitStatisticsTasks（Unified by CallerMappingCountTask）。
     */
    int addWithoutCount(Map<String, String> params);

    /**
     * Press MultigroupParameterBatchInsertTable relation (each group may contain independent)fieldMappings）；Skipped;noSubmitStatisticsTasks。
     */
    int addWithoutCountBatch(List<Map<String, String>> paramsList);

    /**
     * UpdateTable relation
     */
    String edit(Map<String, String> params);

    /**
     * Refresh tablesField
     */
    String refreshFields(String id);

    /**
     * DeleteTable relation
     */
    String remove(String mappingId, String ids);

    /**
     * Get Table Relationships
     */
    TableGroup getTableGroup(String id);

    /**
     * minPageSearch
     */
    Paging<TableGroup> search(Map<String, String> params);

    /**
     * ResetTasks Meta：ClearRunStatus，and can be selected for writingIncrementalpoints;no aggregationstotal。
     */
    void resetMeta(Mapping mapping, String metaSnapshot);

    /**
     * By Table GroupSOURCE_TOTAL Summarize and write backTasks Meta.total（AllThe volume.
     */
    Meta refreshMetaTotal(Mapping mapping, String metaSnapshot);
}
