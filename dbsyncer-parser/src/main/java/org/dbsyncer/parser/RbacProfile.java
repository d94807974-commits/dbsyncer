/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.parser.model.Permission;
import org.dbsyncer.parser.model.Role;
import org.dbsyncer.parser.model.RolePermission;
import org.dbsyncer.parser.model.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * RBAC Read & WriteRole / PermissionsPoint/ relation).
 *
 * @author wuji
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
