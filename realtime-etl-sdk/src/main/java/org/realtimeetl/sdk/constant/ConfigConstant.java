/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.constant;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2019-11-16 22:14
 */
public class ConfigConstant {

    /**
     * MaleTotalProperties
     */
    public static final String CONFIG_MODEL_ID = "id";
    public static final String CONFIG_MODEL_NAME = "name";
    public static final String CONFIG_MODEL_TYPE = "type";
    public static final String CONFIG_MODEL_CREATE_TIME = "createTime";
    public static final String CONFIG_MODEL_UPDATE_TIME = "updateTime";
    public static final String CONFIG_MODEL_JSON = "json";

    /**
     * ConfigureType
     */
    public static final String SYSTEM = "system";
    public static final String USER = "user";
    public static final String CONNECTOR = "connector";
    public static final String MAPPING = "mapping";
    public static final String TABLE_GROUP = "tableGroup";
    public static final String META = "meta";
    public static final String TASK = "task";
    public static final String GROUP = "group";
    public static final String GROUP_REL = "groupRel";
    public static final String ROLE = "role";
    public static final String PERMISSION = "permission";
    public static final String ROLE_PERMISSION = "rolePermission";
    /**
     * User-RoleRelationsConfigureType（Don't.andUserfor for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for for{@link #USER_ROLE} Confusion)
     */
    public static final String USER_ROLE_REL = "userRole";
    /**
     * ResourceAccess control
     */
    public static final String RESOURCE_ACL = "resourceAcl";
    /**
     * Alert notificationRule
     */
    public static final String NOTICE_RULE = "noticeRule";
    /**
     * Police strategy Corrupt
     */
    public static final String NOTICE_STRATEGY = "noticeStrategy";
    /**
     * Sync task Meta Summary
     */
    public static final String META_DAILY = "metaDaily";

    /**
     * ConnectorConfigureField
     */
    public static final String CONNECTOR_IS_SOURCE = "isSource";
    public static final String CONNECTOR_IS_TARGET = "isTarget";
    public static final String CONNECTOR_STATUS = "status";

    /**
     * EnterpriseShh! EffectivenessTasks type（Save{@code realtime_etl_task}），and {@code CommonTaskTypeEnum#name()} Consistent
     */
    public static final String VALIDATE_SYNC = "validateSync";
    public static final String DATABASE_SYNC = "databaseSync";

    /**
     * Business group relationship tables(realtime_etl_group_rel)OpenminColumns
     */
    public static final String GROUP_REL_GROUP_ID = "groupId";
    /**
     * Resource typereorganizeRESOURCE_TYPE（Model side reuse{@link #CONFIG_MODEL_TYPE}/{@code ConfigModel#type}）
     */
    public static final String GROUP_REL_RESOURCE_TYPE = "resourceType";
    public static final String GROUP_REL_RESOURCE_ID = "resourceId";

    /**
     * Menu Permission Table(realtime_etl_permission)OpenminColumns
     */
    public static final String PERMISSION_CODE = "code";
    public static final String PERMISSION_MODULE = "module";

    /**
     * RolePermissionsRelationship table(realtime_etl_role_permission)OpenminColumns
     */
    public static final String ROLE_PERMISSION_ROLE_ID = "roleId";
    public static final String ROLE_PERMISSION_PERMISSION_ID = "permissionId";

    /**
     * User Role Table(realtime_etl_user_role)OpenminColumns
     */
    public static final String USER_ROLE_USER_ID = "userId";
    public static final String USER_ROLE_ROLE_ID = "roleId";

    /**
     * Resource ACL Table(realtime_etl_resource_acl)OpenminColumns
     */
    public static final String RESOURCE_ACL_SUBJECT_TYPE = "subjectType";
    public static final String RESOURCE_ACL_SUBJECT_ID = "subjectId";
    public static final String RESOURCE_ACL_PERMISSION = "permission";

