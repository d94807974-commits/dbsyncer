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
package org.realtimeetl.parser.model;

import org.realtimeetl.sdk.model.Field;

import java.util.List;
import java.util.Map;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-01-18 22:53
 */
public class TableGroupPicker {

    private final TableGroup tableGroup;

    private final Picker picker;

    private final List<Field> sourceFields;
    private final List<Field> targetFields;
    private final Map<String, Field> targetFieldMap;

    public TableGroupPicker(TableGroup tableGroup) {
        this.tableGroup = tableGroup;
        this.picker = new Picker(tableGroup);
        this.sourceFields = picker.getSourceFields();
        this.targetFields = picker.getTargetFields();
        this.targetFieldMap = picker.getTargetFieldMap();
    }

    public TableGroup getTableGroup() {
        return tableGroup;
    }

    public Picker getPicker() {
        return picker;
    }

    public List<Field> getSourceFields() {
        return sourceFields;
    }

    public List<Field> getTargetFields() {
        return targetFields;
    }

    public Map<String, Field> getTargetFieldMap() {
        return targetFieldMap;
    }
}
