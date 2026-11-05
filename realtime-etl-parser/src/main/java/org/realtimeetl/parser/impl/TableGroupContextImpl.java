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
package org.realtimeetl.parser.impl;

import org.realtimeetl.parser.TableGroupContext;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.TableGroupPicker;
import org.realtimeetl.parser.util.PickerUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-01-16 23:34
 */
@Component
public final class TableGroupContextImpl implements TableGroupContext {

    /**
     * MappingMap relation
     */
    private final Map<String, InnerMapping> tableGroupMap = new ConcurrentHashMap<>();

    @Override
    public void put(Mapping mapping, List<TableGroup> tableGroups) {
        tableGroupMap.computeIfAbsent(mapping.getId(), k -> {
            InnerMapping innerMap = new InnerMapping();
            tableGroups.forEach(tableGroup -> {
                String sourceTableName = tableGroup.getSourceTable().getName();
                innerMap.add(sourceTableName, PickerUtil.mergeTableGroupConfig(mapping, tableGroup));
            });
            return innerMap;
        });
    }

    @Override
    public void update(Mapping mapping, List<TableGroup> tableGroups) {
        tableGroupMap.computeIfPresent(mapping.getId(), (k, innerMap) -> {
            // FirstClearMap relation，UpdateMap relation
            tableGroups.stream().findFirst().ifPresent(tableGroup -> innerMap.remove(tableGroup.getSourceTable().getName()));
            tableGroups.forEach(tableGroup -> {
                String sourceTableName = tableGroup.getSourceTable().getName();
                innerMap.add(sourceTableName, PickerUtil.mergeTableGroupConfig(mapping, tableGroup));
            });
            return innerMap;
        });
    }

    @Override
    public List<TableGroupPicker> getTableGroupPickers(String taskId, String tableName) {
        List<TableGroupPicker> list = new ArrayList<>();
        tableGroupMap.computeIfPresent(taskId, (k, innerMapping) -> {
            innerMapping.pickerMap.computeIfPresent(tableName, (x, pickers) -> {
                list.addAll(pickers);
                return pickers;
            });
            return innerMapping;
        });
        return list;
    }

    @Override
    public void clear(String taskId) {
        tableGroupMap.remove(taskId);
    }

    static final class InnerMapping {

        Map<String, List<TableGroupPicker>> pickerMap = new ConcurrentHashMap<>();

        public void add(String tableName, TableGroup tableGroup) {
            pickerMap.computeIfAbsent(tableName, k -> new ArrayList<>()).add(new TableGroupPicker(tableGroup));
        }

        public void remove(String tableName) {
            pickerMap.remove(tableName);
        }
    }
}
