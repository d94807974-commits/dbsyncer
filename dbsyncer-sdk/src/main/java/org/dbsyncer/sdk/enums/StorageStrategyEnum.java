/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

import org.dbsyncer.sdk.SdkException;
import org.dbsyncer.sdk.storage.Strategy;
import org.dbsyncer.sdk.storage.strategy.ClusterNodeStrategy;
import org.dbsyncer.sdk.storage.strategy.ClusterTaskStrategy;
import org.dbsyncer.sdk.storage.strategy.ConfigStrategy;
import org.dbsyncer.sdk.storage.strategy.ConnectorStrategy;
import org.dbsyncer.sdk.storage.strategy.GroupRelStrategy;
import org.dbsyncer.sdk.storage.strategy.GroupStrategy;
import org.dbsyncer.sdk.storage.strategy.LogStrategy;
import org.dbsyncer.sdk.storage.strategy.MetaAggStrategy;
import org.dbsyncer.sdk.storage.strategy.MetaStrategy;
import org.dbsyncer.sdk.storage.strategy.NoticeRuleStrategy;
import org.dbsyncer.sdk.storage.strategy.NoticeStrategyStorageStrategy;
import org.dbsyncer.sdk.storage.strategy.PermissionStrategy;
import org.dbsyncer.sdk.storage.strategy.ResourceAclStrategy;
import org.dbsyncer.sdk.storage.strategy.RolePermissionStrategy;
import org.dbsyncer.sdk.storage.strategy.RoleStrategy;
import org.dbsyncer.sdk.storage.strategy.TableGroupStrategy;
import org.dbsyncer.sdk.storage.strategy.TaskDetailStrategy;
import org.dbsyncer.sdk.storage.strategy.TaskPlanStrategy;
import org.dbsyncer.sdk.storage.strategy.TaskStrategy;
import org.dbsyncer.sdk.storage.strategy.UserRoleStrategy;
import org.dbsyncer.sdk.storage.strategy.UserStrategy;

/**
 * Storage Policy Count
 *
 * @author AE86
 * @version 1.0.0
 * @date 2023-12-12 00:51
 */
public enum StorageStrategyEnum {

    /**
     * ConfigurePolicy
     */
    CONFIG(StorageEnum.CONFIG, new ConfigStrategy()),

    /**
     * User ConfigurationPolicy
     */
    USER(StorageEnum.USER, new UserStrategy()),

    /**
     * ConnectConfigurePolicy
     */
    CONNECTOR(StorageEnum.CONNECTOR, new ConnectorStrategy()),

    /**
     * TableMappingPolicy
     */
    TABLE_GROUP(StorageEnum.TABLE_GROUP, new TableGroupStrategy()),

    /**
     * Mandate performance(meta)Policy
     */
    META(StorageEnum.META, new MetaStrategy()),

    /**
     * TasksImplementation DetailsPolicy(HarmonizationSync data/Data validation/Database migration Details)
     */
    TASK_DETAIL(StorageEnum.TASK_DETAIL, new TaskDetailStrategy()),

    /**
     * LogPolicy
     */
    LOG(StorageEnum.LOG, new LogStrategy()),

    /**
     * TasksPolicy
     */
    TASK(StorageEnum.TASK, new TaskStrategy()),

    /**
     * ClusterNodes
     */
    CLUSTER_NODE(StorageEnum.CLUSTER_NODE, new ClusterNodeStrategy()),

    /**
     * ClusterTasksMovement control
     */
    CLUSTER_TASK(StorageEnum.CLUSTER_TASK, new ClusterTaskStrategy()),

    /**
     * TasksminPlan
     */
    TASK_PLAN(StorageEnum.TASK_PLAN, new TaskPlanStrategy()),

    /**
     * Business group
     */
    GROUP(StorageEnum.GROUP, new GroupStrategy()),

    /**
     * Business groupRelations
     */
    GROUP_REL(StorageEnum.GROUP_REL, new GroupRelStrategy()),

    /**
     * Role
     */
    ROLE(StorageEnum.ROLE, new RoleStrategy()),

    /**
     * PermissionsPoints 71
     */
    PERMISSION(StorageEnum.PERMISSION, new PermissionStrategy()),

    /**
     * Role-Permissions
     */
    ROLE_PERMISSION(StorageEnum.ROLE_PERMISSION, new RolePermissionStrategy()),

    /**
     * User-Role
     */
    USER_ROLE(StorageEnum.USER_ROLE, new UserRoleStrategy()),

    /**
     * ResourceAccess control
     */
    RESOURCE_ACL(StorageEnum.RESOURCE_ACL, new ResourceAclStrategy()),

    /**
     * Alert notificationRule
     */
    NOTICE_RULE(StorageEnum.NOTICE_RULE, new NoticeRuleStrategy()),

    /**
     * Police strategy Corrupt
     */
    NOTICE_STRATEGY(StorageEnum.NOTICE_STRATEGY, new NoticeStrategyStorageStrategy()),

    /**
     * Sync task Meta Summary
     */
    META_DAILY(StorageEnum.META_DAILY, new MetaAggStrategy());

    private final StorageEnum type;
    private final Strategy strategy;

    StorageStrategyEnum(StorageEnum type, Strategy strategy) {
        this.type = type;
        this.strategy = strategy;
    }

    public static Strategy getStrategy(StorageEnum type) throws SdkException {
        for (StorageStrategyEnum e : StorageStrategyEnum.values()) {
            if (type == e.getType()) {
                return e.getStrategy();
            }
        }
        throw new SdkException(String.format("StorageStrategy type \"%s\" does not exist.", type));
    }

    public StorageEnum getType() {
        return type;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}