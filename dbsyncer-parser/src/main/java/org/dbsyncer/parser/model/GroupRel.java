/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * Business groupRelationship. Parent{@link #getType()} Fixed to{@link ConfigConstant#GROUP_REL}（Storage route;
 * Resource typeSee{@link #resourceType}。
 *
 * @author wuji
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
