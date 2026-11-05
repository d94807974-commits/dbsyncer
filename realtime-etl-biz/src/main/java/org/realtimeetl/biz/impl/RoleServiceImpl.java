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

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.PermissionChecker;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.RoleService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.enums.PermissionModuleEnum;
import org.realtimeetl.biz.enums.ResourceAclSubjectEnum;
import org.realtimeetl.biz.vo.PermissionVO;
import org.realtimeetl.biz.vo.RoleVO;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.RbacProfile;
import org.realtimeetl.parser.model.Permission;
import org.realtimeetl.parser.model.Role;
import org.realtimeetl.parser.model.RolePermission;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link RoleService} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final String PERM_USER_EDIT = "user:edit";

    @Resource
    private RbacProfile rbacProfile;

    @Resource
    private PermissionChecker permissionChecker;

    @Resource
    private ResourceAclService resourceAclService;

    @Override
    public List<RoleVO> listRoles() {
        List<Role> roles = rbacProfile.listRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        List<RoleVO> result = new ArrayList<>(roles.size());
        for (Role role : roles) {
            if (role == null) {
                continue;
            }
            result.add(toRoleVO(role, false));
        }
        result.sort(Comparator.comparing(RoleVO::getName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    @Override
    public RoleVO getRole(String roleId) {
        Assert.hasText(roleId, "Role ID cannot be empty.");
        Role role = rbacProfile.getRoleById(roleId);
        Assert.notNull(role, "Role does not exist");
        return toRoleVO(role, true);
    }

    @Override
    public List<PermissionVO> listPermissions(String roleId) {
        List<Permission> permissions = rbacProfile.listPermissions();
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        Set<String> grantedIds = Collections.emptySet();
        if (StringUtil.isNotBlank(roleId)) {
            List<RolePermission> rels = rbacProfile.listRolePermissionsByRoleId(roleId);
            grantedIds = new HashSet<>();
            if (!CollectionUtils.isEmpty(rels)) {
                for (RolePermission rel : rels) {
                    if (rel != null && StringUtil.isNotBlank(rel.getPermissionId())) {
                        grantedIds.add(rel.getPermissionId());
                    }
                }
            }
        }
        List<PermissionVO> result = new ArrayList<>(permissions.size());
        for (Permission permission : permissions) {
            if (permission == null) {
                continue;
            }
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            vo.setGranted(grantedIds.contains(permission.getId()));
            vo.setModuleName(PermissionModuleEnum.resolveName(permission.getModule()));
            result.add(vo);
        }
        result.sort(Comparator
                .comparing(PermissionVO::getModule, Comparator.nullsLast(String::compareTo))
                .thenComparing(PermissionVO::getCode, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    @Override
    public synchronized String add(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_USER_EDIT);

        String name = requireName(params.get("name"));
        List<Permission> selected = resolveSelectedPermissions(params.get("permissionCodes"));

        long now = System.currentTimeMillis();
        Role role = new Role();
        role.setName(name);
        role.setCreateTime(now);
        role.setUpdateTime(now);
        String roleId = rbacProfile.addRole(role);
        Assert.hasText(roleId, "Creating Role Failed");
        Role created = rbacProfile.getRoleById(roleId);
        Assert.notNull(created, "Creating Role Failed");
        roleId = created.getId();

        bindPermissions(roleId, selected, now);
        return roleId;
    }

    @Override
    public synchronized String edit(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_USER_EDIT);

        String roleId = params.get("roleId");
        Assert.hasText(roleId, "Role ID cannot be empty.");
        Role role = rbacProfile.getRoleById(roleId);
        Assert.notNull(role, "Role does not exist");

        String name = requireName(params.get("name"));
        List<Permission> selected = resolveSelectedPermissions(params.get("permissionCodes"));

        long now = System.currentTimeMillis();
        role.setName(name);
        role.setUpdateTime(now);
        rbacProfile.updateRole(role);
        bindPermissions(roleId, selected, now);
        return roleId;
    }

    @Override
    public synchronized String remove(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String currentUserName = params.get(UserConfigService.CURRENT_USER_NAME);
        permissionChecker.requirePermission(currentUserName, PERM_USER_EDIT);

        String roleId = params.get("roleId");
        Assert.hasText(roleId, "Role ID cannot be empty.");
        Role role = rbacProfile.getRoleById(roleId);
        Assert.notNull(role, "Role does not exist");

        List<?> bound = rbacProfile.listUserRolesByRoleId(roleId);
        Assert.isTrue(CollectionUtils.isEmpty(bound), "Role still bound by user, cannot be deleted");

        rbacProfile.removeRolePermissionsByRoleId(roleId);
        rbacProfile.removeRole(roleId);
        resourceAclService.revokeAllForSubject(ResourceAclSubjectEnum.ROLE.getCode(), roleId);
        return "Delete Role Of Successful";
    }

    private void bindPermissions(String roleId, List<Permission> selected, long now) {
        rbacProfile.removeRolePermissionsByRoleId(roleId);
        if (CollectionUtils.isEmpty(selected)) {
            return;
        }
        for (Permission permission : selected) {
            RolePermission rel = new RolePermission();
            rel.setRoleId(roleId);
            rel.setPermissionId(permission.getId());
            rel.setCreateTime(now);
            rel.setUpdateTime(now);
            rbacProfile.addRolePermission(rel);
        }
    }

    private List<Permission> resolveSelectedPermissions(String permissionCodes) {
        Set<String> codes = parsePermissionCodes(permissionCodes);
        List<Permission> allPermissions = rbacProfile.listPermissions();
        Map<String, Permission> byCode = CollectionUtils.isEmpty(allPermissions)
                ? Collections.emptyMap()
                : allPermissions.stream()
                .filter(p -> p != null && StringUtil.isNotBlank(p.getCode()))
                .collect(Collectors.toMap(Permission::getCode, p -> p, (a, b) -> a));

        List<Permission> selected = new ArrayList<>();
        for (String code : codes) {
            Permission permission = byCode.get(code);
            if (permission == null) {
                throw new BizException("Unknown permission code: " + code);
            }
            selected.add(permission);
        }
        return selected;
    }

    private String requireName(String raw) {
        Assert.hasText(raw, "Role name cannot be empty");
        String name = raw.trim();
        Assert.isTrue(name.length() <= 64, "Role name too long");
        return name;
    }

    private RoleVO toRoleVO(Role role, boolean withCodes) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        List<RolePermission> rolePermissions = rbacProfile.listRolePermissionsByRoleId(role.getId());
        if (CollectionUtils.isEmpty(rolePermissions)) {
            vo.setPermissionCount(0);
            vo.setPermissionCodes(Collections.emptyList());
            return vo;
        }
        vo.setPermissionCount(rolePermissions.size());
        if (!withCodes) {
            return vo;
        }
        List<String> codes = new ArrayList<>();
        for (RolePermission rel : rolePermissions) {
            if (rel == null || StringUtil.isBlank(rel.getPermissionId())) {
                continue;
            }
            Permission permission = rbacProfile.getPermissionById(rel.getPermissionId());
            if (permission != null && StringUtil.isNotBlank(permission.getCode())) {
                codes.add(permission.getCode());
            }
        }
        codes.sort(String::compareTo);
        vo.setPermissionCodes(codes);
        return vo;
    }

    private Set<String> parsePermissionCodes(String raw) {
        if (StringUtil.isBlank(raw)) {
            return Collections.emptySet();
        }
        return Arrays.stream(raw.split(StringUtil.COMMA))
                .map(String::trim)
                .filter(StringUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