    /**
     * Rule sheet for alerts(realtime_etl_notice_rule)OpenminColumns
     */
    public static final String NOTICE_RULE_SCOPE_TYPE = "scopeType";
    public static final String NOTICE_RULE_SCOPE_ID = "scopeId";
    public static final String NOTICE_RULE_AT_ALL = "atAll";
    public static final String NOTICE_RULE_AT_PHONES = "atPhones";
    public static final String NOTICE_RULE_EMAILS = "emails";

    /**
     * Warning strategy forms(realtime_etl_notice_strategy)OpenminColumns
     */
    public static final String NOTICE_STRATEGY_ENABLED = "enabled";
    public static final String NOTICE_STRATEGY_FREQUENCY = "frequency";
    public static final String NOTICE_STRATEGY_CRON = "cron";
    public static final String NOTICE_STRATEGY_NOTICE_TYPES = "noticeTypes";
    public static final String NOTICE_STRATEGY_REMARK = "remark";

    /**
     * Meta Daily summary statement(realtime_etl_meta_agg)OpenminColumns
     */
    public static final String META_DAILY_STAT_DATE = "statDate";

    /**
     * Log Table(realtime_etl_log)Filter Columns
     */
    public static final String LOG_USERNAME = "username";
    public static final String LOG_IP = "ip";
    public static final String LOG_STATUS = "status";

    /**
     * UserTable(realtime_etl_user)OpenminColumns
     */
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_NICKNAME = "nickname";
    /**
     * ListingROLE，ModelField roleCode
     */
    public static final String USER_ROLE = "role";
    /**
     * Number of consecutive login failures
     */
    public static final String USER_LOGIN_FAIL_COUNT = "loginFailCount";
    /**
     * 锁定截止时间（毫秒时间戳，0 表示未锁定）
     */
    public static final String USER_LOCK_UNTIL = "lockUntil";

    /**
     * TableMappingTable(realtime_etl_table_group)OpenminColumns
     */
    public static final String TABLE_GROUP_TASK_ID = "taskId";
    public static final String TABLE_GROUP_SORT_INDEX = "sortIndex";
    public static final String TABLE_GROUP_SOURCE_CONNECTOR_ID = "sourceConnectorId";
    public static final String TABLE_GROUP_TARGET_CONNECTOR_ID = "targetConnectorId";
    public static final String TABLE_GROUP_SOURCE_DATABASE = "sourceDatabase";
    public static final String TABLE_GROUP_TARGET_DATABASE = "targetDatabase";
    public static final String TABLE_GROUP_SOURCE_SCHEMA = "sourceSchema";
    public static final String TABLE_GROUP_TARGET_SCHEMA = "targetSchema";
    public static final String TABLE_GROUP_SOURCE_TABLE = "sourceTable";
    public static final String TABLE_GROUP_TARGET_TABLE = "targetTable";
    public static final String TABLE_GROUP_SOURCE_TOTAL = "sourceTotal";
    public static final String TABLE_GROUP_TARGET_TOTAL = "targetTotal";

    /**
     * Task execution results table(realtime_etl_meta)OpenminColumns
     */
    public static final String META_TASK_ID = "taskId";
    public static final String META_STATE = "state";
    public static final String META_IS_TASK_DETAIL = "isTaskDetail";
    public static final String META_TOTAL = "total";
    public static final String META_SUCCESS = "success";
    public static final String META_FAIL = "fail";
    public static final String META_DIFF = "diff";
    public static final String META_FIXED = "fixed";
    public static final String META_SNAPSHOT = "snapshot";
    /**
     * Task Start Time（DropLibraryreorganizeSTART_TIME）
     */
    public static final String META_START_TIME = "startTime";

    /**
     * Cluster nodes(realtime_etl_cluster_node)
     */
    public static final String CLUSTER_NODE_ID = "nodeId";
    public static final String CLUSTER_IP = "ip";
    public static final String CLUSTER_HTTP_PORT = "httpPort";
    public static final String CLUSTER_WORKER_ID = "workerId";
    public static final String CLUSTER_STATUS = "status";
    public static final String CLUSTER_ROLE = "role";
    public static final String CLUSTER_TERM = "term";
    public static final String CLUSTER_HEARTBEAT_TIME = "heartbeatTime";
    public static final String CLUSTER_START_TIME = "startTime";

