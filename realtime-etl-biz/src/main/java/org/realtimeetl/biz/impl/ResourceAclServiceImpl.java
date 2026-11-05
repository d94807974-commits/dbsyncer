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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.GroupSearchFilter;
import org.realtimeetl.biz.PermissionChecker;
import org.realtimeetl.biz.PermissionService;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.enums.ResourceAclPermissionEnum;
import org.realtimeetl.biz.enums.ResourceAclSubjectEnum;
import org.realtimeetl.biz.enums.ResourceTypeEnum;
import org.realtimeetl.biz.vo.ResourceAclVO;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.DatabaseSyncProfile;
import org.realtimeetl.parser.GroupProfile;
import org.realtimeetl.parser.GroupRelProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.RbacProfile;
import org.realtimeetl.parser.ResourceAclProfile;
import org.realtimeetl.parser.UserProfile;
import org.realtimeetl.parser.ValidateSyncProfile;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Group;
import org.realtimeetl.parser.model.GroupRel;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.ResourceAcl;
import org.realtimeetl.parser.model.Role;
import org.realtimeetl.parser.model.UserInfo;
import org.realtimeetl.parser.model.UserRole;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.SortEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.model.DatabaseSyncTask;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link ResourceAclService} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
@Service
public class ResourceAclServiceImpl implements ResourceAclService {

    private static final String PERM_ACL_EDIT = "acl:edit";

    @Resource
    private ResourceAclProfile resourceAclProfile;

    @Resource
    private GroupProfile groupProfile;

    @Resource
    private GroupRelProfile groupRelProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    @Resource
    private DatabaseSyncProfile databaseSyncProfile;

    @Resource
    private RbacProfile rbacProfile;

    @Resource
    private UserProfile userProfile;

    @Resource
    private PermissionService permissionService;

    @Resource
    private PermissionChecker permissionChecker;

