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
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.Permission;
import org.realtimeetl.parser.model.Role;
import org.realtimeetl.parser.model.RolePermission;
import org.realtimeetl.parser.model.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * RBAC Read & WriteRole / PermissionsPoint/ relation).
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
public interface RbacProfile {

    List<Role> listRoles();

    String addRole(Role role);

    String updateRole(Role role);

    /**
     * PressID Remove Role。
     */
    void removeRole(String roleId);

    Permission getPermissionByCode(String code);

    List<Permission> listPermissions();

    void addPermission(Permission permission);

    RolePermission getRolePermission(String roleId, String permissionId);

    List<RolePermission> listRolePermissionsByRoleId(String roleId);

    /**
     * PressRole ID Batch CheckRole-PermissionsRelationshipIN）。
     */
    List<RolePermission> listRolePermissionsByRoleIds(Collection<String> roleIds);

    void addRolePermission(RolePermission rel);

    /**
     * Remove RoleAllPermissionsTie.
     */
    void removeRolePermissionsByRoleId(String roleId);

    UserRole getUserRole(String userId, String roleId);

    List<UserRole> listUserRolesByUserId(String userId);

    /**
     * PressRoleCha.UserTieDeleteFormer occupation check.
     */
    List<UserRole> listUserRolesByRoleId(String roleId);

    String addUserRole(UserRole rel);

    /**
     * DeleteUserAllRoleTie.
     */
    void removeUserRolesByUserId(String userId);

    Role getRoleById(String id);

    Permission getPermissionById(String id);

    /**
     * PressPermissions ID Batch CheckPermissionsPointIN）。
     */
    List<Permission> listPermissionsByIds(Collection<String> permissionIds);
}
