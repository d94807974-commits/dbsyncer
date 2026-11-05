package org.realtimeetl.parser.util;

import org.realtimeetl.common.enums.CommonTaskTypeEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.GroupRel;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.MetaAgg;
import org.realtimeetl.parser.model.NoticeRule;
import org.realtimeetl.parser.model.NoticeStrategyConfig;
import org.realtimeetl.parser.model.Permission;
import org.realtimeetl.parser.model.ResourceAcl;
import org.realtimeetl.parser.model.Role;
import org.realtimeetl.parser.model.RolePermission;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.UserInfo;
import org.realtimeetl.parser.model.UserRole;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.model.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/11/19 21:59
 */
public abstract class ConfigModelUtil {

    /**
     * WillConfigureModelConvertArguments for storage(Split by Tableminreorganize+ json)。
     *
     * @param model ConfigureModel
     * @return Storage input
     */
    public static Map<String, Object> convertModelToMap(ConfigModel model) {
        // Meta：Strictly pressrealtime_etl_meta OpenminColumns,None name/type/json
        if (model instanceof Meta) {
            return convertMetaToMap((Meta) model);
        }
        // GroupRel：Strictly pressrealtime_etl_group_rel OpenminColumns,None name/json；RESOURCE_TYPE SaveResource type
        if (model instanceof GroupRel) {
            return convertGroupRelToMap((GroupRel) model);
        }
        if (model instanceof Role) {
            return convertRoleToMap((Role) model);
        }
        if (model instanceof Permission) {
            return convertPermissionToMap((Permission) model);
        }
        if (model instanceof RolePermission) {
            return convertRolePermissionToMap((RolePermission) model);
        }
        if (model instanceof UserRole) {
            return convertUserRoleToMap((UserRole) model);
        }
        if (model instanceof ResourceAcl) {
            return convertResourceAclToMap((ResourceAcl) model);
        }
        if (model instanceof NoticeRule) {
            return convertNoticeRuleToMap((NoticeRule) model);
        }
        if (model instanceof NoticeStrategyConfig) {
            return convertNoticeStrategyConfigToMap((NoticeStrategyConfig) model);
        }
        if (model instanceof MetaAgg) {
            return convertMetaDailyToMap((MetaAgg) model);
        }
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, model.getId());
        params.put(ConfigConstant.CONFIG_MODEL_TYPE, model.getType());
        params.put(ConfigConstant.CONFIG_MODEL_NAME, model.getName());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, model.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, model.getUpdateTime());
        params.put(ConfigConstant.CONFIG_MODEL_JSON, JsonUtil.objToJson(model));

        // Connector：Source/Objective CombinedRoleDropminColumn1/0），Easy.search PressRoleFilter
        if (model instanceof Connector) {
            Connector connector = (Connector) model;
            params.put(ConfigConstant.CONNECTOR_IS_SOURCE, connector.isSource() ? 1 : 0);
            params.put(ConfigConstant.CONNECTOR_IS_TARGET, connector.isTarget() ? 1 : 0);
            params.put(ConfigConstant.CONNECTOR_STATUS, connector.getStatus());
        }

        // Map relation：AssociationInfoDropminColumns
        if (model instanceof TableGroup) {
            TableGroup tableGroup = (TableGroup) model;
            params.put(ConfigConstant.TABLE_GROUP_TASK_ID, tableGroup.getTaskId());
            params.put(ConfigConstant.TABLE_GROUP_SORT_INDEX, tableGroup.getIndex());
            params.put(ConfigConstant.TABLE_GROUP_SOURCE_CONNECTOR_ID, StringUtil.getIfBlank(tableGroup.getSourceConnectorId(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_TARGET_CONNECTOR_ID, StringUtil.getIfBlank(tableGroup.getTargetConnectorId(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_SOURCE_DATABASE, StringUtil.getIfBlank(tableGroup.getSourceDatabase(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_TARGET_DATABASE, StringUtil.getIfBlank(tableGroup.getTargetDatabase(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_SOURCE_SCHEMA, StringUtil.getIfBlank(tableGroup.getSourceSchema(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_TARGET_SCHEMA, StringUtil.getIfBlank(tableGroup.getTargetSchema(), StringUtil.EMPTY));
            params.put(ConfigConstant.TABLE_GROUP_SOURCE_TABLE, tableName(tableGroup.getSourceTable()));
            params.put(ConfigConstant.TABLE_GROUP_TARGET_TABLE, tableName(tableGroup.getTargetTable()));
            params.put(ConfigConstant.TABLE_GROUP_SOURCE_TOTAL, tableGroup.getSourceTotal());
            params.put(ConfigConstant.TABLE_GROUP_TARGET_TOTAL, tableGroup.getTargetTotal());
        }
        return params;
    }

    /**
     * SingleUserLine transfer memory login Annual(realtime_etl_user OpenminColumns,None name/type/json)。
     */
    public static Map<String, Object> convertUserInfoToMap(UserInfo user) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, user.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, user.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, user.getUpdateTime());
        params.put(ConfigConstant.USER_USERNAME, user.getUsername());
        params.put(ConfigConstant.USER_PASSWORD, user.getPassword());
        params.put(ConfigConstant.USER_NICKNAME, user.getNickname());
        params.put(ConfigConstant.USER_ROLE, user.getRoleCode());
        params.put(ConfigConstant.USER_LOGIN_FAIL_COUNT, user.getLoginFailCount());
        params.put(ConfigConstant.USER_LOCK_UNTIL, user.getLockUntil());
        return params;
    }

    /**
     * CLD Revert model from storage line (and {@link #convertModelToMap}/{@link #convertUserInfoToMap} Symmetry.
     */
    public static <T> T parseFromRow(Map row, Class<T> clazz) {
        if (row == null || clazz == null) {
            return null;
        }
        if (Meta.class.equals(clazz)) {
            return (T) parseMeta(row);
        }
        if (UserInfo.class.equals(clazz)) {
            return (T) parseUserInfo(row);
        }
        if (GroupRel.class.equals(clazz)) {
            return (T) parseGroupRel(row);
        }
        if (Role.class.equals(clazz)) {
            Role role = JsonUtil.mapToObj(row, Role.class);
            if (role != null) {
                role.setType(ConfigConstant.ROLE);
            }
            return (T) role;
        }
        if (Permission.class.equals(clazz)) {
            Permission permission = JsonUtil.mapToObj(row, Permission.class);
            if (permission != null) {
                permission.setType(ConfigConstant.PERMISSION);
            }
            return (T) permission;
        }
        if (RolePermission.class.equals(clazz)) {
            RolePermission rel = JsonUtil.mapToObj(row, RolePermission.class);
            if (rel != null) {
                rel.setType(ConfigConstant.ROLE_PERMISSION);
            }
            return (T) rel;
        }
        if (UserRole.class.equals(clazz)) {
            UserRole rel = JsonUtil.mapToObj(row, UserRole.class);
            if (rel != null) {
                rel.setType(ConfigConstant.USER_ROLE_REL);
            }
            return (T) rel;
        }
        if (ResourceAcl.class.equals(clazz)) {
            ResourceAcl acl = JsonUtil.mapToObj(row, ResourceAcl.class);
            if (acl != null) {
                acl.setType(ConfigConstant.RESOURCE_ACL);
            }
            return (T) acl;
        }
        if (NoticeRule.class.equals(clazz)) {
            NoticeRule rule = JsonUtil.mapToObj(row, NoticeRule.class);
            if (rule != null) {
                rule.setType(ConfigConstant.NOTICE_RULE);
            }
            return (T) rule;
        }
        if (NoticeStrategyConfig.class.equals(clazz)) {
            NoticeStrategyConfig strategy = JsonUtil.mapToObj(row, NoticeStrategyConfig.class);
            if (strategy != null) {
                strategy.setType(ConfigConstant.NOTICE_STRATEGY);
            }
            return (T) strategy;
        }
        if (MetaAgg.class.equals(clazz)) {
            MetaAgg metaAgg = JsonUtil.mapToObj(row, MetaAgg.class);
            if (metaAgg != null) {
                metaAgg.setType(ConfigConstant.META_DAILY);
            }
            return (T) metaAgg;
        }
        Object json = row.get(ConfigConstant.CONFIG_MODEL_JSON);
        if (json == null) {
            return null;
        }
        return JsonUtil.jsonToObj(String.valueOf(json), clazz);
    }

    /**
     * GroupRelationship flow model(s)RESOURCE_TYPE → resourceType）。
     */
    private static GroupRel parseGroupRel(Map row) {
        return JsonUtil.mapToObj(row, GroupRel.class);
    }

    /**
     * GroupRelationship storage participation:resourceType → RESOURCE_TYPE，Do Not Writename/json。
     */
    private static Map<String, Object> convertGroupRelToMap(GroupRel rel) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, rel.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, rel.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, rel.getUpdateTime());
        params.put(ConfigConstant.GROUP_REL_GROUP_ID, StringUtil.getIfBlank(rel.getGroupId(), StringUtil.EMPTY));
        params.put(ConfigConstant.GROUP_REL_RESOURCE_TYPE, StringUtil.getIfBlank(rel.getResourceType(), StringUtil.EMPTY));
        params.put(ConfigConstant.GROUP_REL_RESOURCE_ID, StringUtil.getIfBlank(rel.getResourceId(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertRoleToMap(Role role) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, role.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, role.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, role.getUpdateTime());
        params.put(ConfigConstant.CONFIG_MODEL_NAME, StringUtil.getIfBlank(role.getName(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertPermissionToMap(Permission permission) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, permission.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, permission.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, permission.getUpdateTime());
        params.put(ConfigConstant.PERMISSION_CODE, StringUtil.getIfBlank(permission.getCode(), StringUtil.EMPTY));
        params.put(ConfigConstant.CONFIG_MODEL_NAME, StringUtil.getIfBlank(permission.getName(), StringUtil.EMPTY));
        params.put(ConfigConstant.PERMISSION_MODULE, StringUtil.getIfBlank(permission.getModule(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertRolePermissionToMap(RolePermission rel) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, rel.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, rel.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, rel.getUpdateTime());
        params.put(ConfigConstant.ROLE_PERMISSION_ROLE_ID, StringUtil.getIfBlank(rel.getRoleId(), StringUtil.EMPTY));
        params.put(ConfigConstant.ROLE_PERMISSION_PERMISSION_ID, StringUtil.getIfBlank(rel.getPermissionId(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertUserRoleToMap(UserRole rel) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, rel.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, rel.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, rel.getUpdateTime());
        params.put(ConfigConstant.USER_ROLE_USER_ID, StringUtil.getIfBlank(rel.getUserId(), StringUtil.EMPTY));
        params.put(ConfigConstant.USER_ROLE_ROLE_ID, StringUtil.getIfBlank(rel.getRoleId(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertResourceAclToMap(ResourceAcl acl) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, acl.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, acl.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, acl.getUpdateTime());
        params.put(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, acl.getSubjectType());
        params.put(ConfigConstant.RESOURCE_ACL_SUBJECT_ID, StringUtil.getIfBlank(acl.getSubjectId(), StringUtil.EMPTY));
        params.put(ConfigConstant.GROUP_REL_RESOURCE_TYPE, StringUtil.getIfBlank(acl.getResourceType(), StringUtil.EMPTY));
        params.put(ConfigConstant.GROUP_REL_RESOURCE_ID, StringUtil.getIfBlank(acl.getResourceId(), StringUtil.EMPTY));
        params.put(ConfigConstant.RESOURCE_ACL_PERMISSION, acl.getPermission());
        return params;
    }

    private static Map<String, Object> convertNoticeRuleToMap(NoticeRule rule) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, rule.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, rule.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, rule.getUpdateTime());
        params.put(ConfigConstant.NOTICE_RULE_SCOPE_TYPE, StringUtil.getIfBlank(rule.getScopeType(), StringUtil.EMPTY));
        params.put(ConfigConstant.NOTICE_RULE_SCOPE_ID, StringUtil.getIfBlank(rule.getScopeId(), StringUtil.EMPTY));
        params.put(ConfigConstant.NOTICE_RULE_AT_ALL, rule.getAtAll());
        params.put(ConfigConstant.NOTICE_RULE_AT_PHONES, StringUtil.getIfBlank(rule.getAtPhones(), StringUtil.EMPTY));
        params.put(ConfigConstant.NOTICE_RULE_EMAILS, StringUtil.getIfBlank(rule.getEmails(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertNoticeStrategyConfigToMap(NoticeStrategyConfig config) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, config.getId());
        params.put(ConfigConstant.CONFIG_MODEL_NAME, StringUtil.getIfBlank(config.getName(), StringUtil.EMPTY));
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, config.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, config.getUpdateTime());
        params.put(ConfigConstant.NOTICE_STRATEGY_NOTICE_TYPES, config.getNoticeTypes());
        params.put(ConfigConstant.NOTICE_STRATEGY_ENABLED, config.getEnabled());
        params.put(ConfigConstant.NOTICE_STRATEGY_FREQUENCY, config.getFrequency());
        params.put(ConfigConstant.NOTICE_STRATEGY_CRON, StringUtil.getIfBlank(config.getCron(), StringUtil.EMPTY));
        params.put(ConfigConstant.NOTICE_STRATEGY_REMARK, StringUtil.getIfBlank(config.getRemark(), StringUtil.EMPTY));
        return params;
    }

    private static Map<String, Object> convertMetaDailyToMap(MetaAgg daily) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, daily.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, daily.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, daily.getUpdateTime());
        params.put(ConfigConstant.META_DAILY_STAT_DATE, StringUtil.getIfBlank(daily.getStatDate(), StringUtil.EMPTY));
        params.put(ConfigConstant.META_TOTAL, daily.getTotal());
        params.put(ConfigConstant.META_SUCCESS, daily.getSuccess());
        params.put(ConfigConstant.META_FAIL, daily.getFail());
        return params;
    }

    private static Meta parseMeta(Map row) {
        Map<String, Object> data = new HashMap<>(row);
        Object snapshot = data.get(ConfigConstant.META_SNAPSHOT);
        if (snapshot instanceof String && StringUtil.isNotBlank((String) snapshot)) {
            data.put(ConfigConstant.META_SNAPSHOT, JsonUtil.parseMap((String) snapshot));
        }
        Meta meta = JsonUtil.mapToObj(data, Meta.class);
        return meta == null ? new Meta() : meta;
    }

    private static UserInfo parseUserInfo(Map row) {
        HashMap data = new HashMap<>(row);
        // Listingrole Corresponding ModelField roleCode
        Object role = data.remove(ConfigConstant.USER_ROLE);
        if (role != null) {
            data.put("roleCode", role);
        }
        return JsonUtil.mapToObj(data, UserInfo.class);
    }

    private static Map<String, Object> convertMetaToMap(Meta meta) {
        Map<String, Object> params = new HashMap<>();
        params.put(ConfigConstant.CONFIG_MODEL_ID, meta.getId());
        params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, meta.getCreateTime());
        params.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, meta.getUpdateTime());
        params.put(ConfigConstant.META_START_TIME, meta.getStartTime());
        params.put(ConfigConstant.META_TASK_ID, meta.getTaskId());
        params.put(ConfigConstant.META_STATE, meta.getState());
        params.put(ConfigConstant.META_IS_TASK_DETAIL, meta.getIsTaskDetail());
        params.put(ConfigConstant.META_TOTAL, meta.getTotal() == null ? 0L : meta.getTotal().get());
        params.put(ConfigConstant.META_SUCCESS, meta.getSuccess() == null ? 0L : meta.getSuccess().get());
        params.put(ConfigConstant.META_FAIL, meta.getFail() == null ? 0L : meta.getFail().get());
        params.put(ConfigConstant.META_DIFF, meta.getDiff() == null ? 0L : meta.getDiff().get());
        params.put(ConfigConstant.META_FIXED, meta.getFixed() == null ? 0L : meta.getFixed().get());
        params.put(ConfigConstant.META_SNAPSHOT, JsonUtil.objToJson(meta.getSnapshot() == null ? new HashMap<>() : meta.getSnapshot()));
        return params;
    }

    private static String tableName(Table table) {
        return table == null || StringUtil.isBlank(table.getName()) ? StringUtil.EMPTY : table.getName();
    }

    /**
     * Press ModelClass Route storage form.
     */
    public static StorageEnum getStorageEnum(Class<?> clazz) {
        try {
            ConfigModel model = (ConfigModel) clazz.newInstance();
            return getStorageEnum(model.getType());
        } catch (Exception e) {
            return StorageEnum.CONFIG;
        }
    }

    /**
     * BasisConfigureTypeRoute Configuration
     * <p>SyncMapping andEnterpriseShh! EffectivenessTasks（validateSync / databaseSync）Integration{@code realtime_etl_task}。
     *
     * @param type ConfigureType {@link ConfigConstant} / {@link CommonTaskTypeEnum#name()}
     * @return Storage Enumeration
     */
    public static StorageEnum getStorageEnum(String type) {
        if (type == null) {
            return StorageEnum.CONFIG;
        }
        switch (type) {
            case ConfigConstant.USER:
                return StorageEnum.USER;
            case ConfigConstant.CONNECTOR:
                return StorageEnum.CONNECTOR;
            case ConfigConstant.MAPPING:
            case ConfigConstant.TASK:
            case ConfigConstant.VALIDATE_SYNC:
            case ConfigConstant.DATABASE_SYNC:
                // Synchronization/validation/transferrealtime_etl_task
                return StorageEnum.TASK;
            case ConfigConstant.TABLE_GROUP:
                return StorageEnum.TABLE_GROUP;
            case ConfigConstant.META:
                return StorageEnum.META;
            case ConfigConstant.GROUP:
                return StorageEnum.GROUP;
            case ConfigConstant.GROUP_REL:
                return StorageEnum.GROUP_REL;
            case ConfigConstant.ROLE:
                return StorageEnum.ROLE;
            case ConfigConstant.PERMISSION:
                return StorageEnum.PERMISSION;
            case ConfigConstant.ROLE_PERMISSION:
                return StorageEnum.ROLE_PERMISSION;
            case ConfigConstant.USER_ROLE_REL:
                return StorageEnum.USER_ROLE;
            case ConfigConstant.RESOURCE_ACL:
                return StorageEnum.RESOURCE_ACL;
            case ConfigConstant.NOTICE_RULE:
                return StorageEnum.NOTICE_RULE;
            case ConfigConstant.NOTICE_STRATEGY:
                return StorageEnum.NOTICE_STRATEGY;
            case ConfigConstant.META_DAILY:
                return StorageEnum.META_DAILY;
            default:
                // Follow-upInsertYes.CommonTaskTypeEnum Keep going.task Table
                return CommonTaskTypeEnum.parse(type) != null ? StorageEnum.TASK : StorageEnum.CONFIG;
        }
    }
}
