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
 * Sync taskList：Source/Target Connectioninstrument summary.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-29
 */
public class MappingListConnectorVO {

    private String name;
    private MappingListConnectorConfigVO config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MappingListConnectorConfigVO getConfig() {
        return config;
    }

    public void setConfig(MappingListConnectorConfigVO config) {
        this.config = config;
    }
}
