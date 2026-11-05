/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.Mapping;

import java.util.List;
import java.util.function.Consumer;

/**
 * Sync task
 *
 * @author wuji
 * @version 1.0.0
 */
public interface MappingProfile {

    /**
     * Pressid QuestionMapping TasksConfigure。
     */
    Mapping get(String id);

    /**
     * InsertTasksConfigure。
     */
    String add(Mapping mapping);

    /**
     * BatchInsertTasksConfigure。
     */
    void addBatch(List<Mapping> mappings);

    /**
     * UpdateTasksConfigure。
     */
    String update(Mapping mapping);

    /**
     * DeleteTasksConfigure
     */
    void delete(String id);

    void clearRunData(String id);

    /**
     * DeleteTasksIt's time to clean up.Row data（PhysicsDROP DetailsminTable.
     */
    void dropTaskDetailTable(String id);

    /**
     * Press ModelTypeminPageQuestionTasks，Optional PressNameBlurSearch。
     *
     * @param searchKey optional; non-empty time pair{@code name} Do it.LIKE
     * @param ids optional 38.ID IN Filter.
     */
    Paging<Mapping> query(int pageNum, int pageSize, String searchKey, List<String> ids);

    /**
     * Press ModelTypeminPageAll over the clock.AllTasks。
     */
    void pageScanTasks(int pageSize, Consumer<List<Mapping>> pageConsumer);

}
