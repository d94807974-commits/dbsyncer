/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.GroupSearchFilter;
import org.dbsyncer.biz.GroupService;
import org.dbsyncer.parser.NoticeRuleProfile;
import org.dbsyncer.biz.ResourceAclService;
import org.dbsyncer.biz.enums.ResourceTypeEnum;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.GroupProfile;
import org.dbsyncer.parser.GroupRelProfile;
import org.dbsyncer.parser.model.Group;
import org.dbsyncer.parser.model.GroupRel;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * {@link GroupService} Achieved.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
@Service
public class GroupServiceImpl implements GroupService {

    private static final int NAME_MAX_LEN = 50;

    @Resource
    private GroupProfile groupProfile;

    @Resource
    private GroupRelProfile groupRelProfile;

    @Resource
    private ResourceAclService resourceAclService;

    @Resource
    private NoticeRuleProfile noticeRuleProfile;

    @Override
    public String add(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String name = validateAndNormalizeName(params.get(ConfigConstant.CONFIG_MODEL_NAME), null);
        long now = System.currentTimeMillis();
        Group group = new Group();
        group.setName(name);
        group.setCreateTime(now);
        group.setUpdateTime(now);
        return groupProfile.addGroup(group);
    }

    @Override
    public String edit(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String id = StringUtil.trim(params.get(ConfigConstant.CONFIG_MODEL_ID));
        Assert.hasText(id, "Group ID cannot be empty");
        Group group = groupProfile.getGroup(id);
        Assert.notNull(group, "Group does not exist");
        String name = validateAndNormalizeName(params.get(ConfigConstant.CONFIG_MODEL_NAME), id);
        group.setName(name);
        group.setUpdateTime(System.currentTimeMillis());
        return groupProfile.updateGroup(group);
    }

    @Override
    public String remove(String id) {
        Assert.hasText(id, "Group ID cannot be empty");
        Group group = groupProfile.getGroup(id);
        Assert.notNull(group, "Group does not exist");
        // Iron.②：Clear first.rel Then delete the group.
        groupRelProfile.removeByGroup(id);
        groupProfile.removeGroup(id);
        //DeleteAuthorizationResource
        resourceAclService.revokeAllForResource(ConfigConstant.GROUP, id);
        //DeleteAlert notificationRule
        noticeRuleProfile.removeByScopeId(ConfigConstant.GROUP, id);
        return id;
    }

    @Override
    public List<Group> list() {
        return groupProfile.listGroups();
    }

    @Override
    public Group get(String id) {
        return groupProfile.getGroup(id);
    }

    @Override
    public int countMembers(String id) {
        Assert.hasText(id, "Group ID cannot be empty");
        return groupRelProfile.countByGroup(id);
    }

    @Override
    public void bind(String groupId, String resourceType, String resourceId) {
        Assert.hasText(resourceType, "Resource type cannot be empty");
        Assert.hasText(resourceId, "Resource ID cannot be empty.");
        if (!ResourceTypeEnum.isLeaf(resourceType)) {
            throw new BizException("Unsupported resource type: " + resourceType);
        }
        // EmptygroupId = ClearTheResourceAllAttribution
        if (StringUtil.isBlank(groupId)) {
            groupRelProfile.removeByResource(resourceType, resourceId);
            return;
        }
        Group group = groupProfile.getGroup(groupId);
        Assert.notNull(group, "Group does not exist");
        if (groupRelProfile.getByGroupAndResource(groupId, resourceType, resourceId) != null) {
            return;
        }
        long now = System.currentTimeMillis();
        GroupRel rel = new GroupRel();
        rel.setGroupId(groupId);
        rel.setResourceType(resourceType);
        rel.setResourceId(resourceId);
        rel.setCreateTime(now);
        rel.setUpdateTime(now);
        groupRelProfile.add(rel);
    }

    @Override
    public void bindBatch(String groupId, String resourceType, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return;
        }
        for (String resourceId : resourceIds) {
            if (StringUtil.isBlank(resourceId)) {
                continue;
            }
            bind(groupId, resourceType, resourceId);
        }
    }

    @Override
    public void unbind(String groupId, String resourceType, String resourceId) {
        Assert.hasText(groupId, "Group ID cannot be empty");
        Assert.hasText(resourceType, "Resource type cannot be empty");
        Assert.hasText(resourceId, "Resource ID cannot be empty.");
        if (!ResourceTypeEnum.isLeaf(resourceType)) {
            throw new BizException("Unsupported resource type: " + resourceType);
        }
        groupRelProfile.removeByGroupAndResource(groupId, resourceType, resourceId);
    }

    @Override
    public void unbindBatch(String groupId, String resourceType, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return;
        }
        for (String resourceId : resourceIds) {
            if (StringUtil.isBlank(resourceId)) {
                continue;
            }
            unbind(groupId, resourceType, resourceId);
        }
    }

    @Override
    public List<GroupRel> listByResource(String resourceType, String resourceId) {
        return groupRelProfile.listByResource(resourceType, resourceId);
    }

    @Override
    public List<String> listResourceIds(String groupId, String resourceType) {
        return groupRelProfile.listResourceIds(groupId, resourceType);
    }

    @Override
    public GroupSearchFilter resolveSearchFilter(String groupIdParam, String resourceType) {
        // Promotenull = Do Not FilterAll）
        if (StringUtil.isBlank(groupIdParam)) {
            return GroupSearchFilter.NONE;
        }
        List<String> ids = listResourceIds(groupIdParam, resourceType);
        if (CollectionUtils.isEmpty(ids)) {
            return GroupSearchFilter.EMPTY;
        }
        return new GroupSearchFilter(false, ids);
    }

    /**
     * Verify and regularizeGroup Name；excludeId When not available, allow the same name to belong to itself.EditThe scene.
     */
    private String validateAndNormalizeName(String rawName, String excludeId) {
        String name = StringUtil.trim(rawName);
        Assert.hasText(name, "Group name cannot be empty");
        if (name.length() > NAME_MAX_LEN) {
            throw new BizException("Group names cannot exceed" + NAME_MAX_LEN + "Character");
        }
        Group existing = groupProfile.getByName(name);
        if (existing != null && !StringUtil.equals(existing.getId(), excludeId)) {
            throw new BizException("Group name already exists");
        }
        return name;
    }

}
