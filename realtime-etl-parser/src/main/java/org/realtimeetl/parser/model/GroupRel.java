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

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * Business groupRelationship. Parent{@link #getType()} Fixed to{@link ConfigConstant#GROUP_REL}（Storage route;
 * Resource typeSee{@link #resourceType}。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public class GroupRel extends ConfigModel {

    /**
     * Group ID
     */
    private String groupId;

    /**
     * Resource type：connector / mapping / validateSync / databaseSync
     */
    private String resourceType;

    /**
     * Resource ID（ConnectorTasks ID）
     */
    private String resourceId;

    public GroupRel() {
        super.setName("");
        super.setType(ConfigConstant.GROUP_REL);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
