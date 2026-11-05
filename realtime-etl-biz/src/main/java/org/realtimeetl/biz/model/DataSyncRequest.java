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
package org.realtimeetl.biz.model;

import java.util.List;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-23 15:24
 */
public class DataSyncRequest {

    /**
     * MappingTasksid
     */
    private String mappingId;

    /**
     * TableTasksid
     */
    private String tableGroupId;

    /**
     * Change data
     */
    private List<DataSyncEvent> dataList;

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getTableGroupId() {
        return tableGroupId;
    }

    public void setTableGroupId(String tableGroupId) {
        this.tableGroupId = tableGroupId;
    }

    public List<DataSyncEvent> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataSyncEvent> dataList) {
        this.dataList = dataList;
    }
}
