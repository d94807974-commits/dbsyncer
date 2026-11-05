/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.enums;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019-11-12 20:29
 */
public enum StorageEnum {

    /**
     * GlobalConfigure：Shrink AsSystem settings(system)/AnnouncementsConfigure(notice)
     */
    CONFIG("config"),
    /**
     * User Configuration
     */
    USER("user"),
    /**
     * ConnectConfigure
     */
    CONNECTOR("connector"),
    /**
     * TableMappingConfigure
     */
    TABLE_GROUP("table_group"),
    /**
     * Mandate performance(meta)
     */
    META("meta"),
    /**
     * Log：Connector、Mapping、Mapping、SyncInfo、System Log
     */
    LOG("log"),
    /**
     * Tasks
     */
    TASK("task"),
    /**
     * TasksImplementation Details(HarmonizationSync data/Data validation/Database migration Details)
     */
    TASK_DETAIL("task_detail"),
    /**
     * ClusterNodes
     */
    CLUSTER_NODE("cluster_node"),
    /**
     * ClusterTasksMovement control
     */
    CLUSTER_TASK("cluster_task"),
    /**
     * TasksminPlan
     */
    TASK_PLAN("cluster_task_plan"),
    /**
     * Business group
     */
    GROUP("group"),
    /**
     * Business groupRelations
     */
    GROUP_REL("group_rel"),
    /**
     * Role
     */
    ROLE("role"),
    /**
     * PermissionsPoints 71
     */
    PERMISSION("permission"),
    /**
     * Role-Permissions
     */
    ROLE_PERMISSION("role_permission"),
    /**
     * User-Role
     */
    USER_ROLE("user_role"),
    /**
     * ResourceAccess control
     */
    RESOURCE_ACL("resource_acl"),
    /**
     * Alert notificationRule
     */
    NOTICE_RULE("notice_rule"),
    /**
     * Police strategy Corrupt
     */
    NOTICE_STRATEGY("notice_strategy"),
    /**
     * Sync task Meta Summary
     */
    META_DAILY("meta_agg");

    private final String type;

    StorageEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
