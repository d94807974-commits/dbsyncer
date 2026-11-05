/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.TableGroupPicker;

import java.util.List;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-01-16 23:48
 */
public interface TableGroupContext {

    void put(Mapping mapping, List<TableGroup> tableGroups);

    void update(Mapping mapping, List<TableGroup> tableGroups);

    List<TableGroupPicker> getTableGroupPickers(String taskId, String tableName);

    void clear(String taskId);
}
