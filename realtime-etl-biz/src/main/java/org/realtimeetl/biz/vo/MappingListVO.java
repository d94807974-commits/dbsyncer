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
package org.realtimeetl.biz.vo;

/**
 * 同步任务列表行（仅列表展示字段，不含完整配置）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-29
 */
public class MappingListVO {

    private String id;
    private String name;
    private String model;
    private MappingListConnectorVO sourceConnector;
    private MappingListConnectorVO targetConnector;
    private MappingListMetaVO meta;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public MappingListConnectorVO getSourceConnector() {
        return sourceConnector;
    }

    public void setSourceConnector(MappingListConnectorVO sourceConnector) {
        this.sourceConnector = sourceConnector;
    }

    public MappingListConnectorVO getTargetConnector() {
        return targetConnector;
    }

    public void setTargetConnector(MappingListConnectorVO targetConnector) {
        this.targetConnector = targetConnector;
    }

    public MappingListMetaVO getMeta() {
        return meta;
    }

    public void setMeta(MappingListMetaVO meta) {
        this.meta = meta;
    }
}