    @Override
    public List<ResourceAclVO> listAll() {
        List<ResourceAcl> list = resourceAclProfile.listAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<ResourceAclVO> result = new ArrayList<>(list.size());
        for (ResourceAcl acl : list) {
            if (acl != null) {
                result.add(toResourceAclVO(acl));
            }
        }
        result.sort(Comparator
                .comparingInt(ResourceAclVO::getSubjectType)
                .thenComparing(ResourceAclVO::getSubjectName, Comparator.nullsLast(String::compareTo))
                .thenComparing(ResourceAclVO::getResourceName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    @Override
    public Paging<ResourceAclVO> query(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params == null ? null : params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params == null ? null : params.get("pageSize"), 10);
   
        Query query = new Query(pageNum, pageSize);
        String subjectTypeRaw = params == null ? null : params.get(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE);
        if (StringUtil.isNotBlank(subjectTypeRaw) && NumberUtil.isCreatable(subjectTypeRaw.trim())) {
            int subjectType = NumberUtil.toInt(subjectTypeRaw.trim());
            if (ResourceAclSubjectEnum.isSupported(subjectType)) {
                query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, subjectType);
            }
        }
        String subjectId = params == null ? null : params.get(ConfigConstant.RESOURCE_ACL_SUBJECT_ID);
        if (StringUtil.isNotBlank(subjectId)) {
            query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_ID, subjectId.trim());
        }
        query.addOrderBy(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, SortEnum.ASC);
        query.addOrderBy(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, SortEnum.DESC);

        Paging paging = resourceAclProfile.query(query);
        Paging<ResourceAclVO> result = new Paging<>(pageNum, pageSize);
        result.setTotal(paging == null ? 0 : paging.getTotal());
        if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
            result.setData(Collections.emptyList());
            return result;
        }
        List<ResourceAclVO> rows = new ArrayList<>(paging.getData().size());
        for (Object item : paging.getData()) {
            if (item instanceof ResourceAcl) {
                rows.add(toResourceAclVO((ResourceAcl) item));
            }
        }
        result.setData(rows);
        return result;
    }

    @Override
    public ResourceAclVO get(String id) {
        Assert.hasText(id, "Authorized ID is not empty.");
        ResourceAcl acl = resourceAclProfile.get(id);
        Assert.notNull(acl, "License does not exist");
        return toResourceAclVO(acl);
    }

    @Override
    public synchronized String save(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_ACL_EDIT);

        int subjectType = normalizeSubjectType(params.get("subjectType"));
        String subjectId = resolveSubjectId(subjectType, params.get("subjectId"), params.get("username"));
        int permission = normalizePermission(params.get("permission"));

        String id = params.get("id");
        // EditSingle strip: tapeid Only the record is updated
        if (StringUtil.isNotBlank(id)) {
            String resourceType = normalizeResourceType(params.get("resourceType"));
            List<String> resourceIds = parseResourceIds(params.get("resourceIds"), params.get("resourceId"));
            Assert.isTrue(!CollectionUtils.isEmpty(resourceIds), "Select Resources");
            String resourceId = resourceIds.get(0);
            assertResourceExists(resourceType, resourceId);
            ResourceAcl existing = resourceAclProfile.get(id);
            Assert.notNull(existing, "License does not exist");
            ResourceAcl conflict = resourceAclProfile.getByUnique(subjectType, subjectId, resourceType, resourceId);
            Assert.isTrue(conflict == null || StringUtil.equals(conflict.getId(), id),
                    "The subject already has the same resource authorization, please do not repeat");
            existing.setSubjectType(subjectType);
            existing.setSubjectId(subjectId);
            existing.setResourceType(resourceType);
            existing.setResourceId(resourceId);
            existing.setPermission(permission);
            existing.setUpdateTime(System.currentTimeMillis());
            resourceAclProfile.update(existing);
            return existing.getId();
        }

        // Insert：SupportINTERTypeBulk,payload Likegroup:id1,id2|connector:id3
        List<String[]> bindings = parseResourceBindings(params);
        Assert.isTrue(!CollectionUtils.isEmpty(bindings), "Select Resources");
        int created = 0;
        int updated = 0;
        for (String[] binding : bindings) {
            String resourceType = binding[0];
            String resourceId = binding[1];
            assertResourceExists(resourceType, resourceId);
            if (upsertAcl(subjectType, subjectId, resourceType, resourceId, permission)) {
                created++;
            } else {
                updated++;
            }
        }
        return String.format("Could not close temporary folder: %s", bindings.size(), created, updated);
    }

    @Override
    public synchronized void grantOwner(String username, String resourceType, String resourceId) {
        if (StringUtil.isBlank(username) || StringUtil.isBlank(resourceType) || StringUtil.isBlank(resourceId)) {
            return;
        }
        if (permissionService.isAdmin(username)) {
            return;
        }
        if (!ResourceTypeEnum.isLeaf(resourceType)) {
            return;
        }
        UserInfo user = userProfile.getByUsername(username);
        if (user == null || StringUtil.isBlank(user.getId())) {
            return;
        }
        upsertAcl(ResourceAclSubjectEnum.USER.getCode(), user.getId(), resourceType, resourceId,
                ResourceAclPermissionEnum.READ_WRITE.getCode());
    }

    /**
     * Press the only keyInsertorOverwritePermissions。
     *
     * @return true Insert，false Overwrite
     */
    private boolean upsertAcl(int subjectType, String subjectId, String resourceType, String resourceId, int permission) {
        long now = System.currentTimeMillis();
        ResourceAcl existing = resourceAclProfile.getByUnique(subjectType, subjectId, resourceType, resourceId);
        if (existing != null) {
            existing.setPermission(permission);
            existing.setUpdateTime(now);
            resourceAclProfile.update(existing);
            return false;
        }
        ResourceAcl acl = new ResourceAcl();
        acl.setSubjectType(subjectType);
        acl.setSubjectId(subjectId);
        acl.setResourceType(resourceType);
        acl.setResourceId(resourceId);
        acl.setPermission(permission);
        acl.setCreateTime(now);
        acl.setUpdateTime(now);
        resourceAclProfile.add(acl);
        return true;
    }

    /**
     * Parsing.Resource：PriorityresourcePayload（type:id,...|type:id），NoOnlyType resourceType + resourceIds。
     * Each one is[resourceType, resourceId]。
     */
    private List<String[]> parseResourceBindings(Map<String, String> params) {
        List<String[]> bindings = new ArrayList<>();
        String payload = params.get("resourcePayload");
        if (StringUtil.isNotBlank(payload)) {
            for (String segment : payload.split("\\|")) {
                if (StringUtil.isBlank(segment) || !segment.contains(":")) {
                    continue;
                }
                int idx = segment.indexOf(':');
                String type = normalizeResourceType(segment.substring(0, idx));
                for (String resourceId : parseResourceIds(segment.substring(idx + 1), null)) {
                    bindings.add(new String[]{type, resourceId});
                }
            }
            return bindings;
        }
        String resourceType = normalizeResourceType(params.get("resourceType"));
        for (String resourceId : parseResourceIds(params.get("resourceIds"), params.get("resourceId"))) {
            bindings.add(new String[]{resourceType, resourceId});
        }
        return bindings;
    }

    private List<String> parseResourceIds(String resourceIds, String resourceId) {
        String raw = resourceIds;
        if (StringUtil.isBlank(raw)) {
            raw = resourceId;
        }
        if (StringUtil.isBlank(raw)) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(StringUtil.COMMA))
                .map(StringUtil::trim)
                .filter(StringUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public synchronized String remove(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_ACL_EDIT);
        String id = params.get("id");
        Assert.hasText(id, "Authorized ID is not empty.");
        ResourceAcl acl = resourceAclProfile.get(id);
        Assert.notNull(acl, "License does not exist");
        resourceAclProfile.remove(id);
        return "Deleted successfully";
    }

    @Override
    public void revokeAllForResource(String resourceType, String resourceId) {
        if (StringUtil.isBlank(resourceType) || StringUtil.isBlank(resourceId)) {
            return;
        }
        resourceAclProfile.removeByResource(resourceType, resourceId);
    }

    @Override
    public void revokeAllForSubject(int subjectType, String subjectId) {
        if (StringUtil.isBlank(subjectId)) {
            return;
        }
        resourceAclProfile.removeBySubject(subjectType, subjectId);
    }

    @Override
    public boolean check(String username, String resourceType, String resourceId, String action) {
        if (StringUtil.isBlank(username) || StringUtil.isBlank(resourceType)
                || StringUtil.isBlank(resourceId) || StringUtil.isBlank(action)) {
            return false;
        }
        if (permissionService.isAdmin(username)) {
            return true;
        }
        UserInfo user = userProfile.getByUsername(username);
        if (user == null || StringUtil.isBlank(user.getId())) {
            return false;
        }
        String need = action.trim().toLowerCase(Locale.ROOT);
        if (hasAction(resourceAclProfile.listBySubject(ResourceAclSubjectEnum.USER.getCode(), user.getId()),
                resourceType, resourceId, need)) {
            return true;
        }
        List<UserRole> userRoles = rbacProfile.listUserRolesByUserId(user.getId());
        if (!CollectionUtils.isEmpty(userRoles)) {
            for (UserRole ur : userRoles) {
                if (ur == null || StringUtil.isBlank(ur.getRoleId())) {
                    continue;
                }
                if (hasAction(resourceAclProfile.listBySubject(ResourceAclSubjectEnum.ROLE.getCode(), ur.getRoleId()),
                        resourceType, resourceId, need)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void require(String username, String resourceType, String resourceId, String action) {
        Assert.hasText(username, "Unable to get logged-in user.");
        Assert.isTrue(check(username, resourceType, resourceId, action), "No resource permission");
    }

    @Override
    public Set<String> resolveAllowedIds(String username, String resourceType, String action) {
        if (StringUtil.isBlank(username) || StringUtil.isBlank(resourceType) || StringUtil.isBlank(action)) {
            return Collections.emptySet();
        }
        if (permissionService.isAdmin(username)) {
            return null;
        }
        UserInfo user = userProfile.getByUsername(username);
        if (user == null || StringUtil.isBlank(user.getId())) {
            return Collections.emptySet();
        }
        Set<String> allowed = new LinkedHashSet<>();
        collectAllowedIds(resourceAclProfile.listBySubject(ResourceAclSubjectEnum.USER.getCode(), user.getId()), resourceType, action, allowed);
        List<UserRole> userRoles = rbacProfile.listUserRolesByUserId(user.getId());
        if (!CollectionUtils.isEmpty(userRoles)) {
            for (UserRole ur : userRoles) {
                collectAllowedIds(resourceAclProfile.listBySubject(ResourceAclSubjectEnum.ROLE.getCode(), ur.getRoleId()),
                        resourceType, action, allowed);
            }
        }
        return allowed;
    }

    @Override
    public GroupSearchFilter applyToSearchFilter(GroupSearchFilter groupFilter, Set<String> allowedIds) {
        GroupSearchFilter base = groupFilter == null ? GroupSearchFilter.NONE : groupFilter;
        if (allowedIds == null) {
            return base;
        }
        if (allowedIds.isEmpty()) {
            return GroupSearchFilter.EMPTY;
        }
        if (base.isEmptyResult()) {
            return GroupSearchFilter.EMPTY;
        }
        if (!base.hasIdFilter()) {
            return new GroupSearchFilter(false, new ArrayList<>(allowedIds));
        }
        List<String> intersected = base.getIds().stream()
                .filter(allowedIds::contains)
                .collect(Collectors.toList());
        if (intersected.isEmpty()) {
            return GroupSearchFilter.EMPTY;
        }
        return new GroupSearchFilter(false, intersected);
    }

    @Override
    public <T> List<T> filter(String username, String resourceType, List<T> list,
                              Function<T, String> idGetter, String action) {
        if (CollectionUtils.isEmpty(list) || idGetter == null) {
            return list == null ? Collections.emptyList() : list;
        }
        Set<String> allowed = resolveAllowedIds(username, resourceType, action);
        if (allowed == null) {
            return list;
        }
        if (allowed.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(list.size());
        for (T item : list) {
            if (item == null) {
                continue;
            }
            String id = idGetter.apply(item);
            if (StringUtil.isNotBlank(id) && allowed.contains(id)) {
                result.add(item);
            }
        }
        return result;
    }

    private void collectAllowedIds(List<ResourceAcl> resourceIds, String resourceType, String need, Set<String> out) {
        if (CollectionUtils.isEmpty(resourceIds) || out == null) {
            return;
        }
        String action = StringUtil.isBlank(need) ? StringUtil.EMPTY : need.trim().toLowerCase(Locale.ROOT);
        for (ResourceAcl resource : resourceIds) {
            if (resource == null || !ResourceAclPermissionEnum.satisfies(resource.getPermission(), action)) {
                continue;
            }
            if (StringUtil.equals(resourceType, resource.getResourceType())) {
                out.add(resource.getResourceId());
                continue;
            }
            if (StringUtil.equals(ResourceTypeEnum.GROUP.getCode(), resource.getResourceType()) && StringUtil.isNotBlank(resource.getResourceId())) {
                List<String> memberIds = groupRelProfile.listResourceIds(resource.getResourceId(), resourceType);
                if (!CollectionUtils.isEmpty(memberIds)) {
                    out.addAll(memberIds);
                }
            }
        }
    }

    private boolean hasAction(List<ResourceAcl> acls, String resourceType, String resourceId, String need) {
        if (CollectionUtils.isEmpty(acls)) {
            return false;
        }
        for (ResourceAcl acl : acls) {
            if (acl == null || !ResourceAclPermissionEnum.satisfies(acl.getPermission(), need)) {
                continue;
            }
            if (StringUtil.equals(resourceType, acl.getResourceType())
                    && StringUtil.equals(resourceId, acl.getResourceId())) {
                return true;
            }
            if (ResourceTypeEnum.GROUP.getCode().equals(acl.getResourceType()) && StringUtil.isNotBlank(acl.getResourceId())) {
                List<GroupRel> rels = groupRelProfile.listByResource(resourceType, resourceId);
                if (!CollectionUtils.isEmpty(rels)) {
                    for (GroupRel rel : rels) {
                        if (rel != null && StringUtil.equals(acl.getResourceId(), rel.getGroupId())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ResourceAclVO toResourceAclVO(ResourceAcl acl) {
        ResourceAclVO vo = new ResourceAclVO();
        BeanUtils.copyProperties(acl, vo);
        ResourceAclSubjectEnum subject = ResourceAclSubjectEnum.get(acl.getSubjectType());
        vo.setSubjectTypeName(subject == null ? String.valueOf(acl.getSubjectType()) : subject.getName());
        vo.setSubjectName(resolveSubjectName(acl.getSubjectType(), acl.getSubjectId()));
        vo.setResourceName(resolveResourceName(acl.getResourceType(), acl.getResourceId()));
        ResourceAclPermissionEnum permission = ResourceAclPermissionEnum.get(acl.getPermission());
        vo.setPermissionName(permission == null ? String.valueOf(acl.getPermission()) : permission.getName());
        return vo;
    }

    private String resolveSubjectName(int subjectType, String subjectId) {
        ResourceAclSubjectEnum subject = ResourceAclSubjectEnum.get(subjectType);
        if (subject == null) {
            return subjectId;
        }
        if (subject.isRole()) {
            Role role = rbacProfile.getRoleById(subjectId);
            return role == null ? subjectId : role.getName();
        }
        List<UserInfo> users = userProfile.listUserInfos();
        if (!CollectionUtils.isEmpty(users)) {
            for (UserInfo user : users) {
                if (user != null && StringUtil.equals(subjectId, user.getId())) {
                    return user.getNickname() + " (" + user.getUsername() + ")";
                }
            }
        }
        return subjectId;
    }

    private String resolveResourceName(String resourceType, String resourceId) {
        ResourceTypeEnum type = ResourceTypeEnum.get(resourceType);
        if (type == null) {
            return resourceId;
        }
        switch (type) {
            case GROUP:
                Group group = groupProfile.getGroup(resourceId);
                return group == null ? resourceId : group.getName();
            case CONNECTOR:
                Connector connector = connectorProfile.getConnector(resourceId);
                return connector == null ? resourceId : connector.getName();
            case MAPPING:
                Mapping mapping = mappingProfile.get(resourceId);
                return mapping == null ? resourceId : mapping.getName();
            case VALIDATE_SYNC:
                ValidateSyncTask validateSyncTask = validateSyncProfile.get(resourceId);
                return validateSyncTask == null ? resourceId : validateSyncTask.getName();
            case DATABASE_SYNC:
                DatabaseSyncTask databaseSyncTask = databaseSyncProfile.get(resourceId);
                return databaseSyncTask == null ? resourceId : databaseSyncTask.getName();
            default:
                return resourceId;
        }
    }

    private String normalizeResourceType(String raw) {
        String type = StringUtil.isBlank(raw) ? ResourceTypeEnum.GROUP.getCode() : raw.trim();
        Assert.isTrue(ResourceTypeEnum.isSupported(type), "Unsupported resource type: " + type);
        return type;
    }

    private void assertResourceExists(String resourceType, String resourceId) {
        ResourceTypeEnum type = ResourceTypeEnum.get(resourceType);
        Assert.notNull(type, "Unsupported resource type: " + resourceType);
        switch (type) {
            case GROUP:
                Assert.notNull(groupProfile.getGroup(resourceId), "Group does not exist");
                return;
            case CONNECTOR:
                Assert.notNull(connectorProfile.getConnector(resourceId), "Technical connection does not exist");
                return;
            case MAPPING:
                Assert.notNull(mappingProfile.get(resourceId), "Sync task does not exist");
                return;
            case VALIDATE_SYNC:
                Assert.notNull(validateSyncProfile.get(resourceId), "The revised verification task does not exist");
                return;
            case DATABASE_SYNC:
                Assert.notNull(databaseSyncProfile.get(resourceId), "The vault migration task does not exist");
                return;
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }
    }

    private String resolveSubjectId(int subjectType, String subjectId, String username) {
        ResourceAclSubjectEnum subject = ResourceAclSubjectEnum.get(subjectType);
        Assert.notNull(subject, "Subject type only supports users/roles");
        if (StringUtil.isNotBlank(subjectId)) {
            if (subject.isRole()) {
                Assert.notNull(rbacProfile.getRoleById(subjectId), "Role does not exist");
            } else {
                Assert.notNull(userProfile.getById(subjectId), "User does not exist");
            }
            return subjectId;
        }
        if (subject.isRole()) {
            throw new IllegalArgumentException("Please select a role");
        }
        Assert.hasText(username, "Select User");
        UserInfo user = userProfile.getByUsername(username.trim());
        Assert.notNull(user, "User does not exist");
        Assert.hasText(user.getId(), "User ID invalid");
        return user.getId();
    }

    private int normalizeSubjectType(String raw) {
        Assert.hasText(raw, "Subject type cannot be empty");
        Assert.isTrue(NumberUtil.isCreatable(raw.trim()), "Invalid Subject Type");
        int code = NumberUtil.toInt(raw.trim());
        Assert.isTrue(ResourceAclSubjectEnum.isSupported(code), "Subject type only supports users/roles");
        return code;
    }

    private int normalizePermission(String raw) {
        Assert.hasText(raw, "Please select permissions");
        Assert.isTrue(NumberUtil.isCreatable(raw.trim()), "Permission invalid");
        int code = NumberUtil.toInt(raw.trim());
        Assert.isTrue(ResourceAclPermissionEnum.isSupported(code), "Permissions only to read/read/write");
        return code;
    }
}
