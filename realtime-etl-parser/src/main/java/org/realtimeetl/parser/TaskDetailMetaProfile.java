/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.Meta;

import java.util.List;

/**
 * TasksDetailsmetaImplementation resultsTable
 *
 * @author wuji
 * @version 1.0.0
 */
public interface TaskDetailMetaProfile {

    /**
     * AccessTasksYes.metaCache
     */
    Meta getMeta(String tableGroupId);

    /**
     * Add Meta。
     */
    String add(Meta meta);

    /**
     * UpdateMeta。
     */
    String update(Meta meta);

    /**
     * Delete Meta。
     */
    void remove(String tableGroupId);

    /**
     * ClearDetailsminPre-empt empty table after table dataTasksIt's still in use.
     */
    void clearData(String taskId);

    /**
     * PhysicsDeleteDetailsminTable , No ReconstructionDeleteTasksused).
     */
    void dropTaskDetailTable(String taskId);

    /**
     * DeleteAllTask detail level
     */
    void deleteMetaByTableGroupIds(List<String> tableGroupIds);
}