    /**
     * Cluster task schedule(realtime_etl_cluster_task)
     */
    public static final String CLUSTER_TASK_TABLE = "realtime_etl_cluster_task";
    public static final String SCHEDULE_NODE_ID = "nodeId";
    public static final String SCHEDULE_TASK_TYPE = "taskType";

    /**
     * Job Break Schedule(realtime_etl_cluster_task_plan)
     */
    public static final String CLUSTER_TASK_PLAN_TABLE = "realtime_etl_cluster_task_plan";
    public static final String PLAN_START_CURSOR = "startCursor";
    public static final String PLAN_END_CURSOR = "endCursor";
    public static final String PLAN_LAST_CURSOR = "lastCursor";
    public static final String PLAN_SUCCESS = "success";
    public static final String PLAN_FAIL = "fail";
    public static final String PLAN_LAST_PAGE = "lastPage";
    public static final String PLAN_STATUS = "status";

    /**
     * TasksAdministrationMeta.SNAPSHOT StatisticsDatabase migrationLibrary Map status SummaryJSON（Without Tabletables）
     */
    public static final String META_SNAPSHOT_DATABASE = "databaseSnapshots";

    /**
     * Result Meta.SNAPSHOT Inner key: Planned sheet running snapshotJSON（TASK_ID=detail.id）
     */
    public static final String META_SNAPSHOT_TABLE_ONE = "tableSnapshot";

    /**
     * Result Meta.SNAPSHOT StatisticsDatabase migrationThe CWC is now in force.Library Map index
     */
    public static final String META_SNAPSHOT_MAPPING_INDEX = "mappingIndex";

    /**
     * TasksImplementation DetailsTable(realtime_etl_task_detail)Streamlining Columns(PressTasksminTable)
     * <p>TYPE Sixty-first session{@link #CONFIG_MODEL_TYPE}
     */
    public static final String DETAIL_IS_SUCCESS = "isSuccess";
    public static final String DETAIL_TARGET_TABLE = "targetTable";
    public static final String DATA_TABLE_GROUP_ID = "tableGroupId";
    public static final String DATA_ERROR = "error";

    /**
     * Binlog / Detailed Load
     */
    public static final String BINLOG_DATA = "data";

    /**
     * Tasks/Frontend Compatibility Key
     */
    public static final String TASK_STATUS = "status";
    public static final String TASK_ID = "taskId";
    public static final String DATA_TARGET_TABLE_NAME = "targetTableName";
    public static final String TASK_SOURCE_TABLE_NAME = "sourceTableName";
    public static final String TASK_SOURCE_TOTAL = "sourceTotal";
    public static final String TASK_TARGET_TOTAL = "targetTotal";
    public static final String TASK_DIFF_TOTAL = "diffTotal";
    public static final String TASK_FIXED_TOTAL = "fixedTotal";
    public static final String TASK_CONTENT = "content";

    /**
     * Database migrationPresentationField(Table/VO Map key, non-independent line)
     */
    public static final String DATABASE_SYNC_DETAIL_TABLE_INDEX = "tableIndex";
    public static final String DATABASE_SYNC_DETAIL_SOURCE_DATABASE = "sourceDatabase";
    public static final String DATABASE_SYNC_DETAIL_SOURCE_SCHEMA = "sourceSchema";
    public static final String DATABASE_SYNC_DETAIL_TARGET_DATABASE = "targetDatabase";
    public static final String DATABASE_SYNC_DETAIL_TARGET_SCHEMA = "targetSchema";
    public static final String DATABASE_SYNC_DETAIL_SOURCE_TABLE = "sourceTable";
    public static final String DATABASE_SYNC_DETAIL_TARGET_TABLE = "targetTable";
    public static final String DATABASE_SYNC_DETAIL_SUCCESS_TOTAL = "successTotal";
    public static final String DATABASE_SYNC_DETAIL_FAIL_TOTAL = "failTotal";

    /**
     * InternalminPageLoad DefaultminPage/5/
     */
    public static final int PAGE_SIZE = 1000;

}
